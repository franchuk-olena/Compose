package com.example.cryptoexchangeproject.data.repository

import com.example.cryptoexchangeproject.BuildConfig
import com.example.cryptoexchangeproject.data.local.CryptoDao
import com.example.cryptoexchangeproject.data.local.CryptoEntity
import com.example.cryptoexchangeproject.data.remote.CoinDeskApi
import com.example.cryptoexchangeproject.domain.model.CryptoCurrency
import com.example.cryptoexchangeproject.domain.repository.CryptoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CryptoRepositoryImpl(
    private val api: CoinDeskApi,
    private val dao: CryptoDao
) : CryptoRepository {

    override fun getCryptoList(): Flow<List<CryptoCurrency>> {
        return dao.getTopCryptoList().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun syncCryptoData(): Result<Unit> {
        return try {
            val rawApiKey = BuildConfig.COINDESK_API_KEY
            val authHeader = when {
                rawApiKey.isBlank() || rawApiKey == "YOUR_API_KEY" -> null
                rawApiKey.startsWith("Apikey ") -> rawApiKey
                else -> "Apikey $rawApiKey"
            }

            val response = api.getTopList(apiKey = authHeader)
            val dtoList = response.data?.list

            if (dtoList.isNullOrEmpty()) {
                return Result.failure(IllegalStateException("Порожня відповідь від API"))
            }

            val currentTime = System.currentTimeMillis()
            val entities = dtoList.take(30).mapIndexedNotNull { index, dto ->
                val id = dto.id ?: dto.symbol ?: return@mapIndexedNotNull null
                val name = dto.name ?: "Unknown"
                val symbol = dto.symbol ?: ""
                val logoUrl = dto.logoUrl ?: ""
                val price = dto.priceUsd ?: dto.price

                CryptoEntity(
                    id = id,
                    rank = index + 1,
                    name = name,
                    symbol = symbol,
                    logoUrl = logoUrl,
                    priceUsd = price,
                    lastUpdatedTimestamp = currentTime
                )
            }

            if (entities.isEmpty()) {
                return Result.failure(IllegalStateException("Не вдалося розпарсити валюти"))
            }

            dao.updateTop30(entities)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun CryptoEntity.toDomain(): CryptoCurrency {
        return CryptoCurrency(
            id = id,
            rank = rank,
            name = name,
            symbol = symbol,
            logoUrl = logoUrl,
            priceUsd = priceUsd,
            lastUpdatedTimestamp = lastUpdatedTimestamp
        )
    }
}
