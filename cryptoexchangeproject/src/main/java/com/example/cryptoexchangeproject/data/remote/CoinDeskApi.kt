package com.example.cryptoexchangeproject.data.remote

import com.example.cryptoexchangeproject.data.remote.dto.CoinDeskResponseDto
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface CoinDeskApi {

    @GET("asset/v1/top/list")
    suspend fun getTopList(
        @Header("Authorization") apiKey: String,
        @Query("page_size") pageSize: Int = 30,
        @Query("sort_by") sortBy: String = "MARKET_CAP",
        @Query("sort_direction") sortDirection: String = "DESC"
    ): CoinDeskResponseDto

    companion object {
        const val BASE_URL = "https://data-api.coindesk.com/"
    }
}