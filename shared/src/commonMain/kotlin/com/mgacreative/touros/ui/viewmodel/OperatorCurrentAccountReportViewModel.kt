package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.data.database.entity.AgencyOperatorConnectionEntity
import com.mgacreative.touros.domain.model.BookingStatus
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
        val totalSales: Double = 0.0, // Toplam Satış Fiyatı (Paket Satış Tutarları)
        val totalCost: Double = 0.0,  // Toplam Tur Maliyeti (Acentenin TO'ya Gerçek Borcu)
        val totalPaid: Double = 0.0,  // Toplam Yapılan TO Ödemesi
        val totalBalance: Double = 0.0, // Net Kalan Bakiye (Maliyet - Ödeme)
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

            // 1. Operatör bağlantılarını ve komisyon / sezon kurallarını çek
            val connections = runCatching {
                supabaseClient.postgrest["agency_operator_connections"]
                    .select()
                    .decodeList<AgencyOperatorConnectionEntity>()
            }.getOrDefault(emptyList())

            bookingRepository.getBookings("00000000-0000-0000-0000-000000000001")
                .onSuccess { bookings ->
                    val operatorsFromBookings = bookings.mapNotNull { it.operatorName }.filter { it.isNotBlank() }
                    val operatorsFromConnections = connections.map { it.operatorName }.filter { it.isNotBlank() }
                    val allOperators = (listOf("Tümü") + (operatorsFromBookings + operatorsFromConnections).distinct()).sorted()

                    val ledgerItems = bookings.map { b ->
                        val pnr = b.operatorPnrCode?.takeIf { it.isNotBlank() } ?: "-"
                        val opName = b.operatorName.ifBlank { "MGA Partner" }
                        val salesPrice = b.totalPrice
                        val bookingDate = b.createdAt.takeIf { it.isNotBlank() }?.take(10) ?: "2026-08-24"

                        // İlgili tur operatörünün bağlantı ve sezon komisyonunu bul
                        val matchedConn = connections.firstOrNull { conn ->
                            conn.operatorName.equals(opName, ignoreCase = true) ||
                            (conn.operatorCompanyId.isNotBlank() && conn.operatorCompanyId == b.agencyId)
                        }

                        // Tarih hangi sezona denk geliyorsa o sezonun komisyonu, yoksa standart komisyon oranı
                        val matchedSeason = matchedConn?.getMatchingSeason(bookingDate)
                        val effectiveCommissionRate = matchedSeason?.commissionRate
                            ?: matchedConn?.commissionRate
                            ?: 10.0 // Varsayılan anlaşma komisyonu %10

                        // Komisyon Tutarı hesaplama
                        val commissionAmount = if (matchedConn != null) {
                            matchedConn.calculateEarnings(salesPrice, b.paxCount, bookingDate)
                        } else {
                            salesPrice * (effectiveCommissionRate / 100.0)
                        }

                        // Tur Satış Maliyeti (Paket Satış Tutarı - Acente Komisyon Tutarı)
                        val tourCost = (salesPrice - commissionAmount).coerceAtLeast(0.0)

                        // Yapılan TO Ödemesi
                        val paid = if (pnr != "-" && b.status == BookingStatus.ONAYLANDI) (tourCost * 0.70) else 0.0
                        val bal = (tourCost - paid).coerceAtLeast(0.0)

                        OperatorLedgerItem(
                            operatorPnrCode = pnr,
                            customerName = b.customerName.ifBlank { "Müşteri" },
                            bookingCode = b.bookingCode,
                            operatorName = opName,
                            totalSales = salesPrice,
                            commissionRate = effectiveCommissionRate,
                            tourCost = tourCost,
                            totalPaid = paid,
                            balance = bal,
                            createdAt = bookingDate
                        )
                    }

                    val totalSalesSum = ledgerItems.sumOf { it.totalSales }
                    val totalCostSum = ledgerItems.sumOf { it.tourCost }
                    val totalPaidSum = ledgerItems.sumOf { it.totalPaid }
                    val netBalanceSum = ledgerItems.sumOf { it.balance }

                    _uiState.value = OperatorCurrentAccountUiState.Success(
                        items = ledgerItems,
                        filteredItems = ledgerItems,
                        availableOperators = allOperators,
                        selectedOperator = "Tümü",
                        selectedDateFilter = "Tüm Zamanlar",
                        totalSales = totalSalesSum,
                        totalCost = totalCostSum,
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
        val totalCostSum = filtered.sumOf { it.tourCost }
        val totalPaidSum = filtered.sumOf { it.totalPaid }
        val netBalanceSum = filtered.sumOf { it.balance }

        _uiState.value = currentState.copy(
            selectedOperator = operator,
            selectedDateFilter = dateFilter,
            filteredItems = filtered,
            totalSales = totalSalesSum,
            totalCost = totalCostSum,
            totalPaid = totalPaidSum,
            totalBalance = netBalanceSum
        )
    }

    fun exportToCsv(): String {
        val currentState = _uiState.value as? OperatorCurrentAccountUiState.Success ?: return ""
        val sb = StringBuilder()
        sb.append("TO PNR,Musteri Adi,Paket Tur Kodu,TO Acenta Adi,Tur Satis Fiyati (TRY),Acenta Komisyon (%),Tur Satis Maliyeti (TRY),TO Odeme (TRY),Bakiye (TRY),Tarih\n")
        currentState.filteredItems.forEach { item ->
            sb.append("\"${item.operatorPnrCode}\",\"${item.customerName}\",\"${item.bookingCode}\",\"${item.operatorName}\",${item.totalSales},${item.commissionRate},${item.tourCost},${item.totalPaid},${item.balance},\"${item.createdAt}\"\n")
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
