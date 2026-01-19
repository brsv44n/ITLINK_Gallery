package com.brsv.itlink_gallery.domain.models

sealed interface CacheState {
    object Empty : CacheState
    object Loading : CacheState
    data class Ready(val content: String, val timestamp: Long) : CacheState
    data class Error(val error: CacheError) : CacheState
}
