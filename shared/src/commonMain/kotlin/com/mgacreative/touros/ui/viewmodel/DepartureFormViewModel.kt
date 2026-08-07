package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.domain.model.Departure
import com.mgacreative.touros.domain.repository.DashboardRepository
import com.mgacreative.touros.domain.usecase.GetCurrentUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DepartureFormUiState(
    val tourId: String = "",
    val tourTitle: String = "Kapadokya Balon & Vadi Turu",
    val departureDate: String = "2026-08-14",
    val returnDate: String = "2026-08-17",
    val priceOverride: String = "4500",
    val childPriceOverride: String = "3000",
    val infantPriceOverride: String = "1000",
    val currency: String = "TRY",
    val capacity: String = "30",
    val optionDeadlineDays: String = "7",
    val isGuaranteed: Boolean = true,
    val isRecurring: Boolean = false,
    val selectedDayOfWeek: Int = 5, // 5 = Cuma
    val recurrenceEndDate: String = "2026-10-31",
    val isLoading: Boolean = false,
    val isSavedSuccess: Boolean = false,
    val generatedCount: Int = 0,
    val errorMessage: String? = null
)

class DepartureFormViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DepartureFormUiState())
    val uiState: StateFlow<DepartureFormUiState> = _uiState.asStateFlow()

    fun updateTourId(tourId: String) { _uiState.value = _uiState.value.copy(tourId = tourId) }
    fun updateDepartureDate(date: String) { _uiState.value = _uiState.value.copy(departureDate = date) }
    fun updateReturnDate(date: String) { _uiState.value = _uiState.value.copy(returnDate = date) }
    fun updatePriceOverride(price: String) { _uiState.value = _uiState.value.copy(priceOverride = price) }
    fun updateChildPriceOverride(price: String) { _uiState.value = _uiState.value.copy(childPriceOverride = price) }
    fun updateInfantPriceOverride(price: String) { _uiState.value = _uiState.value.copy(infantPriceOverride = price) }
    fun updateCapacity(cap: String) { _uiState.value = _uiState.value.copy(capacity = cap) }
    fun updateIsGuaranteed(isGuaranteed: Boolean) { _uiState.value = _uiState.value.copy(isGuaranteed = isGuaranteed) }
    fun updateIsRecurring(isRecurring: Boolean) { _uiState.value = _uiState.value.copy(isRecurring = isRecurring) }
    fun updateSelectedDayOfWeek(day: Int) { _uiState.value = _uiState.value.copy(selectedDayOfWeek = day) }
    fun updateRecurrenceEndDate(date: String) { _uiState.value = _uiState.value.copy(recurrenceEndDate = date) }

    fun saveDeparture() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val currentState = _uiState.value

            if (currentState.isRecurring) {
                // Bulk Generation (Tekrarlayan Seferler)
                val count = 12 // Örn: 12 adet cuma seferi oluşturuldu
                _uiState.value = currentState.copy(
                    isLoading = false,
                    isSavedSuccess = true,
                    generatedCount = count
                )
            } else {
                // Tek Sefer Oluşturma
                _uiState.value = currentState.copy(
                    isLoading = false,
                    isSavedSuccess = true,
                    generatedCount = 1
                )
            }
        }
    }
}
