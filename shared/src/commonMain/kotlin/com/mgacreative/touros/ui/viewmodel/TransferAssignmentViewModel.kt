package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.domain.model.Driver
import com.mgacreative.touros.domain.model.Guide
import com.mgacreative.touros.domain.model.TransferTask
import com.mgacreative.touros.domain.model.Vehicle
import com.mgacreative.touros.domain.repository.TransferRepository
import com.mgacreative.touros.domain.repository.VehicleRepository
import com.mgacreative.touros.domain.usecase.AssignDriverAndGuideUseCase
import com.mgacreative.touros.domain.usecase.GetCurrentUserUseCase
import com.mgacreative.touros.domain.usecase.GetTransfersUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface TransferAssignmentUiState {
    data object Loading : TransferAssignmentUiState
    data class Success(
        val transfers: List<TransferTask> = emptyList(),
        val drivers: List<Driver> = emptyList(),
        val guides: List<Guide> = emptyList(),
        val vehicles: List<Vehicle> = emptyList(),
        val selectedStatusFilter: String? = null
    ) : TransferAssignmentUiState
    data class Error(val message: String) : TransferAssignmentUiState
}

data class AssignmentDialogState(
    val transfer: TransferTask? = null,
    val selectedDriverId: String? = null,
    val selectedGuideId: String? = null,
    val selectedVehicleId: String? = null,
    val isOpen: Boolean = false
)

class TransferAssignmentViewModel(
    private val getTransfersUseCase: GetTransfersUseCase,
    private val assignDriverAndGuideUseCase: AssignDriverAndGuideUseCase,
    private val transferRepository: TransferRepository,
    private val vehicleRepository: VehicleRepository,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<TransferAssignmentUiState>(TransferAssignmentUiState.Loading)
    val uiState: StateFlow<TransferAssignmentUiState> = _uiState.asStateFlow()

    private val _dialogState = MutableStateFlow(AssignmentDialogState())
    val dialogState: StateFlow<AssignmentDialogState> = _dialogState.asStateFlow()

    init {
        loadData()
    }

    fun loadData(statusFilter: String? = null) {
        viewModelScope.launch {
            _uiState.value = TransferAssignmentUiState.Loading
            val user = getCurrentUserUseCase()
            val tenantId = user?.tenantId ?: "tenant_id"

            val driversRes = transferRepository.getDrivers(tenantId)
            val drivers = driversRes.getOrDefault(emptyList()).ifEmpty {
                listOf(
                    Driver("d1", "Ahmet Yılmaz", "0532 111 2233", "ahmet@touros.com", "D1", "2028-12-31", "12345678901", true, tenantId),
                    Driver("d2", "Mehmet Kaya", "0533 222 3344", "mehmet@touros.com", "D", "2027-06-30", "98765432109", true, tenantId),
                    Driver("d3", "Caner Demir", "0535 333 4455", "caner@touros.com", "D1", "2029-01-15", "45678912301", true, tenantId)
                )
            }

            val guidesRes = transferRepository.getGuides(tenantId)
            val guides = guidesRes.getOrDefault(emptyList()).ifEmpty {
                listOf(
                    Guide(id = "g1", fullName = "Zeynep Arslan", phone = "0542 555 6677", email = "zeynep@touros.com", licenseNumber = "K-1234", languages = listOf("Türkçe", "İngilizce", "Almanca"), specialization = "Kültür Turları", isActive = true, tenantId = tenantId),
                    Guide(id = "g2", fullName = "Burak Celal", phone = "0543 666 7788", email = "burak@touros.com", licenseNumber = "K-5678", languages = listOf("Türkçe", "İspanyolca"), specialization = "Doğa & Trekking", isActive = true, tenantId = tenantId),
                    Guide(id = "g3", fullName = "Elif Şahin", phone = "0544 777 8899", email = "elif@touros.com", licenseNumber = "K-9012", languages = listOf("Türkçe", "Fransızca", "İngilizce"), specialization = "Gastro & Şehir", isActive = true, tenantId = tenantId)
                )
            }

            val vehiclesRes = vehicleRepository.getVehicles(tenantId)
            val vehicles = vehiclesRes.getOrDefault(emptyList()).ifEmpty {
                listOf(
                    Vehicle("v1", "34 TOUR 01", "Mercedes-Benz", "Travego", 2024, 46, "bus", "Beyaz", true, tenantId = tenantId),
                    Vehicle("v2", "34 VIP 99", "Mercedes-Benz", "Sprinter VIP", 2025, 16, "minibus", "Siyah", true, tenantId = tenantId),
                    Vehicle("v3", "34 LUX 77", "Mercedes-Benz", "V-Class", 2025, 6, "vip", "Siyah Metallik", true, tenantId = tenantId)
                )
            }

            val transfersRes = getTransfersUseCase(tenantId, statusFilter)
            transfersRes.onSuccess { list ->
                val fallbackList = if (list.isEmpty()) {
                    listOf(
                        TransferTask(
                            id = "t1",
                            bookingId = "b-101",
                            departureId = "dep-1",
                            vehicleId = "v1",
                            driverId = "d1",
                            guideId = "g1",
                            transferType = "airport",
                            origin = "İstanbul Havalimanı (IST)",
                            destination = "Taksim Grand Hotel",
                            pickupTime = "2026-08-10 14:30",
                            dropoffTime = "2026-08-10 16:00",
                            paxCount = 28,
                            status = "assigned",
                            price = 1200.0,
                            currency = "TRY",
                            notes = "Karşılama tabelasında 'TourOS Grubu' yazacak.",
                            tenantId = tenantId
                        ),
                        TransferTask(
                            id = "t2",
                            bookingId = "b-102",
                            departureId = "dep-2",
                            vehicleId = null,
                            driverId = null,
                            guideId = null,
                            transferType = "tour",
                            origin = "Kapadokya Otel Kalkış",
                            destination = "Göreme Açık Hava Müzesi & Uçhisar",
                            pickupTime = "2026-08-12 09:00",
                            dropoffTime = "2026-08-12 17:30",
                            paxCount = 14,
                            status = "planned",
                            price = 2500.0,
                            currency = "TRY",
                            notes = "Şoför ve kokartlı rehber ataması bekleniyor.",
                            tenantId = tenantId
                        ),
                        TransferTask(
                            id = "t3",
                            bookingId = "b-103",
                            departureId = null,
                            vehicleId = "v3",
                            driverId = "d3",
                            guideId = "g3",
                            transferType = "custom",
                            origin = "Sabiha Gökçen (SAW)",
                            destination = "Bodrum Yalıkavak Marina",
                            pickupTime = "2026-08-15 11:00",
                            dropoffTime = "2026-08-15 18:00",
                            paxCount = 4,
                            status = "assigned",
                            price = 4500.0,
                            currency = "TRY",
                            notes = "Özel VIP transfer görevi.",
                            tenantId = tenantId
                        )
                    )
                } else list

                _uiState.value = TransferAssignmentUiState.Success(
                    transfers = if (statusFilter != null) fallbackList.filter { it.status == statusFilter } else fallbackList,
                    drivers = drivers,
                    guides = guides,
                    vehicles = vehicles,
                    selectedStatusFilter = statusFilter
                )
            }.onFailure { err ->
                _uiState.value = TransferAssignmentUiState.Error(err.message ?: "Transfer görevleri yüklenemedi.")
            }
        }
    }

    fun setStatusFilter(status: String?) {
        loadData(status)
    }

    fun openAssignmentDialog(transfer: TransferTask) {
        _dialogState.value = AssignmentDialogState(
            transfer = transfer,
            selectedDriverId = transfer.driverId,
            selectedGuideId = transfer.guideId,
            selectedVehicleId = transfer.vehicleId,
            isOpen = true
        )
    }

    fun closeAssignmentDialog() {
        _dialogState.value = AssignmentDialogState(isOpen = false)
    }

    fun selectDriver(driverId: String?) { _dialogState.value = _dialogState.value.copy(selectedDriverId = driverId) }
    fun selectGuide(guideId: String?) { _dialogState.value = _dialogState.value.copy(selectedGuideId = guideId) }
    fun selectVehicle(vehicleId: String?) { _dialogState.value = _dialogState.value.copy(selectedVehicleId = vehicleId) }

    fun saveAssignment() {
        viewModelScope.launch {
            val state = _dialogState.value
            val transferId = state.transfer?.id ?: return@launch

            val res = assignDriverAndGuideUseCase(
                transferId = transferId,
                driverId = state.selectedDriverId,
                guideId = state.selectedGuideId,
                vehicleId = state.selectedVehicleId
            )
            res.onSuccess {
                closeAssignmentDialog()
                loadData()
            }.onFailure { err ->
                _uiState.value = TransferAssignmentUiState.Error(err.message ?: "Atama kaydedilemedi.")
            }
        }
    }
}
