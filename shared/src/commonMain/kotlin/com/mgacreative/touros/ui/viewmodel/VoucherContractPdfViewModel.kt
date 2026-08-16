package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.domain.engine.VoucherContractTemplateEngine
import com.mgacreative.touros.domain.model.Booking
import com.mgacreative.touros.domain.model.GeneratedDocument
import com.mgacreative.touros.domain.usecase.GenerateVoucherOrContractPdfUseCase
import com.mgacreative.touros.domain.usecase.GetBookingsUseCase
import com.mgacreative.touros.domain.usecase.GetCurrentUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class VoucherContractPdfUiState(
    val bookings: List<Booking> = emptyList(),
    val selectedBooking: Booking? = null,
    val previewHtmlContent: String = "",
    val generatedDocument: GeneratedDocument? = null,
    val isLoading: Boolean = false,
    val notificationMessage: String? = null,
    val errorMessage: String? = null
)

class VoucherContractPdfViewModel(
    private val getBookingsUseCase: GetBookingsUseCase,
    private val generateVoucherOrContractPdfUseCase: GenerateVoucherOrContractPdfUseCase,
    private val templateEngine: VoucherContractTemplateEngine,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(VoucherContractPdfUiState())
    val uiState: StateFlow<VoucherContractPdfUiState> = _uiState.asStateFlow()

    init {
        loadBookings()
    }

    fun loadBookings() {
        viewModelScope.launch {
            val user = getCurrentUserUseCase()
            val tenantId = user?.tenantId ?: "tenant_id"
            val res = getBookingsUseCase.getBookings(tenantId)
            val list = res.getOrDefault(emptyList())
            val first = list.firstOrNull() ?: Booking("b101", customerName = "Hans Müller", totalPrice = 18500.0, currency = "TRY")
            val tourTitle = first.items.firstOrNull()?.description ?: "Kapadokya Balon & Vadi Turu"

            _uiState.value = _uiState.value.copy(
                bookings = if (list.isEmpty()) listOf(first) else list,
                selectedBooking = first,
                previewHtmlContent = templateEngine.buildVoucherHtmlTemplate(first.id, first.customerName, tourTitle, "Cave Hotel & Spa", "15.08.2026", 2),
                isLoading = false
            )
        }
    }

    fun selectBooking(booking: Booking) {
        val tourTitle = booking.items.firstOrNull()?.description ?: "Kapadokya Balon & Vadi Turu"
        _uiState.value = _uiState.value.copy(
            selectedBooking = booking,
            previewHtmlContent = templateEngine.buildVoucherHtmlTemplate(booking.id, booking.customerName, tourTitle, "Lüks Konaklama Oteli", "15.08.2026", 2)
        )
    }

    fun generatePdf(docType: String) {
        viewModelScope.launch {
            val booking = _uiState.value.selectedBooking ?: return@launch
            _uiState.value = _uiState.value.copy(isLoading = true)

            val user = getCurrentUserUseCase()
            val tenantId = user?.tenantId ?: "tenant_id"
            val tourTitle = booking.items.firstOrNull()?.description ?: "Kapadokya Balon & Vadi Turu"

            val previewHtml = if (docType == "contract") {
                templateEngine.buildContractHtmlTemplate(booking.id, booking.customerName, tourTitle, booking.totalPrice, booking.currency)
            } else {
                templateEngine.buildVoucherHtmlTemplate(booking.id, booking.customerName, tourTitle, "Lüks Konaklama Oteli", "15.08.2026", 2)
            }

            val res = generateVoucherOrContractPdfUseCase(booking.id, docType, tenantId)
            res.onSuccess { doc ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    generatedDocument = doc,
                    previewHtmlContent = previewHtml,
                    notificationMessage = "✅ Otomatik ${if (docType == "contract") "Paket Tur Sözleşmesi" else "Seyahat Voucher"} PDF'i Oluşturuldu!"
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = err.message ?: "PDF üretme hatası."
                )
            }
        }
    }
}
