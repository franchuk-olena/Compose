package com.example.cryptoexchange.domain.usecase

import com.example.cryptoexchange.domain.repository.CryptoRepository

class SyncCryptoUseCase(
    private val repository: CryptoRepository
) {
    suspend operator fun invoke(): Result<Unit> = repository.syncCryptoData()
}
