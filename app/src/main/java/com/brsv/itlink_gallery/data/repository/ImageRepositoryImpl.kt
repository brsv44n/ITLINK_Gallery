package com.brsv.itlink_gallery.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import coil.ImageLoader
import coil.request.CachePolicy
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult
import coil.size.Precision
import com.brsv.itlink_gallery.di.AppModule
import com.brsv.itlink_gallery.domain.models.CacheInfo
import com.brsv.itlink_gallery.domain.models.ImageFile
import com.brsv.itlink_gallery.domain.repository.ImageRepository
import com.brsv.itlink_gallery.utils.sha256
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val imageLoader: ImageLoader,
    @AppModule.IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ImageRepository {

    companion object {
        private const val CACHE_DURATION_MS = 7 * 24 * 60 * 60 * 1000L // 7 дней
        private const val PREVIEW_SIZE = 100
    }

    private val originalsDir = File(context.filesDir, "images/originals").apply { mkdirs() }
    private val previewsDir = File(context.filesDir, "images/previews").apply { mkdirs() }
    private val metadataDir = File(context.filesDir, "images/metadata").apply { mkdirs() }

    private val loadingMutex = Mutex()
    private val loadingUrls = ConcurrentHashMap<String, Deferred<ImageFile>>()

    override suspend fun prefetchImage(url: String): Result<ImageFile> =
        withContext(ioDispatcher) {
            runCatching {
                loadImageInternal(url, forceRefresh = false)
            }
        }

    override suspend fun getCachedImage(url: String): ImageFile? =
        withContext(ioDispatcher) {
            val hash = url.sha256()
            val original = File(originalsDir, "$hash.jpg")
            val preview = File(previewsDir, "${hash}_preview.jpg")
            val metadata = File(metadataDir, "$hash.meta")

            return@withContext if (original.exists() && preview.exists() &&
                isCacheValid(metadata)
            ) {
                ImageFile(original, preview)
            } else {
                null
            }
        }

    override suspend fun loadImage(url: String): ImageFile =
        withContext(ioDispatcher) {
            loadImageInternal(url, forceRefresh = false)
        }

    override suspend fun clearCache(): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            originalsDir.deleteRecursively()
            previewsDir.deleteRecursively()
            metadataDir.deleteRecursively()

            originalsDir.mkdirs()
            previewsDir.mkdirs()
            metadataDir.mkdirs()

            Unit
        }
    }

    override suspend fun getCacheInfo(): CacheInfo = withContext(ioDispatcher) {
        val originalFiles = originalsDir.listFiles()?.toList() ?: emptyList()
        val previewFiles = previewsDir.listFiles()?.toList() ?: emptyList()

        val totalSize = (originalFiles.sumOf { it.length() } +
                previewFiles.sumOf { it.length() })

        CacheInfo(
            originalCount = originalFiles.size,
            previewCount = previewFiles.size,
            totalSizeBytes = totalSize,
            originalDir = originalsDir.absolutePath,
            previewDir = previewsDir.absolutePath
        )
    }

    private suspend fun loadImageInternal(
        url: String,
        forceRefresh: Boolean
    ): ImageFile = coroutineScope {
        loadingMutex.withLock {
            val existingDeferred = loadingUrls[url]
            if (existingDeferred != null && !forceRefresh) {
                return@withLock existingDeferred.await()
            }

            val deferred = async {
                try {
                    val hash = url.sha256()
                    val originalFile = File(originalsDir, "$hash.jpg")
                    val previewFile = File(previewsDir, "${hash}_preview.jpg")
                    val metadataFile = File(metadataDir, "$hash.meta")

                    if (!forceRefresh &&
                        originalFile.exists() &&
                        previewFile.exists() &&
                        isCacheValid(metadataFile)
                    ) {
                        return@async ImageFile(originalFile, previewFile)
                    }

                    downloadImage(
                        url = url,
                        targetFile = originalFile,
                        sizePx = null
                    )

                    downloadImage(
                        url = url,
                        targetFile = previewFile,
                        sizePx = PREVIEW_SIZE
                    )

                    metadataFile.writeText(System.currentTimeMillis().toString())

                    ImageFile(originalFile, previewFile)

                } finally {
                    loadingMutex.withLock {
                        loadingUrls.remove(url)
                    }
                }
            }

            loadingUrls[url] = deferred
            deferred.await()
        }
    }

    private suspend fun downloadImage(
        url: String,
        targetFile: File,
        sizePx: Int?
    ) {
        val request = ImageRequest.Builder(context)
            .data(url)
            .apply {
                sizePx?.let { size(it) }
                precision(Precision.INEXACT)
                allowHardware(false)
                diskCachePolicy(CachePolicy.DISABLED) // Мы свой кэш используем
                memoryCachePolicy(CachePolicy.DISABLED)
            }
            .build()

        val result = imageLoader.execute(request)

        if (result is SuccessResult) {
            val drawable = result.drawable
            if (drawable is BitmapDrawable) {
                targetFile.outputStream().use { output ->
                    drawable.bitmap.compress(
                        Bitmap.CompressFormat.JPEG,
                        85,
                        output
                    )
                }
            } else {
                throw IOException("Failed to get bitmap from drawable")
            }
        } else {
            throw IOException("Failed to load image: ${(result as? ErrorResult)?.throwable?.message}")
        }
    }

    private fun isCacheValid(metadataFile: File): Boolean {
        if (!metadataFile.exists()) return false

        return runCatching {
            val cachedTime = metadataFile.readText().toLong()
            System.currentTimeMillis() - cachedTime < CACHE_DURATION_MS
        }.getOrDefault(false)
    }
}
