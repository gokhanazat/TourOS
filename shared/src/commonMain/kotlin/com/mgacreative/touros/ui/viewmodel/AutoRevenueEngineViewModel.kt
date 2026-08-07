package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.domain.model.Booking
import com.mgacreative.touros.domain.model.Invoice
import com.mgacreative.touros.domain.usecase.GetCurrentUserUseCase
import com.mgacreative.touros.domain.usecase.ProcessAutoRevenueUseCase
import com.mgacreative.touros.domain.repository.FinanceRepository
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
    val autoProcessedAt: String = "2026-08-06 12:00"
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
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<AutoRevenueUiState>(AutoRevenueUiState.Loading)
    val uiState: StateFlow<AutoRevenueUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = AutoRevenueUiState.Loading
            val user = getCurrentUserUseCase()
            val tenantId = user?.tenantId ?: "tenant_id"

            val invoicesRes = financeRepository.getInvoices(tenantId)
            val invoices = invoicesRes.getOrDefault(emptyList())

            val logs = if (invoices.isEmpty()) {
                listOf(
                    AutoRevenueLogItem("B-202608-001", "Hans Müller", 12000.0, "INV-B-202608-001", 10000.0, 2000.0, 12000.0, "issued", "2026-08-06 10:15"),
                    AutoRevenueLogItem("B-202608-002", "Sarah Jenkins", 24000.0, "INV-B-202608-002", 20000.0, 4000.0, 24000.0, "issued", "2026-08-06 11:30"),
                    AutoRevenueLogItem("B-202608-003", "Jean Dupont", 18000.0, "INV-B-202608-003", 15000.0, 3000.0, 18000.0, "issued", "2026-08-06 11:45")
                )
            } else {
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
                        autoProcessedAt = inv.issuedAt ?: "2026-08-06"
                    )
                }
            }

            val totalRev = logs.sumOf { it.totalAmount }
            val totalTax = logs.sumOf { it.taxAmount }

            _uiState.value = AutoRevenueUiState.Success(
                logs = logs,
                totalRevenue = totalRev,
                totalTaxCollected = totalTax
            )
        }
    }

    fun triggerAutoRevenueForBooking(bookingCode: String, customerName: String, amount: Double) {
        viewModelScope.launch {
            val user = getCurrentUserUseCase()
            val tenantId = user?.tenantId ?: "tenant_id"

            val booking = Booking(
                id = "b-new",
                bookingCode = bookingCode,
                customerName = customerName,
                totalPrice = amount,
                status = com.mgacreative.touros.domain.model.BookingStatus.ONAYLANDI,
                tenantId = tenantId
            )

            val res = processAutoRevenueUseCase(booking)
            res.onSuccess {
                loadData()
            }.onFailure { err ->
                _uiState.value = AutoRevenueUiState.Error(err.message ?: "Muhasebeleştirme başarısız.")
            }
        }
    }
}
