package com.example.cryptoexchangeproject.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CryptoDto(
    @SerializedName("id")
    val id: String?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("symbol")
    val symbol: String?,
    @SerializedName("image")
    val logoUrl: String?,
    @SerializedName("current_price")
    val priceUsd: Double?,
    @SerializedName("market_cap_rank")
    val rank: Int?
)
