package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.domain.model.PickupPoint
import com.mgacreative.touros.domain.usecase.GetCurrentUserUseCase
import com.mgacreative.touros.domain.usecase.GetDriverPickupListUseCase
import com.mgacreative.touros.domain.usecase.UpdatePickupStatusUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface DriverPickupUiState {
    data object Loading : DriverPickupUiState
    data class Success(
        val pickups: List<PickupPoint> = emptyList(),
        val selectedPickup: PickupPoint? = null,
        val driverName: String = "Ahmet Yılmaz",
        val vehicleInfo: String = "34 TOUR 01 (Mercedes-Benz Travego)"
    ) : DriverPickupUiState
    data class Error(val message: String) : DriverPickupUiState
}

class DriverPickupListViewModel(
    private val getDriverPickupListUseCase: GetDriverPickupListUseCase,
    private val updatePickupStatusUseCase: UpdatePickupStatusUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<DriverPickupUiState>(DriverPickupUiState.Loading)
    val uiState: StateFlow<DriverPickupUiState> = _uiState.asStateFlow()

    init {
        loadPickupList()
    }

    fun loadPickupList() {
        viewModelScope.launch {
            _uiState.value = DriverPickupUiState.Loading
            val user = getCurrentUserUseCase()
            val tenantId = user?.tenantId ?: "tenant_id"

            val res = getDriverPickupListUseCase(tenantId)
            res.onSuccess { list ->
                val fallbackList = if (list.isEmpty()) {
                    listOf(
                        PickupPoint(
                            id = "p1",
                            transferId = "t1",
                            passengerName = "Hans Müller",
                            passengerPhone = "+49 171 1234567",
                            hotelName = "Hilton Istanbul Bosphorus",
                            locationName = "Harbiye, Şişli / İstanbul",
                            latitude = 41.0435,
                            longitude = 28.9882,
                            scheduledTime = "08:30",
                            status = "picked_up",
                            paxCount = 2,
                            roomNumber = "402",
                            notes = "VIP yolcu, 2 valiz.",
                            tenantId = tenantId
                        ),
                        PickupPoint(
                            id = "p2",
                            transferId = "t1",
                            passengerName = "Sarah Jenkins",
                            passengerPhone = "+44 7700 900077",
                            hotelName = "Ciragan Palace Kempinski",
                            locationName = "Beşiktaş / İstanbul",
                            latitude = 41.0439,
                            longitude = 29.0069,
                            scheduledTime = "09:00",
                            status = "pending",
                            paxCount = 4,
                            roomNumber = "118",
                            notes = "Çocuk koltuğu talep edildi.",
                            tenantId = tenantId
                        ),
                        PickupPoint(
                            id = "p3",
                            transferId = "t1",
                            passengerName = "Jean Dupont",
                            passengerPhone = "+33 612 345678",
                            hotelName = "Swissôtel The Bosphorus",
                            locationName = "Maçka, Beşiktaş / İstanbul",
                            latitude = 41.0401,
                            longitude = 28.9958,
                            scheduledTime = "09:30",
                            status = "pending",
                            paxCount = 3,
                            roomNumber = "305",
                            notes = "Lobi resepsiyonu karşısında bekleyecek.",
                            tenantId = tenantId
                        )
                    )
                } else list

                _uiState.value = DriverPickupUiState.Success(
                    pickups = fallbackList,
                    selectedPickup = fallbackList.firstOrNull { it.status == "pending" } ?: fallbackList.firstOrNull()
                )
            }.onFailure { err ->
                _uiState.value = DriverPickupUiState.Error(err.message ?: "Pickup listesi çekilemedi.")
            }
        }
    }

    fun selectPickupForMap(pickup: PickupPoint) {
        val currentState = _uiState.value as? DriverPickupUiState.Success ?: return
        _uiState.value = currentState.copy(selectedPickup = pickup)
    }

    fun updateStatus(pickupId: String, newStatus: String) {
        viewModelScope.launch {
            updatePickupStatusUseCase(pickupId, newStatus)
            val currentState = _uiState.value as? DriverPickupUiState.Success ?: return@launch
            val updatedList = currentState.pickups.map {
                if (it.id == pickupId) it.copy(status = newStatus) else it
            }
            _uiState.value = currentState.copy(
                pickups = updatedList,
                selectedPickup = updatedList.find { it.id == pickupId } ?: currentState.selectedPickup
            )
        }
    }
}
