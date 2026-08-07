package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.domain.model.GuideAssignedTour
import com.mgacreative.touros.domain.usecase.GetCurrentUserUseCase
import com.mgacreative.touros.domain.usecase.GetGuideAssignedToursUseCase
import com.mgacreative.touros.domain.usecase.TogglePassengerCheckInUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface GuideMobileUiState {
    data object Loading : GuideMobileUiState
    data class Success(
        val assignedTours: List<GuideAssignedTour> = emptyList(),
        val selectedTour: GuideAssignedTour? = null,
        val activeTab: Int = 0, // 0: Turlarım, 1: Yolcu Listesi & Yoklama, 2: Pickup Noktaları
        val passengerSearchQuery: String = "",
        val guideName: String = "Zeynep Arslan",
        val licenseNumber: String = "K-12345"
    ) : GuideMobileUiState
    data class Error(val message: String) : GuideMobileUiState
}

class GuideMobileViewModel(
    private val getGuideAssignedToursUseCase: GetGuideAssignedToursUseCase,
    private val togglePassengerCheckInUseCase: TogglePassengerCheckInUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<GuideMobileUiState>(GuideMobileUiState.Loading)
    val uiState: StateFlow<GuideMobileUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = GuideMobileUiState.Loading
            val user = getCurrentUserUseCase()
            val tenantId = user?.tenantId ?: "tenant_id"
            val guideId = user?.id ?: "guide_id"

            val res = getGuideAssignedToursUseCase(guideId, tenantId)
            res.onSuccess { list ->
                _uiState.value = GuideMobileUiState.Success(
                    assignedTours = list,
                    selectedTour = list.firstOrNull(),
                    guideName = user?.fullName ?: "Zeynep Arslan",
                    licenseNumber = "K-12345"
                )
            }.onFailure { err ->
                _uiState.value = GuideMobileUiState.Error(err.message ?: "Arayüz bilgileri yüklenemedi.")
            }
        }
    }

    fun selectTab(tabIndex: Int) {
        val state = _uiState.value as? GuideMobileUiState.Success ?: return
        _uiState.value = state.copy(activeTab = tabIndex)
    }

    fun selectTour(tour: GuideAssignedTour) {
        val state = _uiState.value as? GuideMobileUiState.Success ?: return
        _uiState.value = state.copy(selectedTour = tour)
    }

    fun updatePassengerSearch(query: String) {
        val state = _uiState.value as? GuideMobileUiState.Success ?: return
        _uiState.value = state.copy(passengerSearchQuery = query)
    }

    fun toggleCheckIn(passengerId: String) {
        viewModelScope.launch {
            val state = _uiState.value as? GuideMobileUiState.Success ?: return@launch
            val currentTour = state.selectedTour ?: return@launch

            val updatedPassengers = currentTour.passengers.map { p ->
                if (p.passengerId == passengerId) {
                    val newCheckState = !p.isCheckIn
                    togglePassengerCheckInUseCase(passengerId, newCheckState)
                    p.copy(isCheckIn = newCheckState)
                } else p
            }

            val updatedTour = currentTour.copy(passengers = updatedPassengers)
            val updatedToursList = state.assignedTours.map {
                if (it.departureId == currentTour.departureId) updatedTour else it
            }

            _uiState.value = state.copy(
                assignedTours = updatedToursList,
                selectedTour = updatedTour
            )
        }
    }
}
