package com.example.cryptoexchange.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CoinDeskResponseDto(
    @SerializedName("Data")
    val data: CoinDeskDataContainerDto?
)

data class CoinDeskDataContainerDto(
    @SerializedName("LIST")
    val list: List<CryptoDto>?
)
