package com.brsv.itlink_gallery.di

import android.content.Context
import android.util.Log
import coil.ImageLoader
import coil.request.CachePolicy
import com.brsv.itlink_gallery.BuildConfig
import com.brsv.itlink_gallery.data.local.FileCacheManagerImpl
import com.brsv.itlink_gallery.data.network.FileApi
import com.brsv.itlink_gallery.data.repository.ImageRepositoryImpl
import com.brsv.itlink_gallery.data.repository.MainRepositoryImpl
import com.brsv.itlink_gallery.domain.FileCacheManager
import com.brsv.itlink_gallery.domain.repository.ImageRepository
import com.brsv.itlink_gallery.domain.repository.MainRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import retrofit2.HttpException
import retrofit2.Retrofit
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class IoDispatcher

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class AppCoroutineExceptionHandler

    @Provides
    @Singleton
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher =
        Dispatchers.IO

    @Provides
    @Singleton
    @AppCoroutineExceptionHandler
    fun provideCoroutineExceptionHandler(): CoroutineExceptionHandler =
        CoroutineExceptionHandler { _, exception ->
            when (exception) {
                is IOException -> {
                    Log.e("Network", "Network error: ${exception.message}")
                }

                is HttpException -> {
                    Log.e("Network", "HTTP error ${exception.code()}: ${exception.message()}")
                }

                is CancellationException -> {
                    Log.d("Coroutine", "Coroutine cancelled")
                }

                else -> {
                    Log.e("App", "Unexpected error", exception)
                }
            }
        }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .build()

    @Provides
    @Singleton
    fun provideFileApi(
        retrofit: Retrofit
    ): FileApi =
        retrofit.create(FileApi::class.java)

    @Provides
    @Singleton
    fun provideFileCacheManager(
        @ApplicationContext context: Context,
        fileApi: FileApi,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): FileCacheManager =
        FileCacheManagerImpl(
            context = context,
            fileApi = fileApi,
            ioDispatcher = ioDispatcher
        )

    @Provides
    @Singleton
    fun provideContentRepository(
        fileCacheManager: FileCacheManager,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): MainRepository =
        MainRepositoryImpl(
            fileCacheManager = fileCacheManager,
            ioDispatcher = ioDispatcher
        )

    @Provides
    @Singleton
    fun provideImageLoader(
        @ApplicationContext context: Context
    ): ImageLoader =
        ImageLoader.Builder(context)
            .respectCacheHeaders(false)
            .diskCachePolicy(CachePolicy.DISABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .build()

    @Provides
    @Singleton
    fun provideImageRepository(
        @ApplicationContext context: Context,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
        imageLoader: ImageLoader
    ): ImageRepository = ImageRepositoryImpl(
        context = context,
        imageLoader = imageLoader,
        ioDispatcher = ioDispatcher
    )
}
