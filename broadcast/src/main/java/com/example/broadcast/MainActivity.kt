package com.example.broadcast

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.broadcast.ui.theme.ComposeTheme


class MainActivity : ComponentActivity() {

    private val viewModel: BroadcastViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ComposeTheme {
                val events by viewModel.events.collectAsState()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    EventLogScreen(
                        events = events,
                        onDeleteEvent = { event -> viewModel.deleteEvent(event) },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}