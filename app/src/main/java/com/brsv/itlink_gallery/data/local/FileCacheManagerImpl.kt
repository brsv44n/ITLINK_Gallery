package com.brsv.itlink_gallery.data.local

import android.content.Context
import com.brsv.itlink_gallery.data.network.FileApi
import com.brsv.itlink_gallery.di.AppModule.IoDispatcher
import com.brsv.itlink_gallery.domain.FileCacheManager
import com.brsv.itlink_gallery.domain.models.CacheState
import com.brsv.itlink_gallery.domain.models.toCacheError
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.update
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileCacheManagerImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val fileApi: FileApi,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val fileName: String = "cached_images.txt",
) : FileCacheManager {

    companion object {

        private const val CACHE_DURATION_MS = 24 * 60 * 60 * 1000L // 24 часа
    }

    private val loadMutex = Mutex()
    private val fileMutex = Mutex()

    private val atomicCacheState = atomic<CacheState>(CacheState.Empty)

    private val _cacheState = MutableStateFlow(atomicCacheState.value)
    override val cacheState: StateFlow<CacheState> = _cacheState.asStateFlow()

    private val isLoading = atomic(false)

    private val cachedFile: File
        get() = File(context.filesDir, fileName)

    private val metadataFile: File
        get() = File(context.filesDir, "$fileName.meta")

    override suspend fun getFileContent(forceRefresh: Boolean): Result<String> =
        withContext(ioDispatcher) {

            if (isLoading.value && !forceRefresh) {
                return@withContext waitForResult()
            }

            loadMutex.withLock {
                try {
                    isLoading.value = true
                    updateState(CacheState.Loading)

                    val content = if (forceRefresh || shouldUpdateCache()) {
                        loadFromServer()
                    } else {
                        loadFromCache().getOrThrow()
                    }

                    updateState(CacheState.Ready(content, System.currentTimeMillis()))
                    Result.success(content)

                } catch (error: Throwable) {
                    updateState(CacheState.Error(error.toCacheError()))
                    Result.failure(error)
                } finally {
                    isLoading.value = false
                }
            }
        }

    private suspend fun loadFromServer(): String = fileMutex.withLock {
        val response = fileApi.getFile()
        if (!response.isSuccessful) {
            throw IOException("HTTP ${response.code()}")
        }

        val content = response.body()?.string()
            ?: throw IOException("Empty response body")

        cachedFile.writeText(content)
        metadataFile.writeText(System.currentTimeMillis().toString())

        content
    }

    private suspend fun loadFromCache(): Result<String> = fileMutex.withLock {
        runCatching {
            cachedFile.takeIf { it.exists() }?.readText()
                ?: throw IOException("Cache file not found")
        }
    }

    private suspend fun waitForResult(): Result<String> {
        return cacheState
            .filterIsInstance<CacheState.Ready>()
            .first()
            .let { Result.success(it.content) }
    }

    private fun updateState(newState: CacheState) {
        atomicCacheState.update { oldState ->
            if (shouldUpdateState(oldState, newState)) newState else oldState
        }

        _cacheState.value = atomicCacheState.value
    }

    private fun shouldUpdateCache(): Boolean {
        if (!cachedFile.exists() || !metadataFile.exists()) return true

        return runCatching {
            val lastUpdate = metadataFile.readText().toLong()
            System.currentTimeMillis() - lastUpdate > CACHE_DURATION_MS
        }.getOrDefault(true)
    }

    private fun shouldUpdateState(
        old: CacheState,
        new: CacheState
    ): Boolean =
        when {
            old is CacheState.Loading && new is CacheState.Loading -> false
            old is CacheState.Ready && new is CacheState.Ready -> false
            else -> true
        }
}
