package com.example.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

// 1. Модель даних події
data class BroadcastEvent(
    val id: String = UUID.randomUUID().toString(),
    val type: String,
    val timestamp: String,
    val details: String
)


// 2. Репозиторій із callbackFlow
class BroadcastRepository(private val context: Context) {

    fun getBroadcastEvents(): Flow<BroadcastEvent> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                intent?.action?.let { action ->
                    val formatter = SimpleDateFormat("HH:mm:ss dd.MM.yyyy", Locale.getDefault())
                    val time = formatter.format(Date())

                    // Формуємо деталі із Extras Intent-у
                    val extrasDetails = intent.extras?.let { bundle ->
                        bundle.keySet().joinToString(", ") { key -> "$key = ${bundle.get(key)}" }
                    } ?: "Параметри відсутні"

                    val event = BroadcastEvent(
                        type = action,
                        timestamp = time,
                        details = "Деталі події: $extrasDetails\nIntent Action: $action"
                    )

                    trySend(event)
                }
            }
        }

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
            addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED)
            addAction(Intent.ACTION_BATTERY_LOW)
        }

        ContextCompat.registerReceiver(
            context,
            receiver,
            filter,
            ContextCompat.RECEIVER_EXPORTED
        )

        // Обов'язкове скасування реєстрації при закритті потоку
        awaitClose {
            context.unregisterReceiver(receiver)
        }
    }
}