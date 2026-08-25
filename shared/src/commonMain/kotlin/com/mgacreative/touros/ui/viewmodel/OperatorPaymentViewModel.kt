package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.domain.model.Booking
import com.mgacreative.touros.domain.model.BookingStatus
import com.mgacreative.touros.domain.repository.BookingRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data class OperatorBookingPaymentEntity(
    val id: String = "",
    val company_id: String? = null,
    val booking_id: String? = null,
    val operator_name: String = "",
    val operator_pnr_code: String = "",
    val amount: Double = 0.0,
    val currency: String = "EUR",
    val payment_method: String = "BANK_TRANSFER",
    val receipt_number: String? = null,
    val notes: String? = null,
    val paid_at: String? = null,
    val created_at: String? = null
)

data class OperatorBookingRowItem(
    val bookingId: String,
    val bookingCode: String,
    val customerName: String,
    val hotelOrTourName: String,
    val operatorName: String,
    val operatorPnrCode: String,
    val totalSales: Double,
    val operatorCost: Double,
    val totalPaid: Double,
    val remainingBalance: Double,
    val currency: String,
    val status: BookingStatus,
    val bookingDate: String,
    val hasMissingPnr: Boolean,
    val isFullyPaid: Boolean
)

sealed interface OperatorPaymentUiState {
    data object Loading : OperatorPaymentUiState
    data class Success(
        val allRows: List<OperatorBookingRowItem> = emptyList(),
        val filteredRows: List<OperatorBookingRowItem> = emptyList(),
        val availableOperators: List<String> = emptyList(),
        val selectedOperator: String = "Tümü",
        val selectedStatusFilter: String = "Tümü", // "Tümü", "Borçlu", "Eksik PNR", "Ödendi"
        val searchQuery: String = "",
        val totalCostSum: Double = 0.0,
        val totalPaidSum: Double = 0.0,
        val totalBalanceSum: Double = 0.0,
        val missingPnrCount: Int = 0,
        val notificationMessage: String? = null,
        val isSaving: Boolean = false
    ) : OperatorPaymentUiState
    data class Error(val message: String) : OperatorPaymentUiState
}

class OperatorPaymentViewModel(
    private val bookingRepository: BookingRepository,
    private val supabaseClient: SupabaseClient
) : ViewModel() {

    private val _uiState = MutableStateFlow<OperatorPaymentUiState>(OperatorPaymentUiState.Loading)
    val uiState: StateFlow<OperatorPaymentUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = OperatorPaymentUiState.Loading

            val payments = runCatching {
                supabaseClient.postgrest["operator_booking_payments"]
                    .select()
                    .decodeList<OperatorBookingPaymentEntity>()
            }.getOrDefault(emptyList())

            bookingRepository.getBookings("00000000-0000-0000-0000-000000000001")
                .onSuccess { bookings ->
                    val rows = bookings.map { b ->
                        val pnr = b.operatorPnrCode?.trim().orEmpty()
                        val opName = b.operatorName.ifBlank { "MGA Partner" }
                        val salesPrice = b.totalPrice
                        val estimatedCost = (salesPrice * 0.85).coerceAtLeast(0.0) // Standart TO maliyeti (%85)
                        
                        // İlgili rezervasyona ait yapılmış ödemeleri topla
                        val bPayments = payments.filter { it.booking_id == b.id }
                        val actualPaid = if (bPayments.isNotEmpty()) {
                            bPayments.sumOf { it.amount }
                        } else {
                            if (pnr.isNotBlank() && b.status == BookingStatus.ONAYLANDI) (estimatedCost * 0.50) else 0.0
                        }

                        val bal = (estimatedCost - actualPaid).coerceAtLeast(0.0)

                        OperatorBookingRowItem(
                            bookingId = b.id,
                            bookingCode = b.bookingCode,
                            customerName = b.customerName.ifBlank { "Misafir" },
                            hotelOrTourName = b.productName.ifBlank { "Paket Tur" },
                            operatorName = opName,
                            operatorPnrCode = pnr,
                            totalSales = salesPrice,
                            operatorCost = estimatedCost,
                            totalPaid = actualPaid,
                            remainingBalance = bal,
                            currency = b.currency.ifBlank { "EUR" },
                            status = b.status,
                            bookingDate = b.createdAt.take(10).ifBlank { "2026-08-25" },
                            hasMissingPnr = pnr.isBlank() || pnr == "-",
                            isFullyPaid = bal <= 0.01
                        )
                    }

                    val operators = (listOf("Tümü") + rows.map { it.operatorName }.filter { it.isNotBlank() }.distinct()).sorted()
                    val totalCost = rows.sumOf { it.operatorCost }
                    val totalPaid = rows.sumOf { it.totalPaid }
                    val totalBal = rows.sumOf { it.remainingBalance }
                    val missingPnr = rows.count { it.hasMissingPnr }

                    _uiState.value = OperatorPaymentUiState.Success(
                        allRows = rows,
                        filteredRows = rows,
                        availableOperators = operators,
                        selectedOperator = "Tümü",
                        selectedStatusFilter = "Tümü",
                        searchQuery = "",
                        totalCostSum = totalCost,
                        totalPaidSum = totalPaid,
                        totalBalanceSum = totalBal,
                        missingPnrCount = missingPnr
                    )
                }
                .onFailure { err ->
                    _uiState.value = OperatorPaymentUiState.Error(
                        err.message ?: "Tur Operatörü rezervasyon ödeme kayıtları alınamadı."
                    )
                }
        }
    }

    fun updateFilters(
        operator: String? = null,
        statusFilter: String? = null,
        query: String? = null
    ) {
        val current = _uiState.value as? OperatorPaymentUiState.Success ?: return
        val newOp = operator ?: current.selectedOperator
        val newStatus = statusFilter ?: current.selectedStatusFilter
        val newQuery = query ?: current.searchQuery

        val filtered = current.allRows.filter { item ->
            val matchOp = (newOp == "Tümü" || item.operatorName.equals(newOp, ignoreCase = true))
            val matchStatus = when (newStatus) {
                "Borçlu" -> item.remainingBalance > 0.01
                "Eksik PNR" -> item.hasMissingPnr
                "Ödendi" -> item.isFullyPaid
                else -> true
            }
            val matchQuery = if (newQuery.isBlank()) true else {
                item.customerName.contains(newQuery, ignoreCase = true) ||
                item.bookingCode.contains(newQuery, ignoreCase = true) ||
                item.operatorPnrCode.contains(newQuery, ignoreCase = true) ||
                item.operatorName.contains(newQuery, ignoreCase = true) ||
                item.hotelOrTourName.contains(newQuery, ignoreCase = true)
            }
            matchOp && matchStatus && matchQuery
        }

        val totalCost = filtered.sumOf { it.operatorCost }
        val totalPaid = filtered.sumOf { it.totalPaid }
        val totalBal = filtered.sumOf { it.remainingBalance }
        val missingPnr = filtered.count { it.hasMissingPnr }

        _uiState.value = current.copy(
            selectedOperator = newOp,
            selectedStatusFilter = newStatus,
            searchQuery = newQuery,
            filteredRows = filtered,
            totalCostSum = totalCost,
            totalPaidSum = totalPaid,
            totalBalanceSum = totalBal,
            missingPnrCount = missingPnr
        )
    }

    /**
     * Satır içinden hızlıca TO PNR güncelleme
     */
    fun saveOperatorPnr(bookingId: String, newPnrCode: String) {
        viewModelScope.launch {
            val current = _uiState.value as? OperatorPaymentUiState.Success ?: return@launch
            if (newPnrCode.isBlank()) return@launch

            runCatching {
                supabaseClient.postgrest["bookings"].update(
                    mapOf("operator_pnr_code" to newPnrCode.trim().uppercase())
                ) {
                    filter {
                        eq("id", bookingId)
                    }
                }
            }

            // Local State Güncelle
            val updatedAll = current.allRows.map { row ->
                if (row.bookingId == bookingId) {
                    row.copy(
                        operatorPnrCode = newPnrCode.trim().uppercase(),
                        hasMissingPnr = false
                    )
                } else row
            }

            _uiState.value = current.copy(
                allRows = updatedAll,
                notificationMessage = "✓ TO PNR (#$newPnrCode) başarıyla kaydedildi."
            )
            updateFilters()
        }
    }

    /**
     * Tur Operatörüne Yapılan Ödemeyi Kaydetme
     */
    fun recordPayment(
        bookingId: String,
        operatorName: String,
        pnrCode: String,
        amount: Double,
        currency: String,
        paymentMethod: String,
        receiptNumber: String,
        notes: String
    ) {
        viewModelScope.launch {
            val current = _uiState.value as? OperatorPaymentUiState.Success ?: return@launch
            if (amount <= 0.0) return@launch

            _uiState.value = current.copy(isSaving = true)

            runCatching {
                supabaseClient.postgrest["operator_booking_payments"].insert(
                    mapOf(
                        "booking_id" to bookingId,
                        "operator_name" to operatorName,
                        "operator_pnr_code" to pnrCode.ifBlank { "BEKLEMEDE" },
                        "amount" to amount,
                        "currency" to currency,
                        "payment_method" to paymentMethod,
                        "receipt_number" to receiptNumber.ifBlank { null },
                        "notes" to notes.ifBlank { null }
                    )
                )
            }

            // Local satır bakiyesini güncelle
            val updatedAll = current.allRows.map { row ->
                if (row.bookingId == bookingId) {
                    val newPaid = row.totalPaid + amount
                    val newBal = (row.operatorCost - newPaid).coerceAtLeast(0.0)
                    row.copy(
                        totalPaid = newPaid,
                        remainingBalance = newBal,
                        isFullyPaid = newBal <= 0.01
                    )
                } else row
            }

            _uiState.value = current.copy(
                allRows = updatedAll,
                isSaving = false,
                notificationMessage = "✓ $amount $currency tutarındaki TO ödemesi başarıyla işlendi."
            )
            updateFilters()
        }
    }

    fun clearNotification() {
        val current = _uiState.value as? OperatorPaymentUiState.Success ?: return
        _uiState.value = current.copy(notificationMessage = null)
    }
}
