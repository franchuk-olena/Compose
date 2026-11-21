package com.example.private24

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle

class PrivatViewModel : ViewModel() {

    private val TAG = "PrivatViewModel"

    private val networkModule = PrivatBankNetworkModule()

    private val _uiState = MutableStateFlow(ExchangeRatesUiState())
    val uiState: StateFlow<ExchangeRatesUiState> = _uiState.asStateFlow()

    private var currentCall: Call<ExchangeArchiveResponse>? = null

    private val strictFormatter: DateTimeFormatter = DateTimeFormatter
        .ofPattern("dd.MM.uuuu")
        .withResolverStyle(ResolverStyle.STRICT)

    fun onDateChanged(newDate: String) {
        _uiState.update {
            it.copy(
                selectedDate = newDate,
                dateError = null
            )
        }
    }

    fun onDatePicked(millis: Long?) {
        if (millis == null) return
        val pickedDate = Instant.ofEpochMilli(millis)
            .atZone(ZoneOffset.UTC)
            .toLocalDate()
        val formatted = pickedDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        onDateChanged(formatted)
    }

    // Додаткове завдання (*): пошук за кодом валюти без мережевого запиту
    fun onSearchQueryChanged(query: String) {
        _uiState.update { currentState ->
            currentState.copy(
                searchQuery = query,
                requestState = applyFilterAndSort(
                    state = currentState.requestState,
                    query = query,
                    sortAlphabetically = currentState.isSortedAlphabetically
                )
            )
        }
    }

    // Додаткове завдання (*): сортування списку за абеткою без мережевого запиту
    fun onToggleSort() {
        _uiState.update { currentState ->
            val newSort = !currentState.isSortedAlphabetically
            currentState.copy(
                isSortedAlphabetically = newSort,
                requestState = applyFilterAndSort(
                    state = currentState.requestState,
                    query = currentState.searchQuery,
                    sortAlphabetically = newSort
                )
            )
        }
    }

    private fun applyFilterAndSort(
        state: ArchiveRequestState,
        query: String,
        sortAlphabetically: Boolean
    ): ArchiveRequestState {
        if (state !is ArchiveRequestState.Success) return state

        val filtered = if (query.isBlank()) {
            state.allRates
        } else {
            state.allRates.filter {
                it.currency?.contains(query.trim(), ignoreCase = true) == true
            }
        }

        val sorted = if (sortAlphabetically) {
            filtered.sortedBy { it.currency.orEmpty() }
        } else {
            filtered
        }

        return state.copy(displayedRates = sorted)
    }

    fun getCurrencyExchange() {
        val rawDate = _uiState.value.selectedDate.trim()

        val validationMessage = validateDate(rawDate)
        if (validationMessage != null) {
            _uiState.update { it.copy(dateError = validationMessage) }
            return
        }

        currentCall?.cancel()

        _uiState.update {
            it.copy(
                dateError = null,
                requestState = ArchiveRequestState.Loading
            )
        }

        Log.d(TAG, "Старт запиту до API ПриватБанку за дату: $rawDate")

        val call = networkModule.service.getArchiveExchangeRates(date = rawDate)
        currentCall = call

        call.enqueue(object : Callback<ExchangeArchiveResponse> {
            override fun onResponse(
                call: Call<ExchangeArchiveResponse>,
                response: Response<ExchangeArchiveResponse>
            ) {
                currentCall = null

                if (!response.isSuccessful) {
                    _uiState.update {
                        it.copy(
                            requestState = ArchiveRequestState.Error(
                                "Помилка сервера (HTTP ${response.code()}). Спробуйте знову."
                            )
                        )
                    }
                    return
                }

                val body = response.body()
                if (body == null) {
                    _uiState.update {
                        it.copy(
                            requestState = ArchiveRequestState.Error(
                                "Сервер повернув порожню відповідь."
                            )
                        )
                    }
                    return
                }

                // Відкидаємо записи без коду валюти
                val validRates = body.exchangeRate
                    .orEmpty()
                    .filter { !it.currency.isNullOrBlank() }

                val responseDate = body.date ?: rawDate

                _uiState.update { currentState ->
                    val successState = ArchiveRequestState.Success(
                        responseDate = responseDate,
                        allRates = validRates,
                        displayedRates = validRates
                    )
                    currentState.copy(
                        requestState = applyFilterAndSort(
                            state = successState,
                            query = currentState.searchQuery,
                            sortAlphabetically = currentState.isSortedAlphabetically
                        )
                    )
                }
            }

            override fun onFailure(call: Call<ExchangeArchiveResponse>, t: Throwable) {
                if (call.isCanceled) return
                currentCall = null
                _uiState.update {
                    it.copy(
                        requestState = ArchiveRequestState.Error(
                            "Не вдалося завантажити курси. Перевірте з'єднання та спробуйте знову."
                        )
                    )
                }
            }
        })
    }

    private fun validateDate(dateStr: String): String? {
        if (dateStr.isBlank()) {
            return "Введіть дату у форматі dd.MM.yyyy"
        }
        val parsed = try {
            LocalDate.parse(dateStr, strictFormatter)
        } catch (e: DateTimeParseException) {
            return "Некоректна або неіснуюча дата (формат: dd.MM.yyyy)"
        }

        val today = LocalDate.now()
        val fourYearsAgo = today.minusYears(4)

        return when {
            parsed.isAfter(today) -> "Не можна запитувати майбутню дату"
            parsed.isBefore(fourYearsAgo) -> "Архів охоплює лише останні 4 роки"
            else -> null
        }
    }

    override fun onCleared() {
        super.onCleared()
        currentCall?.cancel()
    }
}