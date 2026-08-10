package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.data.database.entity.DepartureEntity
import com.mgacreative.touros.data.util.generateUuid
import com.mgacreative.touros.data.util.isValidUuid
import com.mgacreative.touros.domain.usecase.GetCurrentUserUseCase
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DepartureFormUiState(
    val departureId: String? = null,
    val tourId: String = "",
    val tourTitle: String = "Tur Kalkış Seferi",
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
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val supabaseClient: SupabaseClient
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

    fun loadDeparture(departureId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            runCatching {
                supabaseClient.postgrest.from("departures")
                    .select {
                        filter {
                            eq("id", departureId)
                        }
                    }
                    .decodeSingle<DepartureEntity>()
            }.onSuccess { entity ->
                _uiState.value = _uiState.value.copy(
                    departureId = entity.id,
                    tourId = entity.tourId,
                    departureDate = entity.departureDate,
                    returnDate = entity.returnDate ?: "",
                    priceOverride = entity.priceOverride?.toString() ?: "",
                    childPriceOverride = entity.childPriceOverride?.toString() ?: "",
                    infantPriceOverride = entity.infantPriceOverride?.toString() ?: "",
                    currency = entity.currency,
                    capacity = entity.capacity?.toString() ?: "30",
                    optionDeadlineDays = entity.optionDeadlineDays?.toString() ?: "7",
                    isGuaranteed = entity.isGuaranteed,
                    isLoading = false
                )
            }.onFailure { ex ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = ex.message ?: "Kalkış bilgileri yüklenemedi"
                )
            }
        }
    }

    fun saveDeparture() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val currentState = _uiState.value

            val currentUser = getCurrentUserUseCase()
            val tenantId = currentUser?.tenantId?.takeIf { it.isValidUuid() } ?: "00000000-0000-0000-0000-000000000001"
            val tourId = currentState.tourId.takeIf { it.isValidUuid() } ?: "00000000-0000-0000-0000-000000000001"

            val result = runCatching {
                if (currentState.departureId != null && currentState.departureId.isValidUuid()) {
                    // Tekli Güncelleme
                    val entity = DepartureEntity(
                        id = currentState.departureId,
                        tourId = tourId,
                        departureDate = currentState.departureDate.trim(),
                        returnDate = currentState.returnDate.trim().ifBlank { null },
                        priceOverride = currentState.priceOverride.toDoubleOrNull(),
                        childPriceOverride = currentState.childPriceOverride.toDoubleOrNull(),
                        infantPriceOverride = currentState.infantPriceOverride.toDoubleOrNull(),
                        currency = currentState.currency,
                        capacity = currentState.capacity.toIntOrNull() ?: 30,
                        optionDeadlineDays = currentState.optionDeadlineDays.toIntOrNull() ?: 7,
                        isGuaranteed = currentState.isGuaranteed,
                        tenantId = tenantId
                    )
                    supabaseClient.postgrest.from("departures").upsert(entity)
                    1
                } else if (currentState.isRecurring) {
                    // Toplu / Tekrarlayan Tarih Üretimi
                    val startDepDate = currentState.departureDate.trim()
                    val endRecDate = currentState.recurrenceEndDate.trim().ifBlank { addDaysToIsoDate(startDepDate, 60) }
                    val durationDays = calculateDurationDays(startDepDate, currentState.returnDate.trim())

                    val entities = mutableListOf<DepartureEntity>()
                    var currentDep = startDepDate
                    val maxItems = 52

                    while (currentDep <= endRecDate && entities.size < maxItems) {
                        val currentRet = addDaysToIsoDate(currentDep, durationDays)
                        entities.add(
                            DepartureEntity(
                                id = generateUuid(),
                                tourId = tourId,
                                departureDate = currentDep,
                                returnDate = currentRet,
                                priceOverride = currentState.priceOverride.toDoubleOrNull(),
                                childPriceOverride = currentState.childPriceOverride.toDoubleOrNull(),
                                infantPriceOverride = currentState.infantPriceOverride.toDoubleOrNull(),
                                currency = currentState.currency,
                                capacity = currentState.capacity.toIntOrNull() ?: 30,
                                optionDeadlineDays = currentState.optionDeadlineDays.toIntOrNull() ?: 7,
                                isGuaranteed = currentState.isGuaranteed,
                                tenantId = tenantId
                            )
                        )
                        currentDep = addDaysToIsoDate(currentDep, 7) // Haftalık tekrar
                    }

                    if (entities.isNotEmpty()) {
                        supabaseClient.postgrest.from("departures").insert(entities)
                    }
                    entities.size
                } else {
                    // Tekli Yeni Kalkış Ekleme
                    val entity = DepartureEntity(
                        id = generateUuid(),
                        tourId = tourId,
                        departureDate = currentState.departureDate.trim(),
                        returnDate = currentState.returnDate.trim().ifBlank { null },
                        priceOverride = currentState.priceOverride.toDoubleOrNull(),
                        childPriceOverride = currentState.childPriceOverride.toDoubleOrNull(),
                        infantPriceOverride = currentState.infantPriceOverride.toDoubleOrNull(),
                        currency = currentState.currency,
                        capacity = currentState.capacity.toIntOrNull() ?: 30,
                        optionDeadlineDays = currentState.optionDeadlineDays.toIntOrNull() ?: 7,
                        isGuaranteed = currentState.isGuaranteed,
                        tenantId = tenantId
                    )
                    supabaseClient.postgrest.from("departures").insert(entity)
                    1
                }
            }

            result.onSuccess { count ->
                _uiState.value = currentState.copy(
                    isLoading = false,
                    isSavedSuccess = true,
                    generatedCount = count
                )
            }.onFailure { ex ->
                _uiState.value = currentState.copy(
                    isLoading = false,
                    isSavedSuccess = false,
                    errorMessage = ex.message ?: "Kalkış tarihi kaydedilirken hata oluştu"
                )
            }
        }
    }

    private fun addDaysToIsoDate(isoDate: String, days: Int): String {
        val parts = isoDate.split("-")
        if (parts.size != 3) return isoDate
        var year = parts[0].toIntOrNull() ?: return isoDate
        var month = parts[1].toIntOrNull() ?: return isoDate
        var day = parts[2].toIntOrNull() ?: return isoDate

        day += days

        fun daysInMonth(y: Int, m: Int): Int = when (m) {
            1, 3, 5, 7, 8, 10, 12 -> 31
            4, 6, 9, 11 -> 30
            2 -> if ((y % 4 == 0 && y % 100 != 0) || (y % 400 == 0)) 29 else 28
            else -> 30
        }

        while (day > daysInMonth(year, month)) {
            day -= daysInMonth(year, month)
            month++
            if (month > 12) {
                month = 1
                year++
            }
        }
        return "${year.toString().padStart(4, '0')}-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"
    }

    private fun calculateDurationDays(startDate: String, endDate: String): Int {
        if (startDate.isBlank() || endDate.isBlank()) return 3
        val sParts = startDate.split("-")
        val eParts = endDate.split("-")
        if (sParts.size != 3 || eParts.size != 3) return 3

        val sDay = sParts[2].toIntOrNull() ?: 1
        val eDay = eParts[2].toIntOrNull() ?: 4
        val diff = eDay - sDay
        return if (diff > 0) diff else 3
    }
}
