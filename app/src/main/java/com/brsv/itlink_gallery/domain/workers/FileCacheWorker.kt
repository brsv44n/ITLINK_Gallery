package com.brsv.itlink_gallery.domain.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.brsv.itlink_gallery.domain.FileCacheManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class FileCacheWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val fileCacheManager: FileCacheManager
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return fileCacheManager
            .getFileContent(forceRefresh = true)
            .fold(
                onSuccess = { Result.success() },
                onFailure = { Result.retry() }
            )
    }
}
