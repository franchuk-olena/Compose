package com.example.cryptoexchange

import android.app.Application
import com.example.cryptoexchange.di.CryptoAppContainer
import com.example.cryptoexchange.worker.WorkManagerScheduler

class CryptoApplication : Application() {

    lateinit var container: CryptoAppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = CryptoAppContainer(this)
        WorkManagerScheduler.schedulePeriodicSync(this)
    }
}
