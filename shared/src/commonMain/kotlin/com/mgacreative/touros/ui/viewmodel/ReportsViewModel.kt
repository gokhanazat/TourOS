package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.domain.model.Booking
import com.mgacreative.touros.domain.model.BookingStatus
import com.mgacreative.touros.domain.usecase.GetBookingsUseCase
import com.mgacreative.touros.domain.usecase.GetCurrentUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.mgacreative.touros.utils.DateUtils

enum class ReportType(val label: String, val icon: String) {
    ALL("Tüm Rezervasyonlar", "📋"),
    TOUR("Tur & Operasyon", "🚌"),
    HOTEL("Otel & Konaklama", "🏨"),
    OPERATOR("Tur Operatörü Satışları", "💼"),
    FINANCIAL("Finans & Satış", "💳")
}

enum class DatePreset(val label: String) {
    TODAY("Bugün"),
    THIS_WEEK("Bu Hafta"),
    THIS_MONTH("Bu Ay"),
    THIS_YEAR("Bu Yıl"),
    ALL_TIME("Tüm Zamanlar"),
    CUSTOM("Özel Tarih")
}

private val DEFAULT_OPERATOR_LIST = listOf(
    "Tümü",
    "Kendi Ürünlerimiz",
    "Coral Travel",
    "Jolly Tur",
    "Etstur",
    "Setur",
    "TatilBudur",
    "Pegas Touristik",
    "Anex Tour",
    "Touristica",
    "Paximum",
    "Odeon Tour",
    "Hotelbeds",
    "Booking.com"
)

data class ReportsUiState(
    val isLoading: Boolean = false,
    val reportType: ReportType = ReportType.ALL,
    val datePreset: DatePreset = DatePreset.THIS_YEAR,
    val startDate: String = "${DateUtils.getCurrentYear()}-01-01",
    val endDate: String = "${DateUtils.getCurrentYear()}-12-31",
    val selectedOperator: String = "Tümü",
    val selectedStatus: String = "Tümü",
    val searchQuery: String = "",
    val availableOperators: List<String> = DEFAULT_OPERATOR_LIST,
    val availableStatuses: List<String> = listOf("Tümü", "Bekliyor", "Onaylandı", "Tamamlandı", "İptal"),
    val bookings: List<Booking> = emptyList(),
    val filteredBookings: List<Booking> = emptyList(),
    val totalBookingCount: Int = 0,
    val totalQuantityCount: Int = 0,
    val totalRevenue: Double = 0.0,
    val averageRevenue: Double = 0.0,
    val notificationMessage: String? = null,
    val isPrintActive: Boolean = false
)

class ReportsViewModel(
    private val getBookingsUseCase: GetBookingsUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    init {
        loadReports()
    }

    fun loadReports() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, notificationMessage = null)
            val user = getCurrentUserUseCase()
            val tenantId = user?.tenantId.orEmpty()

            getBookingsUseCase.getBookings(tenantId).onSuccess { list ->
                val operatorsFromData = list.mapNotNull { it.operatorName?.trim() }
                    .filter { it.isNotBlank() && !it.contains("MGA", ignoreCase = true) }
                    .distinct()
                val allOperators = (DEFAULT_OPERATOR_LIST + operatorsFromData).distinct()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    bookings = list,
                    availableOperators = allOperators
                )
                applyFilters()
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    notificationMessage = "Rapor verileri yüklenirken hata: ${err.message}"
                )
            }
        }
    }

    fun setReportType(type: ReportType) {
        _uiState.value = _uiState.value.copy(reportType = type)
        applyFilters()
    }

    fun setDatePreset(preset: DatePreset) {
        val today = DateUtils.getToday()
        val todayIso = DateUtils.getTodayIso()
        val (start, end) = when (preset) {
            DatePreset.TODAY -> todayIso to todayIso
            DatePreset.THIS_WEEK -> DateUtils.getFutureDateFormatted(-6, "-", reverseOrder = true) to todayIso
            DatePreset.THIS_MONTH -> {
                val ms = today.second.toString().padStart(2, '0')
                val lastDay = DateUtils.getDaysInMonth(today.second, today.third).toString().padStart(2, '0')
                "${today.third}-$ms-01" to "${today.third}-$ms-$lastDay"
            }
            DatePreset.THIS_YEAR -> "${today.third}-01-01" to "${today.third}-12-31"
            DatePreset.ALL_TIME -> "2020-01-01" to "${today.third + 5}-12-31"
            DatePreset.CUSTOM -> _uiState.value.startDate to _uiState.value.endDate
        }
        _uiState.value = _uiState.value.copy(datePreset = preset, startDate = start, endDate = end)
        applyFilters()
    }

    fun setCustomDates(start: String, end: String) {
        _uiState.value = _uiState.value.copy(datePreset = DatePreset.CUSTOM, startDate = start, endDate = end)
        applyFilters()
    }

    fun setOperatorFilter(operator: String) {
        _uiState.value = _uiState.value.copy(selectedOperator = operator)
        applyFilters()
    }

    fun setStatusFilter(status: String) {
        _uiState.value = _uiState.value.copy(selectedStatus = status)
        applyFilters()
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        applyFilters()
    }

    private fun applyFilters() {
        val state = _uiState.value
        val filtered = state.bookings.filter { b ->
            val isOwnProduct = b.operatorName.isNullOrBlank() || b.operatorName.contains("MGA", ignoreCase = true)

            // 1. Rapor tipi filtresi
            val typeMatch = when (state.reportType) {
                ReportType.ALL -> true
                ReportType.TOUR -> b.bookingType == "TOUR" || b.departureId != null
                ReportType.HOTEL -> b.bookingType == "HOTEL" || b.hotelId != null
                ReportType.OPERATOR -> !isOwnProduct
                ReportType.FINANCIAL -> true
            }

            // 2. Tur Operatörü filtresi
            val operatorMatch = when (state.selectedOperator) {
                "Tümü" -> true
                "Kendi Ürünlerimiz" -> isOwnProduct
                else -> b.operatorName.equals(state.selectedOperator, ignoreCase = true)
            }

            // 3. Durum filtresi
            val statusMatch = when (state.selectedStatus) {
                "Tümü" -> true
                else -> b.status.displayName.equals(state.selectedStatus, ignoreCase = true) ||
                        b.status.name.equals(state.selectedStatus, ignoreCase = true)
            }

            // 4. Tarih filtresi
            val bDate = b.checkInDate ?: b.departureDate ?: ""
            val dateMatch = bDate.isBlank() || (bDate >= state.startDate && bDate <= state.endDate)

            // 5. Arama filtresi
            val q = state.searchQuery.trim().lowercase()
            val searchMatch = q.isEmpty() ||
                    b.bookingCode.lowercase().contains(q) ||
                    b.customerName.lowercase().contains(q) ||
                    (b.productName?.lowercase()?.contains(q) == true) ||
                    (b.operatorName?.lowercase()?.contains(q) == true)

            typeMatch && operatorMatch && statusMatch && dateMatch && searchMatch
        }

        val totalRev = filtered.sumOf { it.totalPrice }
        val totalQty = filtered.sumOf { if (it.nights > 0) it.nights else it.paxCount }
        val avgRev = if (filtered.isNotEmpty()) totalRev / filtered.size else 0.0

        _uiState.value = state.copy(
            filteredBookings = filtered,
            totalBookingCount = filtered.size,
            totalQuantityCount = totalQty,
            totalRevenue = totalRev,
            averageRevenue = avgRev
        )
    }

    fun exportToCsv(): String {
        val state = _uiState.value
        val sb = StringBuilder()
        sb.append("Rezervasyon Kodu,TO PNR,Kullanıcı / Müşteri,Telefon,Tür,Ürün / Operatör,Tarih,Gece / Pax,Tutar (TRY),Ödeme Yöntemi,Durum\n")
        state.filteredBookings.forEach { b ->
            val typeStr = if (b.bookingType == "HOTEL") "Otel" else "Tur"
            val prodStr = b.productName ?: b.operatorName ?: "-"
            val dateStr = b.checkInDate ?: b.departureDate ?: "-"
            val qty = if (b.nights > 0) "${b.nights} Gece" else "${b.paxCount} Kisi"
            val toPnr = b.operatorPnrCode ?: "-"
            sb.append("\"${b.bookingCode}\",\"$toPnr\",\"${b.customerName}\",\"${b.customerPhone}\",\"$typeStr\",\"$prodStr\",\"$dateStr\",\"$qty\",${b.totalPrice},\"${b.paymentMethod ?: "-"}\",\"${b.status.displayName}\"\n")
        }
        _uiState.value = state.copy(notificationMessage = "📄 ${state.filteredBookings.size} adet kayıt CSV olarak hazırlandı ve indiriliyor.")
        return sb.toString()
    }

    fun generatePdfReport(): String {
        val state = _uiState.value
        _uiState.value = state.copy(notificationMessage = "📑 PDF Raporu Oluşturuldu (${state.filteredBookings.size} Kalem Kayıt).")
        return "PDF Generated for ${state.filteredBookings.size} items"
    }

    fun triggerPrint() {
        val state = _uiState.value
        _uiState.value = state.copy(isPrintActive = true, notificationMessage = "🖨️ Rapor yazıcı çıktısı hazırlanıyor...")
    }

    fun clearNotification() {
        _uiState.value = _uiState.value.copy(notificationMessage = null)
    }
}
