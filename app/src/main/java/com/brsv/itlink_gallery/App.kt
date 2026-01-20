package com.brsv.itlink_gallery

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.brsv.itlink_gallery.di.AppModule
import com.brsv.itlink_gallery.domain.FileCacheManager
import com.brsv.itlink_gallery.domain.workers.FileCacheWorkScheduler
import com.brsv.itlink_gallery.presentation.main_screen.MainScreenComponentFactory
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import javax.inject.Inject

@HiltAndroidApp
class App : Application(), Configuration.Provider {

    @Inject
    lateinit var fileCacheManager: FileCacheManager

    @Inject
    @AppModule.AppCoroutineExceptionHandler
    lateinit var exceptionHandler: CoroutineExceptionHandler

    @Inject
    @AppModule.IoDispatcher
    lateinit var ioDispatcher: CoroutineDispatcher

    @Inject
    lateinit var mainScreenComponentFactory: MainScreenComponentFactory

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()

        FileCacheWorkScheduler.enqueueInitialSync(this)
        FileCacheWorkScheduler.enqueuePeriodicSync(this)

    }
}
