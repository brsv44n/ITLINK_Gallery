package com.brsv.itlink_gallery.domain.models

data class CacheInfo(
    val originalCount: Int,
    val previewCount: Int,
    val totalSizeBytes: Long,
    val originalDir: String,
    val previewDir: String
)
