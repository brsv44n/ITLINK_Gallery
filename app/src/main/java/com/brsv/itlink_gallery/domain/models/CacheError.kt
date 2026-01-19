package com.brsv.itlink_gallery.domain.models

import java.io.IOException

sealed interface CacheError {
    data class Network(val cause: Throwable) : CacheError
    data class Io(val cause: Throwable) : CacheError
    data class Unknown(val cause: Throwable) : CacheError
}

fun Throwable.toCacheError(): CacheError =
    when (this) {
        is IOException -> CacheError.Io(this)
        else -> CacheError.Unknown(this)
    }
