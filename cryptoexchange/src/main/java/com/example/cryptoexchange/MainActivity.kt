package com.example.cryptoexchange

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.cryptoexchange.presentation.ui.CryptoListScreen
import com.example.cryptoexchange.presentation.viewmodel.CryptoViewModel
import com.example.cryptoexchange.ui.theme.ComposeTheme

class MainActivity : ComponentActivity() {

    private val viewModel: CryptoViewModel by viewModels {
        val app = application as CryptoApplication
        CryptoViewModel.Factory(
            app.container.getCryptoListUseCase,
            app.container.syncCryptoUseCase
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ComposeTheme {
                CryptoListScreen(viewModel = viewModel)
            }
        }
    }
}
