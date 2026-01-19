package com.brsv.itlink_gallery.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import coil.ImageLoader
import coil.request.ImageRequest
import com.brsv.itlink_gallery.di.AppModule
import com.brsv.itlink_gallery.domain.models.ImageFiles
import com.brsv.itlink_gallery.domain.repository.ImageRepository
import com.brsv.itlink_gallery.utils.sha256
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val imageLoader: ImageLoader,
    @AppModule.IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ImageRepository {

    private val originalsDir = File(context.filesDir, "images/originals").apply { mkdirs() }
    private val previewsDir = File(context.filesDir, "images/previews").apply { mkdirs() }

    override suspend fun prefetchImage(url: String): Result<Unit> =
        withContext(ioDispatcher) {

            val hash = url.sha256()
            val originalFile = File(originalsDir, "$hash.jpg")
            val previewFile = File(previewsDir, "${hash}_preview.jpg")

            if (originalFile.exists() && previewFile.exists()) {
                return@withContext Result.success(Unit)
            }

            runCatching {
                downloadImage(url, originalFile)

                downloadImage(
                    url = url,
                    targetFile = previewFile,
                    sizePx = 200
                )
            }
        }

    override suspend fun getCachedImage(url: String): ImageFiles? =
        withContext(ioDispatcher) {
            val hash = url.sha256()
            val original = File(originalsDir, "$hash.jpg")
            val preview = File(previewsDir, "${hash}_preview.jpg")

            if (original.exists() && preview.exists()) {
                ImageFiles(original, preview)
            } else null
        }

    private suspend fun downloadImage(
        url: String,
        targetFile: File,
        sizePx: Int? = null
    ) {
        val request = ImageRequest.Builder(context)
            .data(url)
            .apply {
                sizePx?.let { size(it) }
                allowHardware(false)
            }
            .build()

        val result = imageLoader.execute(request)
        val bitmap = (result.drawable as BitmapDrawable).bitmap

        targetFile.outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it)
        }
    }
}
