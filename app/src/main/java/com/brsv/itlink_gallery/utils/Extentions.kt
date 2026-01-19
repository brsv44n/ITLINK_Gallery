package com.brsv.itlink_gallery.utils

import java.security.MessageDigest

fun String.sha256(): String {
    val bytes = MessageDigest
        .getInstance("SHA-256")
        .digest(toByteArray(Charsets.UTF_8))

    return buildString(bytes.size * 2) {
        bytes.forEach { byte ->
            append("%02x".format(byte))
        }
    }
}
