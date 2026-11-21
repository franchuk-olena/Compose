package com.example.waterreminderworker

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.TimeUnit

class WaterReminderViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val prefs: SharedPreferences = context.getSharedPreferences("water_prefs", Context.MODE_PRIVATE)

    private val _reminderCount = MutableStateFlow(prefs.getInt("reminder_count", 0))
    val reminderCount: StateFlow<Int> = _reminderCount.asStateFlow()

    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == "reminder_count") {
            _reminderCount.value = prefs.getInt("reminder_count", 0)
        }
    }

    init {
        prefs.registerOnSharedPreferenceChangeListener(listener)
    }

    override fun onCleared() {
        super.onCleared()
        prefs.unregisterOnSharedPreferenceChangeListener(listener)
    }

    fun startReminder(intervalMinutes: Long) {
        val safeInterval = if (intervalMinutes < 15) 15L else intervalMinutes

        val request = PeriodicWorkRequestBuilder<WaterReminderWorker>(
            safeInterval, TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "water_reminder",
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun stopReminder() {
        WorkManager.getInstance(context).cancelUniqueWork("water_reminder")
    }
}