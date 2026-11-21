package com.example.cryptoexchangeproject.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "crypto_currencies")
data class CryptoEntity(
    @PrimaryKey val id: String,
    val rank: Int,
    val name: String,
    val symbol: String,
    val logoUrl: String,
    val priceUsd: Double?,
    val lastUpdatedTimestamp: Long
)