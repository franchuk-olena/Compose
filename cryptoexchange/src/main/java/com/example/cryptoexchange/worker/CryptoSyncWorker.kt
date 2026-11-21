package com.example.cryptoexchange.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.example.cryptoexchange.CryptoApplication

class CryptoSyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): ListenableWorker.Result {
        val application = applicationContext as? CryptoApplication ?: return ListenableWorker.Result.failure()
        val syncUseCase = application.container.syncCryptoUseCase

        val result = syncUseCase()
        return if (result.isSuccess) {
            ListenableWorker.Result.success()
        } else {
            ListenableWorker.Result.retry()
        }
    }
}
