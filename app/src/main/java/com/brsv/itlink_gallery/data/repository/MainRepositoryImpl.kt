package com.brsv.itlink_gallery.data.repository

import com.brsv.itlink_gallery.di.AppModule
import com.brsv.itlink_gallery.domain.FileCacheManager
import com.brsv.itlink_gallery.domain.models.CacheState
import com.brsv.itlink_gallery.domain.models.ContentItem
import com.brsv.itlink_gallery.domain.models.ImageFile
import com.brsv.itlink_gallery.domain.repository.ImageRepository
import com.brsv.itlink_gallery.domain.repository.MainRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MainRepositoryImpl @Inject constructor(
    private val fileCacheManager: FileCacheManager,
    private val imageRepository: ImageRepository,
    @param:AppModule.IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : MainRepository {

    private val parseMutex = Mutex()

    @Volatile
    private var cachedItems: List<ContentItem>? = null

    @Volatile
    private var lastParsedTimestamp: Long = 0L

    private val prefetchScope = CoroutineScope(
        ioDispatcher + SupervisorJob() +
                CoroutineExceptionHandler { _, e ->
                    e.printStackTrace()
                }
    )

    override suspend fun getAllItems(
        forceRefresh: Boolean
    ): Result<List<ContentItem>> = withContext(ioDispatcher) {

        parseMutex.withLock {
            val currentCache = cachedItems
            if (currentCache != null && !forceRefresh) {
                launchPrefetchForItems(currentCache)
                return@withContext Result.success(currentCache)
            }

            fileCacheManager
                .getFileContent(forceRefresh)
                .map { content ->
                    val items = parseContent(content)
                    cachedItems = items

                    launchPrefetchForItems(items)

                    items
                }
        }
    }

    override suspend fun getItem(
        index: Int,
        forceRefresh: Boolean
    ): Result<ContentItem> = withContext(ioDispatcher) {

        getAllItems(forceRefresh).map { items ->
            val item = items.getOrElse(index) {
                throw IndexOutOfBoundsException(
                    "Index=$index, size=${items.size}"
                )
            }

            if (item is ContentItem.Image) {
                prefetchImage(item.url)
            }

            item
        }
    }

    override fun observeItems(): Flow<List<ContentItem>> {
        return fileCacheManager.cacheState
            .filterIsInstance<CacheState.Ready>()
            .map { state ->
                parseMutex.withLock {
                    val currentCache = cachedItems
                    if (currentCache != null &&
                        state.timestamp <= lastParsedTimestamp
                    ) {
                        currentCache
                    } else {
                        val items = parseContent(state.content)
                        cachedItems = items
                        lastParsedTimestamp = state.timestamp

                        launchPrefetchForItems(items)

                        items
                    }
                }
            }
            .distinctUntilChanged()
    }

    private fun launchPrefetchForItems(items: List<ContentItem>) {
        prefetchScope.launch {
            val imageUrls = items
                .filterIsInstance<ContentItem.Image>()
                .map { it.url }
                .distinct()

            imageUrls.take(5).forEach { url ->
                try {
                    imageRepository.prefetchImage(url)
                } catch (e: Exception) {
                    //no-op
                }
            }

            imageUrls.drop(5).forEachIndexed { index, url ->
                delay(index * 500L)
                try {
                    imageRepository.prefetchImage(url)
                } catch (e: Exception) {
                    //no-op
                }
            }
        }
    }

    private suspend fun prefetchImage(url: String) {
        try {
            imageRepository.prefetchImage(url)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getCachedImageFile(url: String): ImageFile? {
        return try {
            imageRepository.getCachedImage(url)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun loadImageWithCache(url: String): Result<ImageFile> {
        return runCatching {
            imageRepository.loadImage(url)
        }
    }

    private fun parseContent(content: String): List<ContentItem> {
        return content
            .lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { mapLineToItem(it) }
            .toList()
    }

    private fun mapLineToItem(line: String): ContentItem {
        return if (isImageUrl(line)) {
            ContentItem.Image(
                raw = line,
                url = line
            )
        } else {
            ContentItem.Text(
                raw = line
            )
        }
    }

    private fun isImageUrl(value: String): Boolean {
        return value.startsWith("http", ignoreCase = true) &&
                IMAGE_EXTENSIONS.any { value.endsWith(it, ignoreCase = true) }
    }

    companion object {
        private val IMAGE_EXTENSIONS = setOf(
            ".jpg", ".jpeg", ".png", ".webp", ".gif"
        )
    }
}
