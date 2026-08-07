package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.domain.model.CurrencyConversionResult
import com.mgacreative.touros.domain.usecase.ConvertCurrencyUseCase
import com.mgacreative.touros.domain.usecase.GetCurrentUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CurrencyConverterUiState(
    val amount: Double = 1000.0,
    val fromCurrency: String = "TRY",
    val toCurrency: String = "EUR",
    val supportedCurrencies: List<String> = listOf("TRY", "EUR", "USD", "GBP", "AED", "RUB"),
    val conversionResult: CurrencyConversionResult = CurrencyConversionResult(),
    val isLoading: Boolean = false,
    val notificationMessage: String? = null,
    val errorMessage: String? = null
)

class CurrencyConverterViewModel(
    private val convertCurrencyUseCase: ConvertCurrencyUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CurrencyConverterUiState())
    val uiState: StateFlow<CurrencyConverterUiState> = _uiState.asStateFlow()

    init {
        convert()
    }

    fun updateAmount(amount: Double) {
        _uiState.value = _uiState.value.copy(amount = amount)
        convert()
    }

    fun updateFromCurrency(code: String) {
        _uiState.value = _uiState.value.copy(fromCurrency = code)
        convert()
    }

    fun updateToCurrency(code: String) {
        _uiState.value = _uiState.value.copy(toCurrency = code)
        convert()
    }

    fun convert() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val user = getCurrentUserUseCase()
            val tenantId = user?.tenantId ?: "tenant_id"

            val res = convertCurrencyUseCase(
                amount = _uiState.value.amount,
                fromCurrency = _uiState.value.fromCurrency,
                toCurrency = _uiState.value.toCurrency,
                tenantId = tenantId
            )

            res.onSuccess { result ->
                _uiState.value = _uiState.value.copy(
                    conversionResult = result,
                    isLoading = false,
                    notificationMessage = "💱 KMP CurrencyFormatter İle Anlık Çeviri Yapıldı!"
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = err.message
                )
            }
        }
    }
}
