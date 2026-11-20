package com.example.workmanager

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.workmanager.ui.theme.ComposeTheme
import java.util.UUID
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ComposeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NewsSyncScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

fun triggerOneTimeSync(context: Context): UUID {
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    val inputData = Data.Builder()
        .putString("user_token", "abc123")
        .build()

    val request = OneTimeWorkRequestBuilder<NewsSyncWorker>()
        .setConstraints(constraints)
        .setInputData(inputData)
        .build()

    WorkManager.getInstance(context).enqueue(request)
    return request.id
}

fun enableNightSync(context: Context) {
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.UNMETERED)
        .setRequiresCharging(true)
        .build()

    val periodicRequest = PeriodicWorkRequestBuilder<NewsSyncWorker>(15, TimeUnit.MINUTES)
        .setConstraints(constraints)
        .setInputData(Data.Builder().putString("user_token", "abc123_night").build())
        .build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "night_news_sync",
        ExistingPeriodicWorkPolicy.KEEP,
        periodicRequest
    )

    Toast.makeText(context, "Нічну синхронізацію увімкнено!", Toast.LENGTH_SHORT).show()
}

@Composable
fun NewsSyncScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val workManager = remember { WorkManager.getInstance(context) }

    var currentRequestId by remember { mutableStateOf<UUID?>(null) }

    val workInfoState = currentRequestId?.let { id ->
        workManager.getWorkInfoByIdLiveData(id).observeAsState()
    }
    val currentWorkInfo = workInfoState?.value

    val statusText = when (currentWorkInfo?.state) {
        WorkInfo.State.ENQUEUED -> "Очікує виконання..."
        WorkInfo.State.RUNNING -> "Виконується завантаження новин..."
        WorkInfo.State.SUCCEEDED -> {
            currentWorkInfo.outputData.getString("result") ?: "Успішно завантажено"
        }
        WorkInfo.State.FAILED -> "Помилка завантаження новин"
        WorkInfo.State.BLOCKED -> "Заблоковано умовою (немає мережі)"
        WorkInfo.State.CANCELLED -> "Синхронізацію скасовано"
        null -> "Очікує запуску..."
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "📰 Новинний Центр",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Статус одноразової синхронізації:",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                currentRequestId = triggerOneTimeSync(context)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Синхронізувати зараз")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { enableNightSync(context) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Увімкнути нічну синхронізацію")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NewsSyncScreenPreview() {
    ComposeTheme {
        NewsSyncScreen()
    }
}