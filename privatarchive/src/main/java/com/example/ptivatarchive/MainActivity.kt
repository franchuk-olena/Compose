package com.example.privatarchive

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.activity.viewModels
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PrivatScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivatScreen(viewModel: ExchangeViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showCalendar by remember { mutableStateOf(false) }

    if (showCalendar) {
        val todayMillis = remember { LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli() }
        val minMillis = remember { LocalDate.now().minusYears(4).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli() }
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = todayMillis,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis in minMillis..todayMillis
            }
        )

        DatePickerDialog(
            onDismissRequest = { showCalendar = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { viewModel.onDatePicked(it) }
                    showCalendar = false
                }) { Text("Обрати") }
            },
            dismissButton = {
                TextButton(onClick = { showCalendar = false }) { Text("Скасувати") }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("ПриватБанк — валюти") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = state.dateInput,
                onValueChange = viewModel::onDateTextChanged,
                label = { Text("Дата (дд.мм.рррр)") },
                singleLine = true,
                enabled = !state.isLoading,
                isError = state.dateError != null,
                supportingText = state.dateError?.let { { Text(it) } },
                trailingIcon = {
                    IconButton(onClick = { showCalendar = true }, enabled = !state.isLoading) {
                        Icon(Icons.Default.DateRange, contentDescription = "Календар")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = viewModel::loadRates,
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Показати курси")
            }

            when (val req = state.requestState) {
                is RequestState.Idle -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Оберіть дату та натисніть «Показати курси»", textAlign = TextAlign.Center)
                    }
                }
                is RequestState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is RequestState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(req.message, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                            Spacer(Modifier.height(8.dp))
                            OutlinedButton(onClick = viewModel::loadRates) { Text("Повторити") }
                        }
                    }
                }
                is RequestState.Success -> {
                    Text(
                        text = "Курси на дату: ${req.responseDate}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    if (req.rates.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("На цю дату даних немає.")
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = state.searchQuery,
                                onValueChange = viewModel::onSearchChanged,
                                placeholder = { Text("Пошук валюти...") },
                                leadingIcon = { Icon(Icons.Default.Search, null) },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = state.isSorted,
                                onClick = viewModel::onToggleSort,
                                label = { Text("А–Я") }
                            )
                        }

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(state.displayedRates, key = { it.currency ?: it.hashCode() }) { item ->
                                CurrencyCard(item)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CurrencyCard(item: ExchangeRateItem) {
    val buy = item.purchaseRate?.let { String.format(Locale.US, "%.2f", it) } ?: "—"
    val sell = item.saleRate?.let { String.format(Locale.US, "%.2f", it) } ?: "—"
    val base = item.baseCurrency ?: "UAH"

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(item.currency.orEmpty(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("До валюти: $base", style = MaterialTheme.typography.bodySmall)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(horizontalAlignment = Alignment.End) {
                    Text("Банк купує", style = MaterialTheme.typography.labelSmall)
                    Text(buy, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Банк продає", style = MaterialTheme.typography.labelSmall)
                    Text(sell, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}