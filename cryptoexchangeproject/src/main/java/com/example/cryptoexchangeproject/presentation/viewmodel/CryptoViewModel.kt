package com.example.cryptoexchangeproject.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.cryptoexchangeproject.domain.usecase.GetCryptoListUseCase
import com.example.cryptoexchangeproject.domain.usecase.SyncCryptoUseCase
import com.example.cryptoexchangeproject.presentation.model.CryptoUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CryptoViewModel(
    private val getCryptoListUseCase: GetCryptoListUseCase,
    private val syncCryptoUseCase: SyncCryptoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CryptoUiState())
    val uiState: StateFlow<CryptoUiState> = _uiState.asStateFlow()

    init {
        observeCryptoList()
    }

    private fun observeCryptoList() {
        viewModelScope.launch {
            getCryptoListUseCase().collect { list ->
                val lastUpdatedTime = list.firstOrNull()?.lastUpdatedTimestamp
                val formattedTime = lastUpdatedTime?.let { formatTimestamp(it) }

                _uiState.update { currentState ->
                    currentState.copy(
                        cryptoList = list,
                        lastUpdatedText = formattedTime
                    )
                }

                if (list.isEmpty() && !_uiState.value.isRefreshing) {
                    performInitialSync()
                }
            }
        }
    }

    private fun performInitialSync() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
            val result = syncCryptoUseCase()
            _uiState.update { currentState ->
                if (result.isSuccess) {
                    currentState.copy(isRefreshing = false)
                } else {
                    val errorMsg = result.exceptionOrNull()?.localizedMessage ?: "Помилка завантаження"
                    currentState.copy(isRefreshing = false, errorMessage = errorMsg)
                }
            }
        }
    }

    fun refreshData() {
        _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
        viewModelScope.launch {
            val result = syncCryptoUseCase()
            _uiState.update { currentState ->
                if (result.isSuccess) {
                    currentState.copy(isRefreshing = false)
                } else {
                    val errorMsg = result.exceptionOrNull()?.localizedMessage ?: "Помилка оновлення даних"
                    currentState.copy(isRefreshing = false, errorMessage = errorMsg)
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun formatTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    class Factory(
        private val getCryptoListUseCase: GetCryptoListUseCase,
        private val syncCryptoUseCase: SyncCryptoUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CryptoViewModel(getCryptoListUseCase, syncCryptoUseCase) as T
        }
    }
}
