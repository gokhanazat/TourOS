package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.domain.model.Booking
import com.mgacreative.touros.domain.usecase.GetBookingsUseCase
import com.mgacreative.touros.domain.usecase.GetCurrentUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CustomerCrmDetail(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val totalBookings: Int,
    val ltvAmount: Double,
    val lastActivityDate: String,
    val bookingTypeStr: String
)

data class SegmentCategory(
    val id: String,
    val name: String,
    val icon: String,
    val description: String,
    val customers: List<CustomerCrmDetail>
) {
    val customerCount: Int get() = customers.size
    val avgLtv: Double get() = if (customers.isNotEmpty()) customers.sumOf { it.ltvAmount } / customers.size else 0.0
}

data class CustomerSegmentationUiState(
    val isLoading: Boolean = false,
    val totalCustomerCount: Int = 0,
    val segments: List<SegmentCategory> = emptyList(),
    val selectedSegmentId: String = "all",
    val searchQuery: String = "",
    val errorMessage: String? = null
)

class CustomerSegmentationViewModel(
    private val getBookingsUseCase: GetBookingsUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomerSegmentationUiState())
    val uiState: StateFlow<CustomerSegmentationUiState> = _uiState.asStateFlow()

    init {
        loadCustomerSegmentation()
    }

    fun loadCustomerSegmentation() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val user = getCurrentUserUseCase()
            val tenantId = user?.tenantId.orEmpty()

            getBookingsUseCase.getBookings(tenantId).onSuccess { bookings ->
                val customersMap = mutableMapOf<String, CustomerCrmDetail>()

                bookings.forEach { b ->
                    val cPhone = b.customerPhone.orEmpty()
                    val cEmail = b.customerEmail.orEmpty()
                    val key = cPhone.ifBlank { cEmail.ifBlank { b.customerName } }
                    if (key.isNotBlank()) {
                        val existing = customersMap[key]
                        val name = b.customerName.ifBlank { existing?.name ?: "Müşteri" }
                        val email = cEmail.ifBlank { existing?.email ?: "-" }
                        val phone = cPhone.ifBlank { existing?.phone ?: "-" }
                        val count = (existing?.totalBookings ?: 0) + 1
                        val ltv = (existing?.ltvAmount ?: 0.0) + b.totalPrice
                        val bDate = b.checkInDate ?: b.departureDate ?: "-"
                        val lastDate = if (existing == null || bDate > existing.lastActivityDate) bDate else existing.lastActivityDate
                        val typeStr = if (b.bookingType == "HOTEL") "Otel" else "Tur"

                        customersMap[key] = CustomerCrmDetail(
                            id = key,
                            name = name,
                            email = email,
                            phone = phone,
                            totalBookings = count,
                            ltvAmount = ltv,
                            lastActivityDate = lastDate,
                            bookingTypeStr = typeStr
                        )
                    }
                }

                val allCustomers = customersMap.values.toList().sortedByDescending { it.ltvAmount }

                // Dynamic Segmentation Logic based on real data
                val vipList = allCustomers.filter { it.totalBookings >= 2 || it.ltvAmount >= 20000.0 }
                val highSpenders = allCustomers.filter { it.ltvAmount >= 10000.0 }
                val newCustomers = allCustomers.filter { it.totalBookings == 1 }
                val activeB2b = allCustomers.filter { it.email.contains("acente") || it.email.contains("b2b") || it.ltvAmount >= 50000.0 }
                val atRisk = allCustomers.filter { it.totalBookings == 1 && it.ltvAmount < 5000.0 }

                val segmentsList = listOf(
                    SegmentCategory("all", "Tüm Müşteriler", "👥", "Sistemdeki tüm kayıtlı müşteriler ve ciro analizi", allCustomers),
                    SegmentCategory("vip", "VIP Sadık Müşteriler", "👑", "Çoklu rezervasyonu ve yüksek harcaması olan VIP kitle", vipList),
                    SegmentCategory("high_spenders", "Yüksek Harcamalı Gezginler", "💎", "LTV ₺10,000+ üstü harcama yapan premium gezginler", highSpenders),
                    SegmentCategory("new_registered", "Yeni Kayıt Olanlar", "🌟", "İlk rezervasyonunu gerçekleştiren yeni müşteriler", newCustomers),
                    SegmentCategory("b2b", "B2B / Kurumsal Temsilciler", "🏢", "Kurumsal acente ve VIP grup yetkilileri", activeB2b),
                    SegmentCategory("at_risk", "Riskli / Standart Müşteriler", "⚠️", "Tekil rezervasyonlu ve destek takibi gereken grup", atRisk)
                )

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    totalCustomerCount = allCustomers.size,
                    segments = segmentsList,
                    selectedSegmentId = if (segmentsList.isNotEmpty()) segmentsList.first().id else "all"
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Müşteri verileri yüklenemedi: ${err.message}"
                )
            }
        }
    }

    fun selectSegment(segmentId: String) {
        _uiState.value = _uiState.value.copy(selectedSegmentId = segmentId)
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }
}
