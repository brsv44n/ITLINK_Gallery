package com.brsv.itlink_gallery.domain.models

import java.io.File

data class ImageFile(
    val original: File,
    val preview: File
)
