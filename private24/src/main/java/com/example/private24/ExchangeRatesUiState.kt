package com.example.private24

import java.time.LocalDate
import java.time.format.DateTimeFormatter

sealed interface ArchiveRequestState {
    data object Idle : ArchiveRequestState
    data object Loading : ArchiveRequestState
    data class Success(
        val responseDate: String,
        val allRates: List<CurrencyItem>,
        val displayedRates: List<CurrencyItem>
    ) : ArchiveRequestState
    data class Error(val message: String) : ArchiveRequestState
}

data class ExchangeRatesUiState(
    val selectedDate: String = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
    val dateError: String? = null,
    val searchQuery: String = "",
    val isSortedAlphabetically: Boolean = false,
    val requestState: ArchiveRequestState = ArchiveRequestState.Idle
) {
    val isLoading: Boolean
        get() = requestState is ArchiveRequestState.Loading
}