package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.domain.model.ota.OTAAccount
import com.mgacreative.touros.domain.model.ota.OTABooking
import com.mgacreative.touros.domain.usecase.ota.ConnectOtaAccountUseCase
import com.mgacreative.touros.domain.usecase.ota.GetOtaBookingsUseCase
import com.mgacreative.touros.domain.usecase.ota.SyncOtaChannelUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OTAHubUiState(
    val isLoading: Boolean = false,
    val accounts: List<OTAAccount> = emptyList(),
    val bookings: List<OTABooking> = emptyList(),
    val syncStatusMessage: String? = null,
    val errorMessage: String? = null
)

class OTAHubViewModel(
    private val connectOtaAccountUseCase: ConnectOtaAccountUseCase,
    private val syncOtaChannelUseCase: SyncOtaChannelUseCase,
    private val getOtaBookingsUseCase: GetOtaBookingsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(OTAHubUiState())
    val uiState: StateFlow<OTAHubUiState> = _uiState.asStateFlow()

    fun connectChannel(account: OTAAccount, tenantId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = connectOtaAccountUseCase(account)
            result.onSuccess { connection ->
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        syncStatusMessage = "${account.providerId.uppercase()} hesabı başarıyla bağlandı. Bağlantı ID: ${connection.connectionId}"
                    )
                }
                loadBookings(account.accountId, tenantId)
            }.onFailure { err ->
                _uiState.update { it.copy(isLoading = false, errorMessage = err.message) }
            }
        }
    }

    fun syncNow(accountId: String, isFullSync: Boolean, tenantId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = syncOtaChannelUseCase(accountId, isFullSync, tenantId)
            result.onSuccess { bookings ->
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        bookings = bookings,
                        syncStatusMessage = "Kanal senkronizasyonu tamamlandı (${bookings.size} rezervasyon)."
                    )
                }
            }.onFailure { err ->
                _uiState.update { it.copy(isLoading = false, errorMessage = err.message) }
            }
        }
    }

    fun loadBookings(accountId: String = "acc-001", tenantId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = getOtaBookingsUseCase(accountId, tenantId)
            result.onSuccess { list ->
                _uiState.update { it.copy(isLoading = false, bookings = list) }
            }.onFailure { err ->
                _uiState.update { it.copy(isLoading = false, errorMessage = err.message) }
            }
        }
    }
}
