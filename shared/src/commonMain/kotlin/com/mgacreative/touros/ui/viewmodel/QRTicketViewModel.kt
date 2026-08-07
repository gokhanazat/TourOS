package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.domain.model.B2CQRTicket
import com.mgacreative.touros.domain.model.QRCheckInResult
import com.mgacreative.touros.domain.usecase.GenerateQRTicketUseCase
import com.mgacreative.touros.domain.usecase.GetCurrentUserUseCase
import com.mgacreative.touros.domain.usecase.ScanValidateQRTicketUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class QRTicketUiState(
    val selectedTab: Int = 0, // 0: Bilet Gösterimi, 1: Tarayıcı (Giriş Kontrolü)
    val qrTicket: B2CQRTicket = B2CQRTicket(),
    val checkInResult: QRCheckInResult? = null,
    val isLoading: Boolean = false,
    val notificationMessage: String? = null,
    val errorMessage: String? = null
)

class QRTicketViewModel(
    private val generateQRTicketUseCase: GenerateQRTicketUseCase,
    private val scanValidateQRTicketUseCase: ScanValidateQRTicketUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(QRTicketUiState())
    val uiState: StateFlow<QRTicketUiState> = _uiState.asStateFlow()

    fun loadTicket(bookingId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val user = getCurrentUserUseCase()
            val tenantId = user?.tenantId ?: "tenant_id"

            val res = generateQRTicketUseCase(bookingId, tenantId)
            res.onSuccess { ticket ->
                _uiState.value = _uiState.value.copy(
                    qrTicket = ticket,
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

    fun scanQRCode(qrData: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val user = getCurrentUserUseCase()
            val tenantId = user?.tenantId ?: "tenant_id"

            val res = scanValidateQRTicketUseCase(qrData, tenantId)
            res.onSuccess { result ->
                val updatedTicket = _uiState.value.qrTicket.copy(
                    checkinStatus = "CHECKED_IN",
                    checkedInAt = result.checkinTime
                )
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    qrTicket = updatedTicket,
                    checkInResult = result,
                    notificationMessage = result.message
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = err.message ?: "QR Bilet doğrulanamadı."
                )
            }
        }
    }
}
