package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.domain.model.B2CCustomerVoucherItem
import com.mgacreative.touros.domain.model.B2CFavoriteTourItem
import com.mgacreative.touros.domain.usecase.GetB2CCustomerVouchersUseCase
import com.mgacreative.touros.domain.usecase.GetCurrentUserUseCase
import com.mgacreative.touros.domain.usecase.ToggleB2CFavoriteTourUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class B2CVoucherFavoritesUiState(
    val selectedTab: Int = 0, // 0: Vouchers, 1: Favoriler
    val vouchers: List<B2CCustomerVoucherItem> = emptyList(),
    val favoriteTours: List<B2CFavoriteTourItem> = listOf(
        B2CFavoriteTourItem("t101", "Kapadokya Balon & Vadi Turu", "Kültür Turu", 2500.0, 4.90, true),
        B2CFavoriteTourItem("t103", "Karadeniz Yaylalar & Doğa Gezisi", "Doğa & Yayla", 3200.0, 4.95, true)
    ),
    val isLoading: Boolean = false,
    val notificationMessage: String? = null,
    val errorMessage: String? = null
)

class B2CVoucherFavoritesViewModel(
    private val getB2CCustomerVouchersUseCase: GetB2CCustomerVouchersUseCase,
    private val toggleB2CFavoriteTourUseCase: ToggleB2CFavoriteTourUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(B2CVoucherFavoritesUiState())
    val uiState: StateFlow<B2CVoucherFavoritesUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val user = getCurrentUserUseCase()
            val tenantId = user?.tenantId ?: "tenant_id"

            val res = getB2CCustomerVouchersUseCase(tenantId)
            res.onSuccess { list ->
                _uiState.value = _uiState.value.copy(
                    vouchers = list,
                    isLoading = false
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = err.message
                )
            }
        }
    }

    fun selectTab(tab: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun toggleFavorite(tourId: String) {
        viewModelScope.launch {
            val user = getCurrentUserUseCase()
            val tenantId = user?.tenantId ?: "tenant_id"

            val res = toggleB2CFavoriteTourUseCase(tourId, tenantId)
            res.onSuccess { result ->
                val updatedFavorites = _uiState.value.favoriteTours.filter { it.tourId != tourId }
                _uiState.value = _uiState.value.copy(
                    favoriteTours = updatedFavorites,
                    notificationMessage = result.message
                )
            }
        }
    }
}
