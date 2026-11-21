package com.example.cryptoexchangeproject.domain.repository

import com.example.cryptoexchangeproject.domain.model.CryptoCurrency
import kotlinx.coroutines.flow.Flow

interface CryptoRepository {
    fun getCryptoList(): Flow<List<CryptoCurrency>>
    suspend fun syncCryptoData(): Result<Unit>
}