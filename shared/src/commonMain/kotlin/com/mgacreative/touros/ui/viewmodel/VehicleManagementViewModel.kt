package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.domain.model.Vehicle
import com.mgacreative.touros.domain.repository.VehicleRepository
import com.mgacreative.touros.domain.usecase.CreateVehicleUseCase
import com.mgacreative.touros.domain.usecase.GetCurrentUserUseCase
import com.mgacreative.touros.domain.usecase.GetVehiclesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface VehicleUiState {
    data object Loading : VehicleUiState
    data class Success(
        val vehicles: List<Vehicle> = emptyList(),
        val selectedFilterType: String? = null // null: Tüm Araçlar, "bus", "minibus", "vip"
    ) : VehicleUiState
    data class Error(val message: String) : VehicleUiState
}

data class VehicleFormState(
    val id: String = "",
    val plateNumber: String = "",
    val brand: String = "",
    val model: String = "",
    val year: String = "2024",
    val capacity: String = "46",
    val vehicleType: String = "bus", // bus (Otobüs), minibus (Minibüs), vip (VIP)
    val color: String = "",
    val isOwned: Boolean = true,
    val ownerInfo: String = "",
    val insuranceExpiry: String = "",
    val inspectionExpiry: String = "",
    val lastMaintenanceDate: String = "",
    val nextMaintenanceDate: String = "",
    val maintenanceNotes: String = "",
    val isActive: Boolean = true,
    val isFormOpen: Boolean = false,
    val isEditing: Boolean = false
)

class VehicleManagementViewModel(
    private val getVehiclesUseCase: GetVehiclesUseCase,
    private val createVehicleUseCase: CreateVehicleUseCase,
    private val vehicleRepository: VehicleRepository,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<VehicleUiState>(VehicleUiState.Loading)
    val uiState: StateFlow<VehicleUiState> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(VehicleFormState())
    val formState: StateFlow<VehicleFormState> = _formState.asStateFlow()

    init {
        loadVehicles()
    }

    fun loadVehicles(typeFilter: String? = null) {
        viewModelScope.launch {
            _uiState.value = VehicleUiState.Loading
            val user = getCurrentUserUseCase()
            val tenantId = user?.tenantId ?: "tenant_id"

            val res = getVehiclesUseCase(tenantId, typeFilter)
            res.onSuccess { list ->
                val fallbackList = if (list.isEmpty()) {
                    listOf(
                        Vehicle(
                            id = "v1",
                            plateNumber = "34 TOUR 01",
                            brand = "Mercedes-Benz",
                            model = "Travego 15 SHD",
                            year = 2024,
                            capacity = 46,
                            vehicleType = "bus",
                            color = "Beyaz",
                            isOwned = true,
                            insuranceExpiry = "2026-12-31",
                            inspectionExpiry = "2026-11-15",
                            lastMaintenanceDate = "2026-05-10",
                            nextMaintenanceDate = "2026-11-10",
                            maintenanceNotes = "Periyodik 30.000 km bakımı tamamlandı. Lastikler yeni.",
                            tenantId = tenantId
                        ),
                        Vehicle(
                            id = "v2",
                            plateNumber = "34 VIP 99",
                            brand = "Mercedes-Benz",
                            model = "Sprinter VIP Design",
                            year = 2025,
                            capacity = 16,
                            vehicleType = "minibus",
                            color = "Siyah",
                            isOwned = true,
                            insuranceExpiry = "2027-01-20",
                            inspectionExpiry = "2026-10-01",
                            lastMaintenanceDate = "2026-06-01",
                            nextMaintenanceDate = "2026-12-01",
                            maintenanceNotes = "Deri koltuk bakımı ve klima gazı kontrolü yapıldı.",
                            tenantId = tenantId
                        ),
                        Vehicle(
                            id = "v3",
                            plateNumber = "34 LUX 77",
                            brand = "Mercedes-Benz",
                            model = "V-Class Maybach Edition",
                            year = 2025,
                            capacity = 6,
                            vehicleType = "vip",
                            color = "Siyah Metallik",
                            isOwned = false,
                            ownerInfo = "Metro Rent A Car",
                            insuranceExpiry = "2026-09-30",
                            inspectionExpiry = "2026-08-30",
                            lastMaintenanceDate = "2026-06-15",
                            nextMaintenanceDate = "2026-09-15",
                            maintenanceNotes = "VIP araç kiralık filo kaydı.",
                            tenantId = tenantId
                        )
                    )
                } else list

                _uiState.value = VehicleUiState.Success(
                    vehicles = if (typeFilter != null) fallbackList.filter { it.vehicleType == typeFilter } else fallbackList,
                    selectedFilterType = typeFilter
                )
            }.onFailure { err ->
                _uiState.value = VehicleUiState.Error(err.message ?: "Araç parkı yüklenemedi.")
            }
        }
    }

    fun setVehicleTypeFilter(type: String?) {
        loadVehicles(type)
    }

    fun openNewForm() {
        _formState.value = VehicleFormState(isFormOpen = true, isEditing = false)
    }

    fun openEditForm(vehicle: Vehicle) {
        _formState.value = VehicleFormState(
            id = vehicle.id,
            plateNumber = vehicle.plateNumber,
            brand = vehicle.brand ?: "",
            model = vehicle.model ?: "",
            year = (vehicle.year ?: 2024).toString(),
            capacity = vehicle.capacity.toString(),
            vehicleType = vehicle.vehicleType,
            color = vehicle.color ?: "",
            isOwned = vehicle.isOwned,
            ownerInfo = vehicle.ownerInfo ?: "",
            insuranceExpiry = vehicle.insuranceExpiry ?: "",
            inspectionExpiry = vehicle.inspectionExpiry ?: "",
            lastMaintenanceDate = vehicle.lastMaintenanceDate ?: "",
            nextMaintenanceDate = vehicle.nextMaintenanceDate ?: "",
            maintenanceNotes = vehicle.maintenanceNotes ?: "",
            isActive = vehicle.isActive,
            isFormOpen = true,
            isEditing = true
        )
    }

    fun closeForm() {
        _formState.value = VehicleFormState(isFormOpen = false)
    }

    fun updatePlateNumber(value: String) { _formState.value = _formState.value.copy(plateNumber = value) }
    fun updateBrand(value: String) { _formState.value = _formState.value.copy(brand = value) }
    fun updateModel(value: String) { _formState.value = _formState.value.copy(model = value) }
    fun updateYear(value: String) { _formState.value = _formState.value.copy(year = value) }
    fun updateCapacity(value: String) { _formState.value = _formState.value.copy(capacity = value) }
    fun updateVehicleType(value: String) { _formState.value = _formState.value.copy(vehicleType = value) }
    fun updateColor(value: String) { _formState.value = _formState.value.copy(color = value) }
    fun updateIsOwned(value: Boolean) { _formState.value = _formState.value.copy(isOwned = value) }
    fun updateOwnerInfo(value: String) { _formState.value = _formState.value.copy(ownerInfo = value) }
    fun updateInsuranceExpiry(value: String) { _formState.value = _formState.value.copy(insuranceExpiry = value) }
    fun updateInspectionExpiry(value: String) { _formState.value = _formState.value.copy(inspectionExpiry = value) }
    fun updateLastMaintenanceDate(value: String) { _formState.value = _formState.value.copy(lastMaintenanceDate = value) }
    fun updateNextMaintenanceDate(value: String) { _formState.value = _formState.value.copy(nextMaintenanceDate = value) }
    fun updateMaintenanceNotes(value: String) { _formState.value = _formState.value.copy(maintenanceNotes = value) }
    fun updateIsActive(value: Boolean) { _formState.value = _formState.value.copy(isActive = value) }

    fun saveVehicle() {
        viewModelScope.launch {
            val user = getCurrentUserUseCase()
            val tenantId = user?.tenantId ?: "tenant_id"
            val state = _formState.value

            val vehicle = Vehicle(
                id = state.id,
                plateNumber = state.plateNumber.uppercase(),
                brand = state.brand.ifBlank { null },
                model = state.model.ifBlank { null },
                year = state.year.toIntOrNull(),
                capacity = state.capacity.toIntOrNull() ?: 46,
                vehicleType = state.vehicleType,
                color = state.color.ifBlank { null },
                isOwned = state.isOwned,
                ownerInfo = state.ownerInfo.ifBlank { null },
                insuranceExpiry = state.insuranceExpiry.ifBlank { null },
                inspectionExpiry = state.inspectionExpiry.ifBlank { null },
                lastMaintenanceDate = state.lastMaintenanceDate.ifBlank { null },
                nextMaintenanceDate = state.nextMaintenanceDate.ifBlank { null },
                maintenanceNotes = state.maintenanceNotes.ifBlank { null },
                isActive = state.isActive,
                tenantId = tenantId
            )

            val res = createVehicleUseCase(vehicle)
            res.onSuccess {
                closeForm()
                loadVehicles()
            }.onFailure { err ->
                _uiState.value = VehicleUiState.Error(err.message ?: "Araç kaydedilemedi.")
            }
        }
    }

    fun deleteVehicle(vehicleId: String) {
        viewModelScope.launch {
            vehicleRepository.deleteVehicle(vehicleId)
            loadVehicles()
        }
    }
}
