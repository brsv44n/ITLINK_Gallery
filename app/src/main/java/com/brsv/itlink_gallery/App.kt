package com.brsv.itlink_gallery

import android.app.Application
import android.util.Log
import com.brsv.itlink_gallery.di.AppModule
import com.brsv.itlink_gallery.domain.FileCacheManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class App : Application() {

    @Inject
    lateinit var fileCacheManager: FileCacheManager

    @Inject
    @AppModule.AppCoroutineExceptionHandler
    lateinit var exceptionHandler: CoroutineExceptionHandler

    @Inject
    @AppModule.IoDispatcher
    lateinit var ioDispatcher: CoroutineDispatcher

    override fun onCreate() {
        super.onCreate()

        Log.d("App", "Application started")

        CoroutineScope(
            SupervisorJob() + ioDispatcher + exceptionHandler
        ).launch {
            Log.d("App", "Requesting file content...")

            val result = fileCacheManager.getFileContent()

            result
                .onSuccess {
                    Log.d("App", "File loaded successfully:")
                    Log.d("App", it.take(200)) // log first 200 chars
                }
                .onFailure {
                    Log.e("App", "File load failed", it)
                }
        }
    }
}
