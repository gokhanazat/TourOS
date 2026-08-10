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

    private fun resolveTenantId(userTenantId: String?): String {
        val tid = userTenantId?.trim()
        return if (!tid.isNullOrBlank() && tid != "tenant_id") tid else "00000000-0000-0000-0000-000000000001"
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = InvoiceManagementUiState.Loading
            val user = getCurrentUserUseCase()
            val tenantId = resolveTenantId(user?.tenantId)

            val res = financeRepository.getInvoices(tenantId)
            val invoices = res.getOrDefault(emptyList()).filter { it.status != "deleted" }

            _uiState.value = InvoiceManagementUiState.Success(invoices = invoices)
        }
    }

    fun createNewInvoice(
        invoiceNo: String,
        invoiceType: String,
        customerName: String,
        customerTaxNo: String?,
        totalAmount: Double,
        notes: String?
    ) {
        viewModelScope.launch {
            val currentState = _uiState.value as? InvoiceManagementUiState.Success ?: return@launch
            _uiState.value = currentState.copy(isCreatingInvoice = true, notificationMessage = null)

            val user = getCurrentUserUseCase()
            val tenantId = resolveTenantId(user?.tenantId)

            val subtotal = ((totalAmount / 1.20) * 100).toInt() / 100.0
            val taxAmount = ((totalAmount - subtotal) * 100).toInt() / 100.0

            val invoice = Invoice(
                invoiceNo = invoiceNo,
                invoiceType = invoiceType,
                customerName = customerName,
                customerTaxNo = customerTaxNo,
                subtotal = subtotal,
                taxRate = 20.0,
                taxAmount = taxAmount,
                totalAmount = totalAmount,
                currency = "TRY",
                status = "issued",
                notes = notes,
                tenantId = tenantId
            )

            val res = createInvoiceUseCase(invoice)
            res.onSuccess {
                val updatedRes = financeRepository.getInvoices(tenantId)
                val updatedInvoices = updatedRes.getOrDefault(emptyList()).filter { it.status != "deleted" }
                val typeLabel = if (invoiceType == "purchase") "Gider Faturası" else "Satış Gelir Faturası"
                _uiState.value = InvoiceManagementUiState.Success(
                    invoices = updatedInvoices,
                    isCreatingInvoice = false,
                    notificationMessage = "✅ $typeLabel (${invoice.invoiceNo}) veritabanına kaydedildi!"
                )
            }.onFailure { err ->
                _uiState.value = InvoiceManagementUiState.Error(err.message ?: "Fatura kaydı başarısız oldu.")
            }
        }
    }

    fun cancelInvoice(invoiceId: String) {
        viewModelScope.launch {
            val user = getCurrentUserUseCase()
            val tenantId = resolveTenantId(user?.tenantId)
            financeRepository.updateInvoiceStatus(invoiceId, "canceled")
            val updatedRes = financeRepository.getInvoices(tenantId)
            val updatedInvoices = updatedRes.getOrDefault(emptyList()).filter { it.status != "deleted" }
            _uiState.value = InvoiceManagementUiState.Success(
                invoices = updatedInvoices,
                notificationMessage = "🚫 Fatura başarıyla iptal edildi."
            )
        }
    }

    fun deleteInvoice(invoiceId: String) {
        viewModelScope.launch {
            val user = getCurrentUserUseCase()
            val tenantId = resolveTenantId(user?.tenantId)
            financeRepository.updateInvoiceStatus(invoiceId, "deleted")
            val updatedRes = financeRepository.getInvoices(tenantId)
            val updatedInvoices = updatedRes.getOrDefault(emptyList()).filter { it.status != "deleted" }
            _uiState.value = InvoiceManagementUiState.Success(
                invoices = updatedInvoices,
                notificationMessage = "🗑️ Fatura kaydı veritabanından silindi."
            )
        }
    }

    fun exportInvoicePdf(invoice: Invoice) {
        viewModelScope.launch {
            val currentState = _uiState.value as? InvoiceManagementUiState.Success ?: return@launch
            val res = exportInvoicePdfUseCase(invoice)
            res.onSuccess { url ->
                _uiState.value = currentState.copy(
                    exportedPdfUrl = url,
                    notificationMessage = "📄 PDF Dışa Aktarıldı: $url"
                )
            }.onFailure { err ->
                _uiState.value = currentState.copy(
                    notificationMessage = "PDF Oluşturma Uyarısı: ${err.message}"
                )
            }
        }
    }
}
