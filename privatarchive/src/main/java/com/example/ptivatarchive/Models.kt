package com.example.privatarchive

import com.google.gson.annotations.SerializedName

// Це головна відповідь від ПриватБанку
data class ExchangeArchiveResponse(
    @SerializedName("date")
    val date: String? = null,
    @SerializedName("exchangeRate")
    val exchangeRate: List<ExchangeRateItem>? = null
)

// Це одна конкретна валюта зі списку
data class ExchangeRateItem(
    @SerializedName("baseCurrency")
    val baseCurrency: String? = null,
    @SerializedName("currency")
    val currency: String? = null,
    @SerializedName("saleRate")
    val saleRate: Double? = null,
    @SerializedName("purchaseRate")
    val purchaseRate: Double? = null
)