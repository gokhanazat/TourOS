package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.domain.model.Booking
import com.mgacreative.touros.domain.model.BookingStatus
import com.mgacreative.touros.domain.model.BookingStatusLog
import com.mgacreative.touros.domain.usecase.GetBookingDetailUseCase
import com.mgacreative.touros.domain.usecase.UpdateBookingStatusUseCase
import com.mgacreative.touros.domain.repository.BookingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface BookingDetailUiState {
    data object Loading : BookingDetailUiState
    data class Success(
        val booking: Booking,
        val statusLogs: List<BookingStatusLog> = emptyList(),
        val selectedTab: Int = 0, // 0: Yolcular & Hizmetler, 1: Ödeme Özeti, 2: Durum Geçmişi
        val isDeleted: Boolean = false
    ) : BookingDetailUiState
    data class Error(val message: String) : BookingDetailUiState
}

class BookingDetailViewModel(
    private val getBookingDetailUseCase: GetBookingDetailUseCase,
    private val updateBookingStatusUseCase: UpdateBookingStatusUseCase,
    private val bookingRepository: BookingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<BookingDetailUiState>(BookingDetailUiState.Loading)
    val uiState: StateFlow<BookingDetailUiState> = _uiState.asStateFlow()

    private var currentBookingId: String = ""

    fun loadBooking(bookingId: String) {
        currentBookingId = bookingId
        viewModelScope.launch {
            _uiState.value = BookingDetailUiState.Loading
            getBookingDetailUseCase(bookingId)
                .onSuccess { result ->
                    _uiState.value = BookingDetailUiState.Success(
                        booking = result.booking,
                        statusLogs = result.statusLogs
                    )
                }
                .onFailure { err ->
                    _uiState.value = BookingDetailUiState.Error(
                        err.message ?: "Rezervasyon detayları alınamadı"
                    )
                }
        }
    }

    fun selectTab(tabIndex: Int) {
        val currentState = _uiState.value
        if (currentState is BookingDetailUiState.Success) {
            _uiState.value = currentState.copy(selectedTab = tabIndex)
        }
    }

    fun updateStatus(targetStatus: BookingStatus) {
        val currentState = _uiState.value
        if (currentState is BookingDetailUiState.Success) {
            viewModelScope.launch {
                updateBookingStatusUseCase(currentBookingId, currentState.booking.status, targetStatus)
                    .onSuccess {
                        if (targetStatus == BookingStatus.IPTAL) {
                            _uiState.value = currentState.copy(isDeleted = true)
                        } else {
                            loadBooking(currentBookingId)
                        }
                    }
            }
        }
    }

    fun updateOperatorPnr(pnrCode: String) {
        if (pnrCode.isBlank()) return
        viewModelScope.launch {
            bookingRepository.updateOperatorPnr(currentBookingId, pnrCode.trim().uppercase(), "ONAYLANDI")
                .onSuccess {
                    loadBooking(currentBookingId)
                }
        }
    }
}
