package com.example.cryptoexchange.di

import android.content.Context
import com.example.cryptoexchange.data.local.AppDatabase
import com.example.cryptoexchange.data.remote.CoinDeskApi
import com.example.cryptoexchange.data.repository.CryptoRepositoryImpl
import com.example.cryptoexchange.domain.repository.CryptoRepository
import com.example.cryptoexchange.domain.usecase.GetCryptoListUseCase
import com.example.cryptoexchange.domain.usecase.SyncCryptoUseCase
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CryptoAppContainer(context: Context) {

    private val database = AppDatabase.getInstance(context)

    private val retrofit = Retrofit.Builder()
        .baseUrl(CoinDeskApi.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api: CoinDeskApi = retrofit.create(CoinDeskApi::class.java)

    val repository: CryptoRepository by lazy {
        CryptoRepositoryImpl(api, database.cryptoDao())
    }

    val getCryptoListUseCase: GetCryptoListUseCase by lazy {
        GetCryptoListUseCase(repository)
    }

    val syncCryptoUseCase: SyncCryptoUseCase by lazy {
        SyncCryptoUseCase(repository)
    }
}
