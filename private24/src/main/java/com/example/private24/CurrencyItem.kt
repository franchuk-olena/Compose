package com.example.private24

import com.google.gson.annotations.SerializedName

data class ExchangeArchiveResponse(
    @SerializedName("date")
    val date: String?,

    @SerializedName("bank")
    val bank: String?,

    @SerializedName("baseCurrency")
    val baseCurrency: Int?,

    @SerializedName("baseCurrencyLit")
    val baseCurrencyLit: String?,

    @SerializedName("exchangeRate")
    val exchangeRate: List<CurrencyItem>?
)

data class CurrencyItem(
    @SerializedName("baseCurrency")
    val baseCurrency: String?,

    @SerializedName("currency")
    val currency: String?,

    @SerializedName("saleRateNB")
    val saleRateNB: Double?,

    @SerializedName("purchaseRateNB")
    val purchaseRateNB: Double?,

    @SerializedName("saleRate")
    val saleRate: Double?,

    @SerializedName("purchaseRate")
    val purchaseRate: Double?
)