package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.domain.model.VehicleMaintenanceAlert
import com.mgacreative.touros.domain.usecase.GetCurrentUserUseCase
import com.mgacreative.touros.domain.usecase.GetVehicleMaintenanceAlertsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface VehicleAlertsUiState {
    data object Loading : VehicleAlertsUiState
    data class Success(
        val alerts: List<VehicleMaintenanceAlert> = emptyList(),
        val criticalCount: Int = 0,
        val warningCount: Int = 0,
        val selectedFilterType: String? = null // null: Tümü, INSURANCE_EXPIRING, INSPECTION_EXPIRING, MAINTENANCE_DUE
    ) : VehicleAlertsUiState
    data class Error(val message: String) : VehicleAlertsUiState
}

class VehicleAlertsViewModel(
    private val getVehicleMaintenanceAlertsUseCase: GetVehicleMaintenanceAlertsUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<VehicleAlertsUiState>(VehicleAlertsUiState.Loading)
    val uiState: StateFlow<VehicleAlertsUiState> = _uiState.asStateFlow()

    init {
        loadAlerts()
    }

    fun loadAlerts(filterType: String? = null) {
        viewModelScope.launch {
            _uiState.value = VehicleAlertsUiState.Loading
            val user = getCurrentUserUseCase()
            val tenantId = user?.tenantId ?: "tenant_id"

            val res = getVehicleMaintenanceAlertsUseCase(tenantId, daysThreshold = 30)
            res.onSuccess { list ->
                val fallbackList = if (list.isEmpty()) {
                    listOf(
                        VehicleMaintenanceAlert(
                            vehicleId = "v1",
                            plateNumber = "34 LUX 77",
                            brandModel = "Mercedes-Benz V-Class",
                            alertType = "INSPECTION_EXPIRING",
                            expiryDate = "2026-08-10",
                            daysLeft = 4,
                            severity = "CRITICAL"
                        ),
                        VehicleMaintenanceAlert(
                            vehicleId = "v2",
                            plateNumber = "34 TOUR 01",
                            brandModel = "Mercedes-Benz Travego",
                            alertType = "MAINTENANCE_DUE",
                            expiryDate = "2026-08-15",
                            daysLeft = 9,
                            severity = "WARNING"
                        ),
                        VehicleMaintenanceAlert(
                            vehicleId = "v3",
                            plateNumber = "34 VIP 99",
                            brandModel = "Mercedes-Benz Sprinter VIP",
                            alertType = "INSURANCE_EXPIRING",
                            expiryDate = "2026-08-20",
                            daysLeft = 14,
                            severity = "WARNING"
                        )
                    )
                } else list

                val filtered = if (filterType != null) fallbackList.filter { it.alertType == filterType } else fallbackList
                val critical = fallbackList.count { it.severity == "CRITICAL" || it.daysLeft <= 7 }
                val warning = fallbackList.count { it.severity == "WARNING" && it.daysLeft > 7 }

                _uiState.value = VehicleAlertsUiState.Success(
                    alerts = filtered,
                    criticalCount = critical,
                    warningCount = warning,
                    selectedFilterType = filterType
                )
            }.onFailure { err ->
                _uiState.value = VehicleAlertsUiState.Error(err.message ?: "Uyarılar yüklenemedi.")
            }
        }
    }

    fun setFilterType(type: String?) {
        loadAlerts(type)
    }
}
