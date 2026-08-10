package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.domain.model.Hotel
import com.mgacreative.touros.domain.repository.HotelRepository
import com.mgacreative.touros.domain.usecase.GetCurrentUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface HotelListUiState {
    data object Loading : HotelListUiState
    data class Success(
        val allHotels: List<Hotel> = emptyList(),
        val filteredHotels: List<Hotel> = emptyList(),
        val searchQuery: String = "",
        val selectedStarFilter: Int? = null,
        val selectedStatusFilter: Boolean? = null
    ) : HotelListUiState
    data class Error(val message: String) : HotelListUiState
}

class HotelListViewModel(
    private val hotelRepository: HotelRepository,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<HotelListUiState>(HotelListUiState.Loading)
    val uiState: StateFlow<HotelListUiState> = _uiState.asStateFlow()

    init {
        loadHotels()
    }

    fun loadHotels() {
        viewModelScope.launch {
            _uiState.value = HotelListUiState.Loading
            val user = getCurrentUserUseCase()
            val tenantId = user?.tenantId ?: ""

            val result = hotelRepository.getHotels(tenantId)
            result.onSuccess { hotels ->
                val currentSearch = (_uiState.value as? HotelListUiState.Success)?.searchQuery ?: ""
                val currentStar = (_uiState.value as? HotelListUiState.Success)?.selectedStarFilter
                val currentStatus = (_uiState.value as? HotelListUiState.Success)?.selectedStatusFilter

                val filtered = applyFilters(hotels, currentSearch, currentStar, currentStatus)

                _uiState.value = HotelListUiState.Success(
                    allHotels = hotels,
                    filteredHotels = filtered,
                    searchQuery = currentSearch,
                    selectedStarFilter = currentStar,
                    selectedStatusFilter = currentStatus
                )
            }.onFailure { err ->
                _uiState.value = HotelListUiState.Error(err.message ?: "Oteller yüklenirken hata oluştu")
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        val state = _uiState.value as? HotelListUiState.Success ?: return
        val filtered = applyFilters(state.allHotels, query, state.selectedStarFilter, state.selectedStatusFilter)
        _uiState.value = state.copy(searchQuery = query, filteredHotels = filtered)
    }

    fun onStarFilterSelected(star: Int?) {
        val state = _uiState.value as? HotelListUiState.Success ?: return
        val filtered = applyFilters(state.allHotels, state.searchQuery, star, state.selectedStatusFilter)
        _uiState.value = state.copy(selectedStarFilter = star, filteredHotels = filtered)
    }

    fun onStatusFilterSelected(status: Boolean?) {
        val state = _uiState.value as? HotelListUiState.Success ?: return
        val filtered = applyFilters(state.allHotels, state.searchQuery, state.selectedStarFilter, status)
        _uiState.value = state.copy(selectedStatusFilter = status, filteredHotels = filtered)
    }

    fun onToggleHotelStatus(hotelId: String, currentStatus: Boolean) {
        viewModelScope.launch {
            val state = _uiState.value as? HotelListUiState.Success ?: return@launch
            val targetHotel = state.allHotels.find { it.id == hotelId } ?: return@launch
            val updated = targetHotel.copy(isActive = !currentStatus)

            val res = hotelRepository.updateHotel(updated)
            if (res.isSuccess) {
                val newAll = state.allHotels.map { if (it.id == hotelId) updated else it }
                val filtered = applyFilters(newAll, state.searchQuery, state.selectedStarFilter, state.selectedStatusFilter)
                _uiState.value = state.copy(allHotels = newAll, filteredHotels = filtered)
            }
        }
    }

    private fun applyFilters(
        hotels: List<Hotel>,
        query: String,
        starFilter: Int?,
        statusFilter: Boolean?
    ): List<Hotel> {
        return hotels.filter { h ->
            val matchesQuery = query.isBlank() ||
                    h.name.contains(query, ignoreCase = true) ||
                    (h.city ?: "").contains(query, ignoreCase = true) ||
                    (h.address ?: "").contains(query, ignoreCase = true) ||
                    (h.phone ?: "").contains(query, ignoreCase = true)

            val matchesStar = starFilter == null || h.starRating == starFilter
            val matchesStatus = statusFilter == null || h.isActive == statusFilter

            matchesQuery && matchesStar && matchesStatus
        }
    }
}
