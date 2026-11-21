package com.example.cryptoexchangeproject.presentation.model

import com.example.cryptoexchangeproject.domain.model.CryptoCurrency

data class CryptoUiState(
    val cryptoList: List<CryptoCurrency> = emptyList(),
    val isRefreshing: Boolean = false,
    val lastUpdatedText: String? = null,
    val errorMessage: String? = null
)