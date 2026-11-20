package com.example.workmanager

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import kotlinx.coroutines.delay

class NewsSyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val token = inputData.getString("user_token") ?: "default_token"
        Log.d("NewsSyncWorker", "Початок завантаження новин з токеном: $token")

        // Імітація завантаження з мережі 3 секунди
        delay(3000)

        Log.d("NewsSyncWorker", "Новини успішно завантажено!")

        val outputData = Data.Builder()
            .putString("result", "Успіх: завантажено 15 новин")
            .build()

        return Result.success(outputData)
    }
}