package com.brsv.itlink_gallery.domain.repository

import com.brsv.itlink_gallery.domain.models.ImageFiles

interface ImageRepository {

    suspend fun prefetchImage(url: String): Result<Unit>

    suspend fun getCachedImage(url: String): ImageFiles?
}
