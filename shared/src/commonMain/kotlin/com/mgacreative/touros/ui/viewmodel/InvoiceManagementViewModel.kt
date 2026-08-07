package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.domain.model.Invoice
import com.mgacreative.touros.domain.repository.FinanceRepository
import com.mgacreative.touros.domain.usecase.CreateInvoiceUseCase
import com.mgacreative.touros.domain.usecase.ExportInvoicePdfUseCase
import com.mgacreative.touros.domain.usecase.GetCurrentUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface InvoiceManagementUiState {
    data object Loading : InvoiceManagementUiState
    data class Success(
        val invoices: List<Invoice> = emptyList(),
        val isCreatingInvoice: Boolean = false,
        val notificationMessage: String? = null,
        val exportedPdfUrl: String? = null
    ) : InvoiceManagementUiState
    data class Error(val message: String) : InvoiceManagementUiState
}

class InvoiceManagementViewModel(
    private val financeRepository: FinanceRepository,
    private val createInvoiceUseCase: CreateInvoiceUseCase,
    private val exportInvoicePdfUseCase: ExportInvoicePdfUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<InvoiceManagementUiState>(InvoiceManagementUiState.Loading)
    val uiState: StateFlow<InvoiceManagementUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = InvoiceManagementUiState.Loading
            val user = getCurrentUserUseCase()
            val tenantId = user?.tenantId ?: "tenant_id"

            val res = financeRepository.getInvoices(tenantId)
            val fetched = res.getOrDefault(emptyList())

            val invoices = if (fetched.isEmpty()) {
                listOf(
                    Invoice("inv1", "INV-B-202608-001", "b1", "sale", "Hans Müller", "1234567890", 10000.0, 20.0, 2000.0, 12000.0, "TRY", "issued", "2026-08-06", "2026-08-13", "Kapadokya Tur Satış Faturası", tenantId),
                    Invoice("inv2", "INV-B-202608-002", "b2", "sale", "Sarah Jenkins", "9876543210", 20000.0, 20.0, 4000.0, 24000.0, "TRY", "paid", "2026-08-05", "2026-08-12", "Ege Turu Satış Faturası", tenantId)
                )
            } else {
                fetched
            }

            _uiState.value = InvoiceManagementUiState.Success(invoices = invoices)
        }
    }

    fun createNewInvoice(
        invoiceNo: String,
        customerName: String,
        customerTaxNo: String?,
        totalAmount: Double,
        notes: String?
    ) {
        viewModelScope.launch {
            val user = getCurrentUserUseCase()
            val tenantId = user?.tenantId ?: "tenant_id"

            val subtotal = ((totalAmount / 1.20) * 100).toInt() / 100.0
            val taxAmount = ((totalAmount - subtotal) * 100).toInt() / 100.0

            val invoice = Invoice(
                invoiceNo = invoiceNo,
                customerName = customerName,
                customerTaxNo = customerTaxNo,
                subtotal = subtotal,
                taxRate = 20.0,
                taxAmount = taxAmount,
                totalAmount = totalAmount,
                currency = "TRY",
                status = "draft",
                notes = notes,
                tenantId = tenantId
            )

            val res = createInvoiceUseCase(invoice)
            res.onSuccess {
                loadData()
            }.onFailure { err ->
                _uiState.value = InvoiceManagementUiState.Error(err.message ?: "Fatura oluşturulamadı.")
            }
        }
    }

    fun exportInvoicePdf(invoice: Invoice) {
        viewModelScope.launch {
            val currentState = _uiState.value as? InvoiceManagementUiState.Success ?: return@launch

            val res = exportInvoicePdfUseCase(invoice)
            res.onSuccess { pdfUrl ->
                val updatedInvoices = currentState.invoices.map {
                    if (it.id == invoice.id) it.copy(status = "issued") else it
                }
                _uiState.value = currentState.copy(
                    invoices = updatedInvoices,
                    notificationMessage = "📄 Fatura (${invoice.invoiceNo}) PDF olarak dışa aktarıldı ve Belge Yönetimi'ne kaydedildi.",
                    exportedPdfUrl = pdfUrl
                )
            }.onFailure { err ->
                _uiState.value = InvoiceManagementUiState.Error(err.message ?: "PDF aktarımı başarısız.")
            }
        }
    }
}
