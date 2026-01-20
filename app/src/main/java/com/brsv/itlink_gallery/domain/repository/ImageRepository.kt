package com.brsv.itlink_gallery.domain.repository

import com.brsv.itlink_gallery.domain.models.CacheInfo
import com.brsv.itlink_gallery.domain.models.ImageFile

interface ImageRepository {

    suspend fun prefetchImage(url: String): Result<ImageFile>

    suspend fun getCachedImage(url: String): ImageFile?

    suspend fun loadImage(url: String): ImageFile

    suspend fun clearCache(): Result<Unit>

    suspend fun getCacheInfo(): CacheInfo

}
