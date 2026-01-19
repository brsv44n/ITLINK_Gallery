package com.brsv.itlink_gallery.domain

import com.brsv.itlink_gallery.domain.models.CacheState
import kotlinx.coroutines.flow.StateFlow

interface FileCacheManager {

    val cacheState: StateFlow<CacheState>

    suspend fun getFileContent(forceRefresh: Boolean = false): Result<String>

}
