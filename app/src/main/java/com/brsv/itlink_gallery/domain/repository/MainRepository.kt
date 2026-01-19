package com.brsv.itlink_gallery.domain.repository

import com.brsv.itlink_gallery.domain.models.ContentItem
import kotlinx.coroutines.flow.Flow

interface MainRepository {

    suspend fun getAllItems(
        forceRefresh: Boolean = false
    ): Result<List<ContentItem>>

    suspend fun getItem(
        index: Int,
        forceRefresh: Boolean = false
    ): Result<ContentItem>

    fun observeItems(): Flow<List<ContentItem>>

}
