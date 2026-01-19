package com.brsv.itlink_gallery.data.network

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET

interface FileApi {

    @GET("test/images.txt")
    suspend fun getFile(): Response<ResponseBody>
}
