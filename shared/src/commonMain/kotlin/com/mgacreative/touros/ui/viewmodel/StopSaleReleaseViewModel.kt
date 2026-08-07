package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.domain.model.HotelStopSale
import com.mgacreative.touros.domain.model.RoomType
import com.mgacreative.touros.domain.repository.HotelRepository
import com.mgacreative.touros.domain.usecase.CreateHotelStopSaleUseCase
import com.mgacreative.touros.domain.usecase.GetCurrentUserUseCase
import com.mgacreative.touros.domain.usecase.GetHotelStopSalesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface StopSaleReleaseUiState {
    data object Loading : StopSaleReleaseUiState
    data class Success(
        val stopSales: List<HotelStopSale> = emptyList(),
        val roomTypes: List<RoomType> = emptyList(),
        val activeStopSaleCount: Int = 0,
        val activeReleaseCount: Int = 0
    ) : StopSaleReleaseUiState
    data class Error(val message: String) : StopSaleReleaseUiState
}

data class StopSaleFormState(
    val id: String = "",
    val roomTypeId: String? = null,
    val actionType: String = "STOP_SALE", // STOP_SALE | RELEASE
    val startDate: String = "",
    val endDate: String = "",
    val reason: String = "",
    val isActive: Boolean = true,
    val isFormOpen: Boolean = false
)

class StopSaleReleaseViewModel(
    private val getHotelStopSalesUseCase: GetHotelStopSalesUseCase,
    private val createHotelStopSaleUseCase: CreateHotelStopSaleUseCase,
    private val hotelRepository: HotelRepository,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<StopSaleReleaseUiState>(StopSaleReleaseUiState.Loading)
    val uiState: StateFlow<StopSaleReleaseUiState> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(StopSaleFormState())
    val formState: StateFlow<StopSaleFormState> = _formState.asStateFlow()

    private var currentHotelId: String = ""

    fun initForHotel(hotelId: String) {
        currentHotelId = hotelId
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = StopSaleReleaseUiState.Loading
            val roomTypesRes = hotelRepository.getRoomTypesForHotel(currentHotelId)
            val roomTypes = roomTypesRes.getOrDefault(emptyList())

            val stopSalesRes = getHotelStopSalesUseCase(currentHotelId)
            stopSalesRes.onSuccess { list ->
                val fallbackList = if (list.isEmpty()) {
                    listOf(
                        HotelStopSale(
                            id = "ss1",
                            hotelId = currentHotelId,
                            roomTypeId = roomTypes.firstOrNull()?.id,
                            actionType = "STOP_SALE",
                            startDate = "2026-07-20",
                            endDate = "2026-07-27",
                            reason = "Yüksek sezonda otel dolu. Satış durduruldu.",
                            isActive = true,
                            createdAt = "2026-07-01"
                        ),
                        HotelStopSale(
                            id = "ss2",
                            hotelId = currentHotelId,
                            roomTypeId = null,
                            actionType = "RELEASE",
                            startDate = "2026-08-10",
                            endDate = "2026-08-20",
                            reason = "Acente kalan 4 oda kontenjanını serbest bıraktı (Release).",
                            isActive = true,
                            createdAt = "2026-07-15"
                        )
                    )
                } else list

                _uiState.value = StopSaleReleaseUiState.Success(
                    stopSales = fallbackList,
                    roomTypes = roomTypes,
                    activeStopSaleCount = fallbackList.count { it.actionType == "STOP_SALE" && it.isActive },
                    activeReleaseCount = fallbackList.count { it.actionType == "RELEASE" && it.isActive }
                )
            }.onFailure { err ->
                _uiState.value = StopSaleReleaseUiState.Error(err.message ?: "Stop Sale verileri yüklenemedi.")
            }
        }
    }

    fun openNewForm(actionType: String = "STOP_SALE") {
        _formState.value = StopSaleFormState(actionType = actionType, isFormOpen = true)
    }

    fun closeForm() {
        _formState.value = StopSaleFormState(isFormOpen = false)
    }

    fun updateRoomTypeId(value: String?) { _formState.value = _formState.value.copy(roomTypeId = value) }
    fun updateActionType(value: String) { _formState.value = _formState.value.copy(actionType = value) }
    fun updateStartDate(value: String) { _formState.value = _formState.value.copy(startDate = value) }
    fun updateEndDate(value: String) { _formState.value = _formState.value.copy(endDate = value) }
    fun updateReason(value: String) { _formState.value = _formState.value.copy(reason = value) }
    fun updateIsActive(value: Boolean) { _formState.value = _formState.value.copy(isActive = value) }

    fun applyStopSaleOrRelease() {
        viewModelScope.launch {
            val user = getCurrentUserUseCase()
            val tenantId = user?.tenantId ?: "tenant_id"
            val state = _formState.value

            val item = HotelStopSale(
                id = state.id,
                hotelId = currentHotelId,
                roomTypeId = state.roomTypeId,
                actionType = state.actionType,
                startDate = state.startDate,
                endDate = state.endDate,
                reason = state.reason.ifBlank { null },
                isActive = state.isActive,
                tenantId = tenantId
            )

            val res = createHotelStopSaleUseCase(item)
            res.onSuccess {
                closeForm()
                loadData()
            }.onFailure { err ->
                _uiState.value = StopSaleReleaseUiState.Error(err.message ?: "İşlem uygulanamadı.")
            }
        }
    }

    fun toggleStatus(id: String, currentIsActive: Boolean) {
        viewModelScope.launch {
            hotelRepository.toggleStopSaleStatus(id, !currentIsActive)
            loadData()
        }
    }

    fun deleteItem(id: String) {
        viewModelScope.launch {
            hotelRepository.deleteStopSale(id)
            loadData()
        }
    }
}
