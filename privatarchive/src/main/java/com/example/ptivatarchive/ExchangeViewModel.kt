package com.example.privatarchive

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
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle

sealed interface RequestState {
    data object Idle : RequestState
    data object Loading : RequestState
    data class Success(val responseDate: String, val rates: List<ExchangeRateItem>) : RequestState
    data class Error(val message: String) : RequestState
}

data class UiState(
    val dateInput: String = "",
    val dateError: String? = null,
    val requestState: RequestState = RequestState.Idle,
    val searchQuery: String = "",
    val isSorted: Boolean = false
) {
    val isLoading: Boolean get() = requestState is RequestState.Loading

    val displayedRates: List<ExchangeRateItem>
        get() {
            val rawList = (requestState as? RequestState.Success)?.rates ?: return emptyList()
            val filtered = if (searchQuery.isBlank()) {
                rawList
            } else {
                rawList.filter { it.currency?.contains(searchQuery.trim(), ignoreCase = true) == true }
            }
            return if (isSorted) filtered.sortedBy { it.currency.orEmpty() } else filtered
        }
}

class ExchangeViewModel : ViewModel() {

    private val formatter = DateTimeFormatter.ofPattern("dd.MM.uuuu")
        .withResolverStyle(ResolverStyle.STRICT)

    private val _uiState = MutableStateFlow(
        UiState(dateInput = LocalDate.now().format(formatter))
    )
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var currentCall: Call<ExchangeArchiveResponse>? = null

    fun onDateTextChanged(newDate: String) {
        _uiState.update { it.copy(dateInput = newDate, dateError = null) }
    }

    fun onDatePicked(millis: Long) {
        val pickedDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
        _uiState.update { it.copy(dateInput = pickedDate.format(formatter), dateError = null) }
    }

    fun onSearchChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onToggleSort() {
        _uiState.update { it.copy(isSorted = !it.isSorted) }
    }

    fun loadRates() {
        val rawDate = _uiState.value.dateInput.trim()
        val error = validateDate(rawDate)
        if (error != null) {
            _uiState.update { it.copy(dateError = error) }
            return
        }

        currentCall?.cancel()
        _uiState.update { it.copy(dateError = null, requestState = RequestState.Loading) }

        Log.d("PrivatBankAPI", "Запит курсів на дату: $rawDate")

        val call = RetrofitClient.api.getArchiveRates(rawDate)
        currentCall = call

        call.enqueue(object : Callback<ExchangeArchiveResponse> {
            override fun onResponse(
                call: Call<ExchangeArchiveResponse>,
                response: Response<ExchangeArchiveResponse>
            ) {
                if (call.isCanceled) return

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val validRates = body.exchangeRate?.filter { !it.currency.isNullOrBlank() } ?: emptyList()
                    _uiState.update {
                        it.copy(requestState = RequestState.Success(body.date ?: rawDate, validRates))
                    }
                } else {
                    _uiState.update {
                        it.copy(requestState = RequestState.Error("Помилка сервера (${response.code()}). Спробуйте ще раз."))
                    }
                }
            }

            override fun onFailure(call: Call<ExchangeArchiveResponse>, t: Throwable) {
                if (call.isCanceled) return
                _uiState.update {
                    it.copy(requestState = RequestState.Error("Не вдалося завантажити курси. Перевірте з'єднання та спробуйте знову."))
                }
            }
        })
    }

    private fun validateDate(dateStr: String): String? {
        if (dateStr.isBlank()) return "Введіть дату у форматі дд.мм.рррр"
        val parsed = try {
            LocalDate.parse(dateStr, formatter)
        } catch (e: DateTimeParseException) {
            return "Неіснуюча або неправильна дата (потрібно дд.мм.рррр)"
        }
        val today = LocalDate.now()
        return when {
            parsed.isAfter(today) -> "Не можна обрати майбутню дату"
            parsed.isBefore(today.minusYears(4)) -> "Архів доступний лише за останні 4 роки"
            else -> null
        }
    }

    override fun onCleared() {
        super.onCleared()
        currentCall?.cancel()
    }
}