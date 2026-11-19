package com.example.broadcast

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BroadcastViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BroadcastRepository(application)

    private val _events = MutableStateFlow<List<BroadcastEvent>>(emptyList())
    val events: StateFlow<List<BroadcastEvent>> = _events.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getBroadcastEvents().collect { newEvent ->
                // Додаємо нову подію на початок списку
                _events.update { currentList ->
                    listOf(newEvent) + currentList
                }
            }
        }
    }

    fun deleteEvent(event: BroadcastEvent) {
        _events.update { currentList ->
            currentList.filter { it.id != event.id }
        }
    }

    fun clearAll() {
        _events.value = emptyList()
    }
}