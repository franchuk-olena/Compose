package com.example.cryptoexchangeproject

import android.app.Application
import com.example.cryptoexchangeproject.di.CryptoAppContainer
import com.example.cryptoexchangeproject.worker.WorkManagerScheduler

class CryptoApplication : Application() {

    lateinit var container: CryptoAppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = CryptoAppContainer(this)
        WorkManagerScheduler.schedulePeriodicSync(this)
    }
}