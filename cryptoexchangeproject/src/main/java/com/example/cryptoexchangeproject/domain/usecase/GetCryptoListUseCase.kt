package com.example.cryptoexchangeproject.domain.usecase

import com.example.cryptoexchangeproject.domain.model.CryptoCurrency
import com.example.cryptoexchangeproject.domain.repository.CryptoRepository
import kotlinx.coroutines.flow.Flow

class GetCryptoListUseCase(
    private val repository: CryptoRepository
) {
    operator fun invoke(): Flow<List<CryptoCurrency>> = repository.getCryptoList()
}