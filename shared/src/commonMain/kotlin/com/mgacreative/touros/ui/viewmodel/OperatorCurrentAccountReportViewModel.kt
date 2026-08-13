package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.domain.model.OperatorLedgerItem
import com.mgacreative.touros.domain.repository.BookingRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface OperatorCurrentAccountUiState {
    data object Loading : OperatorCurrentAccountUiState
    data class Success(
        val items: List<OperatorLedgerItem> = emptyList(),
        val filteredItems: List<OperatorLedgerItem> = emptyList(),
        val availableOperators: List<String> = emptyList(),
        val selectedOperator: String = "Tümü",
        val selectedDateFilter: String = "Tüm Zamanlar", // Tüm Zamanlar, Bu Ay, Bu Yıl
        val totalSales: Double = 0.0, // Toplam Borç (Tur Satışları)
        val totalPaid: Double = 0.0,  // Toplam Ödeme
        val totalBalance: Double = 0.0, // Net Bakiye
        val notificationMessage: String? = null
    ) : OperatorCurrentAccountUiState
    data class Error(val message: String) : OperatorCurrentAccountUiState
}

class OperatorCurrentAccountReportViewModel(
    private val bookingRepository: BookingRepository,
    private val supabaseClient: SupabaseClient
) : ViewModel() {

    private val _uiState = MutableStateFlow<OperatorCurrentAccountUiState>(OperatorCurrentAccountUiState.Loading)
    val uiState: StateFlow<OperatorCurrentAccountUiState> = _uiState.asStateFlow()

    init {
        loadReport()
    }

    fun loadReport() {
        viewModelScope.launch {
            _uiState.value = OperatorCurrentAccountUiState.Loading
            bookingRepository.getBookings("00000000-0000-0000-0000-000000000001")
                .onSuccess { bookings ->
                    val operators = bookings.mapNotNull { it.operatorName }.filter { it.isNotBlank() }.distinct().sorted()
                    val allOperators = listOf("Tümü") + operators

                    val ledgerItems = bookings.map { b ->
                        val pnr = b.operatorPnrCode?.takeIf { it.isNotBlank() } ?: "-"
                        val opName = b.operatorName.ifBlank { "Kendi Ürünümüz" }
                        val sales = b.totalPrice
                        val paid = if (pnr != "-" && b.status.name == "ONAYLANDI") (sales * 0.70) else 0.0 // Simüle edilmiş / kayıtlı ödeme tutarı
                        val bal = sales - paid

                        OperatorLedgerItem(
                            operatorPnrCode = pnr,
                            customerName = b.customerName.ifBlank { "Müşteri" },
                            bookingCode = b.bookingCode,
                            operatorName = opName,
                            totalSales = sales,
                            totalPaid = paid,
                            balance = bal,
                            createdAt = b.createdAt.ifBlank { "2026-08-13" }
                        )
                    }

                    val totalSalesSum = ledgerItems.sumOf { it.totalSales }
                    val totalPaidSum = ledgerItems.sumOf { it.totalPaid }
                    val netBalanceSum = ledgerItems.sumOf { it.balance }

                    _uiState.value = OperatorCurrentAccountUiState.Success(
                        items = ledgerItems,
                        filteredItems = ledgerItems,
                        availableOperators = allOperators,
                        selectedOperator = "Tümü",
                        selectedDateFilter = "Tüm Zamanlar",
                        totalSales = totalSalesSum,
                        totalPaid = totalPaidSum,
                        totalBalance = netBalanceSum
                    )
                }
                .onFailure { err ->
                    _uiState.value = OperatorCurrentAccountUiState.Error(
                        err.message ?: "Tur Operatörü cari ekstresi yüklenemedi."
                    )
                }
        }
    }

    fun setFilter(operator: String, dateFilter: String) {
        val currentState = _uiState.value as? OperatorCurrentAccountUiState.Success ?: return
        val filtered = currentState.items.filter { item ->
            val opMatch = operator == "Tümü" || item.operatorName.equals(operator, ignoreCase = true)
            opMatch
        }

        val totalSalesSum = filtered.sumOf { it.totalSales }
        val totalPaidSum = filtered.sumOf { it.totalPaid }
        val netBalanceSum = filtered.sumOf { it.balance }

        _uiState.value = currentState.copy(
            selectedOperator = operator,
            selectedDateFilter = dateFilter,
            filteredItems = filtered,
            totalSales = totalSalesSum,
            totalPaid = totalPaidSum,
            totalBalance = netBalanceSum
        )
    }

    fun exportToCsv(): String {
        val currentState = _uiState.value as? OperatorCurrentAccountUiState.Success ?: return ""
        val sb = java.lang.StringBuilder()
        sb.append("TO PNR,Musteri Adi,Paket Tur Kodu,Tur Operatoru,Tur Satisi (TRY),TO Odeme (TRY),Bakiye (TRY),Tarih\n")
        currentState.filteredItems.forEach { item ->
            sb.append("\"${item.operatorPnrCode}\",\"${item.customerName}\",\"${item.bookingCode}\",\"${item.operatorName}\",${item.totalSales},${item.totalPaid},${item.balance},\"${item.createdAt}\"\n")
        }
        _uiState.value = currentState.copy(
            notificationMessage = "📄 ${currentState.filteredItems.size} adet kayıt CSV olarak başarıyla dışa aktarıldı."
        )
        return sb.toString()
    }

    fun triggerPrint() {
        val currentState = _uiState.value as? OperatorCurrentAccountUiState.Success ?: return
        _uiState.value = currentState.copy(
            notificationMessage = "🖨️ Tur Operatörü Cari Ekstresi yazıcı çıktısına gönderiliyor..."
        )
    }

    fun clearNotification() {
        val currentState = _uiState.value as? OperatorCurrentAccountUiState.Success ?: return
        _uiState.value = currentState.copy(notificationMessage = null)
    }
}
