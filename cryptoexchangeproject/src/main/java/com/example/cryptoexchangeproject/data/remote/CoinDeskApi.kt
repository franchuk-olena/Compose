package com.example.cryptoexchangeproject.data.remote

import com.example.cryptoexchangeproject.data.remote.dto.CryptoDto
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface CoinDeskApi {

    @GET("coins/markets")
    suspend fun getTopList(
        @Header("x-cg-demo-api-key") apiKey: String? = null,
        @Query("vs_currency") vsCurrency: String = "usd",
        @Query("order") order: String = "market_cap_desc",
        @Query("per_page") pageSize: Int = 30,
        @Query("page") page: Int = 1,
        @Query("sparkline") sparkline: Boolean = false
    ): List<CryptoDto>

    companion object {
        const val BASE_URL = "https://api.coingecko.com/api/v3/"
    }
}