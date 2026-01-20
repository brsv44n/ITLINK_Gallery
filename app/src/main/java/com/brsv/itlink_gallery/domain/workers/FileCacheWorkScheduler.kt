package com.brsv.itlink_gallery.domain.workers

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object FileCacheWorkScheduler {

    private const val UNIQUE_INITIAL_WORK = "file_cache_initial"
    private const val UNIQUE_PERIODIC_WORK = "file_cache_periodic"

    private val networkConstraints: Constraints =
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

    fun enqueueInitialSync(context: Context) {
        val request =
            OneTimeWorkRequestBuilder<FileCacheWorker>()
                .setConstraints(networkConstraints)
                .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                UNIQUE_INITIAL_WORK,
                ExistingWorkPolicy.KEEP,
                request
            )
    }

    fun enqueuePeriodicSync(context: Context) {
        val request =
            PeriodicWorkRequestBuilder<FileCacheWorker>(
                24, TimeUnit.HOURS
            )
                .setConstraints(networkConstraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    30, TimeUnit.SECONDS
                )
                .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                UNIQUE_PERIODIC_WORK,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
    }
}
