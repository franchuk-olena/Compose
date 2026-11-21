package com.example.private24

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface PrivatBankApi {

    // GET https://api.privatbank.ua/p24api/exchange_rates?json&date=01.11.2026
    @GET("exchange_rates?json")
    fun getArchiveExchangeRates(
        @Query("date") date: String
    ): Call<ExchangeArchiveResponse>
}