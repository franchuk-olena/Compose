package com.example.private24

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.private24.ui.theme.ComposeTheme
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val viewModel: PrivatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ComposeTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                CurrencyRatesScreen(
                    uiState = uiState,
                    onDateChanged = viewModel::onDateChanged,
                    onDatePicked = viewModel::onDatePicked,
                    onLoadRates = viewModel::getCurrencyExchange,
                    onSearchQueryChanged = viewModel::onSearchQueryChanged,
                    onToggleSort = viewModel::onToggleSort
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyRatesScreen(
    modifier: Modifier = Modifier,
    uiState: ExchangeRatesUiState = ExchangeRatesUiState(),
    onDateChanged: (String) -> Unit = {},
    onDatePicked: (Long?) -> Unit = {},
    onLoadRates: () -> Unit = {},
    onSearchQueryChanged: (String) -> Unit = {},
    onToggleSort: () -> Unit = {}
) {
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDatePicked(datePickerState.selectedDateMillis)
                        showDatePicker = false
                    }
                ) {
                    Text("Обрати")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Скасувати")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ПриватБанк — валюти") }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = uiState.selectedDate,
                onValueChange = onDateChanged,
                label = { Text("Дата (dd.MM.yyyy)") },
                placeholder = { Text("01.11.2026") },
                isError = uiState.dateError != null,
                supportingText = uiState.dateError?.let { errorMsg -> { Text(errorMsg) } },
                enabled = !uiState.isLoading,
                singleLine = true,
                trailingIcon = {
                    IconButton(
                        onClick = { showDatePicker = true },
                        enabled = !uiState.isLoading
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Обрати дату"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = onLoadRates,
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Показати курси")
            }

            when (val state = uiState.requestState) {
                is ArchiveRequestState.Idle -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Оберіть дату та натисніть «Показати курси»",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                is ArchiveRequestState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is ArchiveRequestState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(onClick = onLoadRates) {
                            Text("Повторити запит")
                        }
                    }
                }

                is ArchiveRequestState.Success -> {
                    if (state.allRates.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("На дату ${state.responseDate} дані про курси відсутні.")
                        }
                    } else {
                        Text(
                            text = "Курси на ${state.responseDate}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = uiState.searchQuery,
                                onValueChange = onSearchQueryChanged,
                                label = { Text("Пошук валюти") },
                                leadingIcon = {
                                    Icon(Icons.Default.Search, contentDescription = null)
                                },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )

                            FilterChip(
                                selected = uiState.isSortedAlphabetically,
                                onClick = onToggleSort,
                                label = { Text("А–Я") }
                            )
                        }

                        if (state.displayedRates.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Валют за запитом «${uiState.searchQuery}» не знайдено.")
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(bottom = 16.dp)
                            ) {
                                items(
                                    items = state.displayedRates,
                                    key = { "${it.currency}_${it.baseCurrency}" }
                                ) { item ->
                                    CurrencyRateCard(item = item)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CurrencyRateCard(item: CurrencyItem) {
    val base = item.baseCurrency ?: "UAH"
    val buyText = item.purchaseRate?.let { String.format(Locale.US, "%.2f %s", it, base) } ?: "—"
    val saleText = item.saleRate?.let { String.format(Locale.US, "%.2f %s", it, base) } ?: "—"

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = item.currency.orEmpty(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Базова валюта: $base",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Банк купує",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = buyText,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Банк продає",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = saleText,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CurrencyRatesPreview() {
    ComposeTheme {
        CurrencyRatesScreen(
            uiState = ExchangeRatesUiState(
                selectedDate = "01.11.2026",
                requestState = ArchiveRequestState.Success(
                    responseDate = "01.11.2026",
                    allRates = listOf(
                        CurrencyItem("UAH", "USD", 41.2, 41.2, 41.6, 41.1),
                        CurrencyItem("UAH", "EUR", 44.5, 44.5, null, null)
                    ),
                    displayedRates = listOf(
                        CurrencyItem("UAH", "USD", 41.2, 41.2, 41.6, 41.1),
                        CurrencyItem("UAH", "EUR", 44.5, 44.5, null, null)
                    )
                )
            )
        )
    }
}