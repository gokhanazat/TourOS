package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.domain.model.Account
import com.mgacreative.touros.domain.model.Payment
import com.mgacreative.touros.domain.repository.FinanceRepository
import com.mgacreative.touros.domain.usecase.GetCurrentUserUseCase
import com.mgacreative.touros.domain.usecase.ProcessPartialPaymentUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BookingPaymentUiState(
    val bookingId: String = "b1",
    val bookingCode: String = "B-202608-001",
    val customerName: String = "Hans Müller",
    val totalPrice: Double = 24000.0,
    val totalPaid: Double = 6000.0,
    val remainingBalance: Double = 18000.0,
    val paymentStatus: String = "PARTIALLY_PAID", // PAID, PARTIALLY_PAID, UNPAID
    val selectedMethod: String = "cash", // cash, credit_card, bank_transfer, online
    val paymentHistory: List<Payment> = emptyList(),
    val accounts: List<Account> = emptyList(),
    val selectedAccountId: String? = null,
    val isLoading: Boolean = false,
    val notificationMessage: String? = null,
    val errorMessage: String? = null
)

class BookingPaymentViewModel(
    private val processPartialPaymentUseCase: ProcessPartialPaymentUseCase,
    private val financeRepository: FinanceRepository,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookingPaymentUiState())
    val uiState: StateFlow<BookingPaymentUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val user = getCurrentUserUseCase()
            val tenantId = user?.tenantId ?: "tenant_id"

            val accountsRes = financeRepository.getAccounts(tenantId)
            val accounts = accountsRes.getOrDefault(
                listOf(
                    Account("acc1", "Merkez Firma Kasası", "cash", "TRY", 150000.0, null, null, true, tenantId),
                    Account("acc2", "Garanti Bankası Ticari", "bank", "TRY", 450000.0, "TR33 0006 2000 0000 1234 56", "Garanti BBVA", true, tenantId),
                    Account("acc3", "İyzico Sanal POS", "pos", "TRY", 85000.0, null, null, true, tenantId)
                )
            )

            val history = listOf(
                Payment("p1", "inv1", "acc1", 6000.0, "TRY", "cash", "2026-08-01", "DEKONT-001", "Ön depozito tahsilatı (%25)", tenantId)
            )

            _uiState.value = _uiState.value.copy(
                accounts = accounts,
                selectedAccountId = accounts.firstOrNull()?.id,
                paymentHistory = history,
                isLoading = false
            )
        }
    }

    fun setPaymentMethod(method: String) {
        _uiState.value = _uiState.value.copy(selectedMethod = method)
    }

    fun setAccountId(accountId: String) {
        _uiState.value = _uiState.value.copy(selectedAccountId = accountId)
    }

    fun processPayment(amount: Double, referenceNo: String?, notes: String?) {
        viewModelScope.launch {
            val state = _uiState.value
            _uiState.value = state.copy(isLoading = true)

            val user = getCurrentUserUseCase()
            val tenantId = user?.tenantId ?: "tenant_id"

            val res = processPartialPaymentUseCase(
                bookingId = state.bookingId,
                paymentMethod = state.selectedMethod,
                amount = amount,
                accountId = state.selectedAccountId,
                referenceNo = referenceNo,
                notes = notes,
                tenantId = tenantId
            )

            res.onSuccess { summary ->
                val newPayment = Payment(
                    id = "p-new",
                    invoiceId = "inv1",
                    accountId = state.selectedAccountId,
                    amount = amount,
                    currency = "TRY",
                    paymentMethod = state.selectedMethod,
                    paymentDate = "2026-08-06",
                    referenceNo = referenceNo,
                    notes = notes,
                    tenantId = tenantId
                )

                val newTotalPaid = state.totalPaid + amount
                val newRemaining = (state.totalPrice - newTotalPaid).coerceAtLeast(0.0)
                val newStatus = if (newRemaining <= 0) "PAID" else "PARTIALLY_PAID"

                _uiState.value = state.copy(
                    totalPaid = newTotalPaid,
                    remainingBalance = newRemaining,
                    paymentStatus = newStatus,
                    paymentHistory = listOf(newPayment) + state.paymentHistory,
                    isLoading = false,
                    notificationMessage = "✅ ${amount} TRY tutarındaki (${state.selectedMethod.uppercase()}) ödeme başarıyla alındı ve kaydoldu."
                )
            }.onFailure { err ->
                _uiState.value = state.copy(
                    isLoading = false,
                    errorMessage = err.message ?: "Ödeme tahsilatı başarısız."
                )
            }
        }
    }
}
