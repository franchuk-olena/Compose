package com.example.cryptoexchangeproject.domain.usecase

import com.example.cryptoexchangeproject.domain.repository.CryptoRepository

class SyncCryptoUseCase(
    private val repository: CryptoRepository
) {
    suspend operator fun invoke(): Result<Unit> = repository.syncCryptoData()
}