package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.domain.model.Booking
import com.mgacreative.touros.domain.model.BookingStatus
import com.mgacreative.touros.domain.model.Invoice
import com.mgacreative.touros.domain.repository.BookingRepository
import com.mgacreative.touros.domain.repository.FinanceRepository
import com.mgacreative.touros.domain.usecase.GetCurrentUserUseCase
import com.mgacreative.touros.domain.usecase.ProcessAutoRevenueUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AutoRevenueLogItem(
    val bookingCode: String,
    val customerName: String,
    val bookingAmount: Double,
    val invoiceNo: String,
    val subtotal: Double,
    val taxAmount: Double,
    val totalAmount: Double,
    val status: String = "issued", // issued, draft, cancelled
    val autoProcessedAt: String = "Bugün"
)

sealed interface AutoRevenueUiState {
    data object Loading : AutoRevenueUiState
    data class Success(
        val logs: List<AutoRevenueLogItem> = emptyList(),
        val totalRevenue: Double = 0.0,
        val totalTaxCollected: Double = 0.0,
        val notificationMessage: String? = null
    ) : AutoRevenueUiState
    data class Error(val message: String) : AutoRevenueUiState
}

class AutoRevenueEngineViewModel(
    private val processAutoRevenueUseCase: ProcessAutoRevenueUseCase,
    private val financeRepository: FinanceRepository,
    private val bookingRepository: BookingRepository,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<AutoRevenueUiState>(AutoRevenueUiState.Loading)
    val uiState: StateFlow<AutoRevenueUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun resolveTenantId(userTenantId: String?): String {
        val tid = userTenantId?.trim()
        return if (!tid.isNullOrBlank() && tid != "tenant_id") tid else "00000000-0000-0000-0000-000000000001"
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = AutoRevenueUiState.Loading
            val user = getCurrentUserUseCase()
            val tenantId = resolveTenantId(user?.tenantId)

            val invoicesRes = financeRepository.getInvoices(tenantId)
            val invoices = invoicesRes.getOrDefault(emptyList())

            val logs = if (invoices.isNotEmpty()) {
                invoices.map { inv ->
                    AutoRevenueLogItem(
                        bookingCode = inv.invoiceNo.removePrefix("INV-"),
                        customerName = inv.customerName,
                        bookingAmount = inv.totalAmount,
                        invoiceNo = inv.invoiceNo,
                        subtotal = inv.subtotal,
                        taxAmount = inv.taxAmount,
                        totalAmount = inv.totalAmount,
                        status = inv.status,
                        autoProcessedAt = inv.issuedAt?.take(10) ?: "Bugün"
                    )
                }
            } else {
                val bookings = bookingRepository.getBookings(tenantId).getOrDefault(emptyList())
                bookings.map { b ->
                    val tax = b.totalPrice * 0.20
                    val subtotal = b.totalPrice - tax
                    val isCanceled = b.status == BookingStatus.IPTAL
                    AutoRevenueLogItem(
                        bookingCode = b.bookingCode.ifBlank { "B-2026-001" },
                        customerName = b.customerName.ifBlank { "Bilinmeyen Müşteri" },
                        bookingAmount = b.totalPrice,
                        invoiceNo = "INV-${b.bookingCode.ifBlank { "B-2026-001" }}",
                        subtotal = (subtotal * 100).toLong() / 100.0,
                        taxAmount = (tax * 100).toLong() / 100.0,
                        totalAmount = b.totalPrice,
                        status = if (isCanceled) "cancelled" else "issued",
                        autoProcessedAt = b.createdAt.take(10).ifBlank { "Bugün" }
                    )
                }
            }

            val totalRev = logs.filter { item -> item.status != "cancelled" && item.status != "canceled" }.sumOf { item -> item.totalAmount }
            val totalTax = logs.filter { item -> item.status != "cancelled" && item.status != "canceled" }.sumOf { item -> item.taxAmount }

            _uiState.value = AutoRevenueUiState.Success(
                logs = logs,
                totalRevenue = totalRev,
                totalTaxCollected = totalTax
            )
        }
    }

    fun processSingleBookingRevenue(booking: Booking) {
        viewModelScope.launch {
            val state = _uiState.value as? AutoRevenueUiState.Success ?: return@launch
            val res = processAutoRevenueUseCase(booking)
            res.onSuccess { invoice ->
                val newLog = AutoRevenueLogItem(
                    bookingCode = booking.bookingCode,
                    customerName = booking.customerName,
                    bookingAmount = booking.totalPrice,
                    invoiceNo = invoice.invoiceNo,
                    subtotal = invoice.subtotal,
                    taxAmount = invoice.taxAmount,
                    totalAmount = invoice.totalAmount,
                    status = invoice.status,
                    autoProcessedAt = invoice.issuedAt?.take(10) ?: "Bugün"
                )
                val updatedLogs = listOf(newLog) + state.logs
                _uiState.value = state.copy(
                    logs = updatedLogs,
                    totalRevenue = state.totalRevenue + invoice.totalAmount,
                    totalTaxCollected = state.totalTaxCollected + invoice.taxAmount,
                    notificationMessage = "⚡ ${booking.bookingCode} için Otomatik Satış Faturası (${invoice.invoiceNo}) oluşturuldu."
                )
            }.onFailure { err ->
                _uiState.value = AutoRevenueUiState.Error(err.message ?: "Otomatik fatura oluşturma başarısız.")
            }
        }
    }
}
