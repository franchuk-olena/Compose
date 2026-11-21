package com.example.cryptoexchangeproject.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CryptoDto(
    @SerializedName("ID")
    val id: String?,
    @SerializedName("NAME")
    val name: String?,
    @SerializedName("SYMBOL")
    val symbol: String?,
    @SerializedName("LOGO_URL")
    val logoUrl: String?,
    @SerializedName("PRICE_USD")
    val priceUsd: Double?,
    @SerializedName("PRICE")
    val price: Double?
)
