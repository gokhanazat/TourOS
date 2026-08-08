package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.domain.model.Tour
import com.mgacreative.touros.domain.model.TourCategory
import com.mgacreative.touros.domain.usecase.GetCurrentUserUseCase
import com.mgacreative.touros.domain.usecase.GetToursUseCase
import com.mgacreative.touros.domain.usecase.ToggleTourStatusUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface TourListUiState {
    data object Loading : TourListUiState
    data class Success(
        val tours: List<Tour>,
        val selectedCategoryFilter: TourCategory? = null,
        val selectedStatusFilter: Boolean? = null,
        val searchQuery: String = ""
    ) : TourListUiState
    data class Error(val message: String) : TourListUiState
}

class TourListViewModel(
    private val getToursUseCase: GetToursUseCase,
    private val toggleTourStatusUseCase: ToggleTourStatusUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<TourListUiState>(TourListUiState.Loading)
    val uiState: StateFlow<TourListUiState> = _uiState.asStateFlow()

    private var tenantId: String = ""
    private var currentCategory: TourCategory? = null
    private var currentStatus: Boolean? = null
    private var currentSearch: String = ""

    init {
        loadTours()
    }

    fun loadTours() {
        viewModelScope.launch {
            _uiState.value = TourListUiState.Loading
            val currentUser = getCurrentUserUseCase()
            tenantId = currentUser?.tenantId ?: "tenant_id"
            fetchFilteredTours()
        }
    }

    private suspend fun fetchFilteredTours() {
        getToursUseCase.getTours(
            tenantId = tenantId,
            categoryFilter = currentCategory,
            statusFilter = currentStatus,
            searchQuery = currentSearch
        ).onSuccess { list ->
            _uiState.value = TourListUiState.Success(
                tours = list,
                selectedCategoryFilter = currentCategory,
                selectedStatusFilter = currentStatus,
                searchQuery = currentSearch
            )
        }.onFailure { exception ->
            val rawMsg = exception.message ?: ""
            val userFriendlyMsg = if (rawMsg.contains("infinite recursion") || rawMsg.contains("42P17") || rawMsg.contains("profiles")) {
                "Veritabanı RLS Politika Hatası (profiles tablosunda sonsuz döngü tespit edildi - Code: 42P17). Lütfen Supabase RLS kuralını güncelleyin."
            } else if (rawMsg.contains("Headers:") || rawMsg.contains("Http Method")) {
                "Tur kataloğu verileri yüklenirken sunucu erişim hatası oluştu."
            } else {
                rawMsg.ifBlank { "Turlar yüklenirken hata oluştu" }
            }
            _uiState.value = TourListUiState.Error(userFriendlyMsg)
        }
    }


    fun onCategoryFilterSelected(category: TourCategory?) {
        currentCategory = category
        viewModelScope.launch { fetchFilteredTours() }
    }

    fun onStatusFilterSelected(status: Boolean?) {
        currentStatus = status
        viewModelScope.launch { fetchFilteredTours() }
    }

    fun onSearchQueryChanged(query: String) {
        currentSearch = query
        viewModelScope.launch { fetchFilteredTours() }
    }

    fun onToggleTourStatus(tourId: String, currentIsActive: Boolean) {
        viewModelScope.launch {
            toggleTourStatusUseCase(tourId, !currentIsActive).onSuccess {
                fetchFilteredTours()
            }
        }
    }
}
