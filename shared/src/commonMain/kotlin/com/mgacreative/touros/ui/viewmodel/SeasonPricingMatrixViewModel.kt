package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.domain.model.HotelSeasonRate
import com.mgacreative.touros.domain.model.RoomType
import com.mgacreative.touros.domain.repository.HotelRepository
import com.mgacreative.touros.domain.usecase.CreateHotelSeasonRateUseCase
import com.mgacreative.touros.domain.usecase.GetCurrentUserUseCase
import com.mgacreative.touros.domain.usecase.GetHotelSeasonRatesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface SeasonPricingMatrixUiState {
    data object Loading : SeasonPricingMatrixUiState
    data class Success(
        val seasonRates: List<HotelSeasonRate> = emptyList(),
        val roomTypes: List<RoomType> = emptyList()
    ) : SeasonPricingMatrixUiState
    data class Error(val message: String) : SeasonPricingMatrixUiState
}

data class SeasonRateFormState(
    val id: String = "",
    val seasonName: String = "",
    val roomTypeId: String? = null,
    val startDate: String = "",
    val endDate: String = "",
    val singlePrice: String = "",
    val doublePrice: String = "",
    val triplePrice: String = "",
    val extraBedPrice: String = "",
    val childPrice: String = "",
    val currency: String = "TRY",
    val mealPlan: String = "BB", // BB, HB, FB, AI, RO
    val minStayDays: String = "1",
    val isActive: Boolean = true,
    val isFormOpen: Boolean = false,
    val isEditing: Boolean = false
)

class SeasonPricingMatrixViewModel(
    private val getHotelSeasonRatesUseCase: GetHotelSeasonRatesUseCase,
    private val createHotelSeasonRateUseCase: CreateHotelSeasonRateUseCase,
    private val hotelRepository: HotelRepository,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<SeasonPricingMatrixUiState>(SeasonPricingMatrixUiState.Loading)
    val uiState: StateFlow<SeasonPricingMatrixUiState> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(SeasonRateFormState())
    val formState: StateFlow<SeasonRateFormState> = _formState.asStateFlow()

    private var currentHotelId: String = ""

    fun initForHotel(hotelId: String) {
        currentHotelId = hotelId
        loadSeasonRatesAndRoomTypes()
    }

    fun loadSeasonRatesAndRoomTypes() {
        viewModelScope.launch {
            _uiState.value = SeasonPricingMatrixUiState.Loading
            val roomTypesRes = hotelRepository.getRoomTypesForHotel(currentHotelId)
            val roomTypes = roomTypesRes.getOrDefault(emptyList())

            val ratesRes = getHotelSeasonRatesUseCase(currentHotelId)
            ratesRes.onSuccess { list ->
                val fallbackList = if (list.isEmpty()) {
                    listOf(
                        HotelSeasonRate(
                            id = "sr1",
                            hotelId = currentHotelId,
                            roomTypeId = roomTypes.firstOrNull()?.id,
                            seasonName = "Yüksek Sezon (Yaz)",
                            startDate = "2026-06-15",
                            endDate = "2026-09-15",
                            singlePrice = 2200.0,
                            doublePrice = 3000.0,
                            triplePrice = 4000.0,
                            extraBedPrice = 800.0,
                            childPrice = 500.0,
                            currency = "TRY",
                            mealPlan = "BB",
                            minStayDays = 3,
                            isActive = true
                        ),
                        HotelSeasonRate(
                            id = "sr2",
                            hotelId = currentHotelId,
                            roomTypeId = roomTypes.firstOrNull()?.id,
                            seasonName = "Orta Sezon (Bahar)",
                            startDate = "2026-04-01",
                            endDate = "2026-06-14",
                            singlePrice = 1600.0,
                            doublePrice = 2200.0,
                            triplePrice = 3000.0,
                            extraBedPrice = 600.0,
                            childPrice = 350.0,
                            currency = "TRY",
                            mealPlan = "BB",
                            minStayDays = 2,
                            isActive = true
                        ),
                        HotelSeasonRate(
                            id = "sr3",
                            hotelId = currentHotelId,
                            roomTypeId = roomTypes.firstOrNull()?.id,
                            seasonName = "Düşük Sezon (Kış)",
                            startDate = "2026-11-01",
                            endDate = "2027-03-31",
                            singlePrice = 1100.0,
                            doublePrice = 1500.0,
                            triplePrice = 2000.0,
                            extraBedPrice = 400.0,
                            childPrice = 250.0,
                            currency = "TRY",
                            mealPlan = "BB",
                            minStayDays = 1,
                            isActive = true
                        )
                    )
                } else list

                _uiState.value = SeasonPricingMatrixUiState.Success(
                    seasonRates = fallbackList,
                    roomTypes = roomTypes
                )
            }.onFailure { err ->
                _uiState.value = SeasonPricingMatrixUiState.Error(err.message ?: "Sezon fiyat matrisi yüklenirken hata oluştu.")
            }
        }
    }

    fun openNewForm() {
        _formState.value = SeasonRateFormState(isFormOpen = true, isEditing = false)
    }

    fun openEditForm(rate: HotelSeasonRate) {
        _formState.value = SeasonRateFormState(
            id = rate.id,
            seasonName = rate.seasonName,
            roomTypeId = rate.roomTypeId,
            startDate = rate.startDate,
            endDate = rate.endDate,
            singlePrice = rate.singlePrice.toString(),
            doublePrice = rate.doublePrice.toString(),
            triplePrice = rate.triplePrice.toString(),
            extraBedPrice = rate.extraBedPrice.toString(),
            childPrice = rate.childPrice.toString(),
            currency = rate.currency,
            mealPlan = rate.mealPlan,
            minStayDays = rate.minStayDays.toString(),
            isActive = rate.isActive,
            isFormOpen = true,
            isEditing = true
        )
    }

    fun closeForm() {
        _formState.value = SeasonRateFormState(isFormOpen = false)
    }

    fun updateSeasonName(value: String) { _formState.value = _formState.value.copy(seasonName = value) }
    fun updateRoomTypeId(value: String?) { _formState.value = _formState.value.copy(roomTypeId = value) }
    fun updateStartDate(value: String) { _formState.value = _formState.value.copy(startDate = value) }
    fun updateEndDate(value: String) { _formState.value = _formState.value.copy(endDate = value) }
    fun updateSinglePrice(value: String) { _formState.value = _formState.value.copy(singlePrice = value) }
    fun updateDoublePrice(value: String) { _formState.value = _formState.value.copy(doublePrice = value) }
    fun updateTriplePrice(value: String) { _formState.value = _formState.value.copy(triplePrice = value) }
    fun updateExtraBedPrice(value: String) { _formState.value = _formState.value.copy(extraBedPrice = value) }
    fun updateChildPrice(value: String) { _formState.value = _formState.value.copy(childPrice = value) }
    fun updateCurrency(value: String) { _formState.value = _formState.value.copy(currency = value) }
    fun updateMealPlan(value: String) { _formState.value = _formState.value.copy(mealPlan = value) }
    fun updateMinStayDays(value: String) { _formState.value = _formState.value.copy(minStayDays = value) }
    fun updateIsActive(value: Boolean) { _formState.value = _formState.value.copy(isActive = value) }

    fun saveSeasonRate() {
        viewModelScope.launch {
            val user = getCurrentUserUseCase()
            val tenantId = user?.tenantId ?: "tenant_id"
            val state = _formState.value

            val rate = HotelSeasonRate(
                id = state.id,
                hotelId = currentHotelId,
                roomTypeId = state.roomTypeId,
                seasonName = state.seasonName,
                startDate = state.startDate,
                endDate = state.endDate,
                singlePrice = state.singlePrice.toDoubleOrNull() ?: 0.0,
                doublePrice = state.doublePrice.toDoubleOrNull() ?: 0.0,
                triplePrice = state.triplePrice.toDoubleOrNull() ?: 0.0,
                extraBedPrice = state.extraBedPrice.toDoubleOrNull() ?: 0.0,
                childPrice = state.childPrice.toDoubleOrNull() ?: 0.0,
                currency = state.currency,
                mealPlan = state.mealPlan,
                minStayDays = state.minStayDays.toIntOrNull() ?: 1,
                isActive = state.isActive,
                tenantId = tenantId
            )

            val res = createHotelSeasonRateUseCase(rate)
            res.onSuccess {
                closeForm()
                loadSeasonRatesAndRoomTypes()
            }.onFailure { err ->
                _uiState.value = SeasonPricingMatrixUiState.Error(err.message ?: "Sezon fiyat kaydı yüklenemedi.")
            }
        }
    }

    fun deleteSeasonRate(rateId: String) {
        viewModelScope.launch {
            hotelRepository.deleteSeasonRate(rateId)
            loadSeasonRatesAndRoomTypes()
        }
    }
}
