package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.data.util.isValidUuid
import com.mgacreative.touros.domain.model.Hotel
import com.mgacreative.touros.domain.model.HotelSeasonRate
import com.mgacreative.touros.domain.repository.BookingRepository
import com.mgacreative.touros.domain.repository.HotelRepository
import com.mgacreative.touros.domain.usecase.GetCurrentUserUseCase
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

data class B2CHotelDetailCheckoutUiState(
    val isLoading: Boolean = true,
    val hotel: Hotel? = null,
    val seasonRates: List<HotelSeasonRate> = emptyList(),
    val availableRoomTypes: List<String> = STANDARD_ROOM_TYPES,
    val selectedRoomType: String = "Standard Room",
    val checkInDate: String = "2026-07-01",
    val checkOutDate: String = "2026-07-05",
    val nights: Int = 4,
    val pricePerNight: Double = 0.0,
    val totalPrice: Double = 0.0,
    val customerName: String = "",
    val customerPhone: String = "",
    val customerEmail: String = "",
    val paymentMethod: String = "KREDİ_KARTI", // KREDİ_KARTI, HAVALE_EFT, ACENTEDE_ODEME
    val cardHolder: String = "",
    val cardNumber: String = "",
    val cardExpiry: String = "",
    val cvv: String = "",
    val isBookingSuccess: Boolean = false,
    val notificationMessage: String? = null,
    val errorMessage: String? = null
)

class B2CHotelDetailCheckoutViewModel(
    private val hotelRepository: HotelRepository,
    private val bookingRepository: BookingRepository,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val supabaseClient: SupabaseClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(B2CHotelDetailCheckoutUiState())
    val uiState: StateFlow<B2CHotelDetailCheckoutUiState> = _uiState.asStateFlow()

    fun loadHotelDetail(hotelId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            val hotelRes = hotelRepository.getHotelById(hotelId)
            val seasonRates = hotelRepository.getSeasonRatesForHotel(hotelId).getOrDefault(emptyList())

            hotelRes.onSuccess { hotel ->
                val ratesRoomTypes = seasonRates.mapNotNull { it.roomTypeName?.trim() }.filter { it.isNotBlank() }
                val allRoomTypes = (ratesRoomTypes + STANDARD_ROOM_TYPES).distinct()
                val selectedRoom = ratesRoomTypes.firstOrNull() ?: _uiState.value.selectedRoomType

                val price = findMatchingPeriodPrice(seasonRates, selectedRoom, _uiState.value.checkInDate)
                val nights = calculateNights(_uiState.value.checkInDate, _uiState.value.checkOutDate)
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    hotel = hotel,
                    seasonRates = seasonRates,
                    availableRoomTypes = allRoomTypes,
                    selectedRoomType = selectedRoom,
                    pricePerNight = price,
                    nights = nights,
                    totalPrice = nights * price
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Otel bilgisi yüklenemedi: ${err.message}"
                )
            }
        }
    }

    fun updateDates(checkIn: String, checkOut: String) {
        val nights = calculateNights(checkIn, checkOut).coerceAtLeast(1)
        val price = findMatchingPeriodPrice(_uiState.value.seasonRates, _uiState.value.selectedRoomType, checkIn)
        _uiState.value = _uiState.value.copy(
            checkInDate = checkIn,
            checkOutDate = checkOut,
            nights = nights,
            pricePerNight = price,
            totalPrice = nights * price
        )
    }

    fun updateRoomType(roomType: String) {
        val price = findMatchingPeriodPrice(_uiState.value.seasonRates, roomType, _uiState.value.checkInDate)
        _uiState.value = _uiState.value.copy(
            selectedRoomType = roomType,
            pricePerNight = price,
            totalPrice = _uiState.value.nights * price
        )
    }

    private fun findMatchingPeriodPrice(
        rates: List<HotelSeasonRate>,
        roomTypeName: String,
        checkInDate: String
    ): Double {
        if (rates.isEmpty()) return 0.0

        val cleanRoom = roomTypeName.trim()
        val cleanCheckIn = checkInDate.trim()

        fun extractPrice(rate: HotelSeasonRate): Double {
            return if (rate.salePrice > 0) rate.salePrice
            else if (rate.doublePrice > 0) rate.doublePrice
            else if (rate.singlePrice > 0) rate.singlePrice
            else if (rate.costPrice > 0) rate.costPrice
            else 0.0
        }

        val exactMatch = rates.firstOrNull { rate ->
            val matchesRoom = rate.roomTypeName.orEmpty().trim().equals(cleanRoom, ignoreCase = true) ||
                    (rate.roomTypeId != null && rate.roomTypeId.trim().equals(cleanRoom, ignoreCase = true))
            val matchesDate = cleanCheckIn.isBlank() || (rate.startDate <= cleanCheckIn && rate.endDate >= cleanCheckIn)
            matchesRoom && matchesDate
        }

        if (exactMatch != null) {
            val price = extractPrice(exactMatch)
            if (price > 0) return price
        }

        val roomMatch = rates.firstOrNull { rate ->
            rate.roomTypeName.orEmpty().trim().equals(cleanRoom, ignoreCase = true) ||
                    (rate.roomTypeId != null && rate.roomTypeId.trim().equals(cleanRoom, ignoreCase = true))
        }

        if (roomMatch != null) {
            val price = extractPrice(roomMatch)
            if (price > 0) return price
        }

        val dateMatch = rates.firstOrNull { rate ->
            cleanCheckIn.isNotBlank() && rate.startDate <= cleanCheckIn && rate.endDate >= cleanCheckIn
        }

        if (dateMatch != null) {
            val price = extractPrice(dateMatch)
            if (price > 0) return price
        }

        for (rate in rates) {
            val price = extractPrice(rate)
            if (price > 0) return price
        }

        return 0.0
    }

    fun updateCustomerInfo(name: String, phone: String, email: String) {
        _uiState.value = _uiState.value.copy(
            customerName = name,
            customerPhone = phone,
            customerEmail = email
        )
    }

    fun updateCardInfo(holder: String, number: String, expiry: String, cvvCode: String) {
        _uiState.value = _uiState.value.copy(
            cardHolder = holder,
            cardNumber = number,
            cardExpiry = expiry,
            cvv = cvvCode
        )
    }

    fun updatePaymentMethod(method: String) {
        _uiState.value = _uiState.value.copy(paymentMethod = method)
    }

    fun submitHotelBooking() {
        viewModelScope.launch {
            val state = _uiState.value
            val hotel = state.hotel ?: return@launch
            
            if (state.customerName.isBlank() || state.customerPhone.isBlank()) {
                _uiState.value = state.copy(errorMessage = "Lütfen müşteri adı ve telefon numarasını giriniz.")
                return@launch
            }

            _uiState.value = state.copy(isLoading = true, errorMessage = null)
            val user = getCurrentUserUseCase()
            val tenantId = user?.tenantId?.takeIf { it.isValidUuid() } ?: hotel.tenantId.takeIf { it.isValidUuid() } ?: "00000000-0000-0000-0000-000000000001"
            val bookingCode = "HTL-${(100000..999999).random()}"

            val booking = com.mgacreative.touros.domain.model.Booking(
                bookingCode = bookingCode,
                hotelId = hotel.id,
                customerName = state.customerName,
                customerPhone = state.customerPhone,
                customerEmail = state.customerEmail,
                checkInDate = state.checkInDate,
                checkOutDate = state.checkOutDate,
                roomTypeName = state.selectedRoomType,
                nights = state.nights,
                totalPrice = state.totalPrice,
                currency = "TRY",
                status = com.mgacreative.touros.domain.model.BookingStatus.BEKLIYOR,
                bookingType = "HOTEL",
                paymentMethod = state.paymentMethod,
                productName = hotel.name,
                notes = "B2C Otel Rezervasyonu (${state.nights} Gece - ${state.selectedRoomType})",
                tenantId = tenantId
            )

            val result = bookingRepository.createBooking(booking)

            if (result.isSuccess) {
                _uiState.value = state.copy(
                    isLoading = false,
                    isBookingSuccess = true,
                    notificationMessage = "🎉 Otel Rezervasyonunuz Başarıyla Oluşturuldu! Rezervasyon Kodu: $bookingCode"
                )
            } else {
                _uiState.value = state.copy(
                    isLoading = false,
                    errorMessage = "Rezervasyon oluşturulamadı: ${result.exceptionOrNull()?.message}"
                )
            }
        }
    }

    private fun calculateNights(checkIn: String, checkOut: String): Int {
        return try {
            fun parseEpochDays(dateStr: String): Int? {
                val cleanStr = dateStr.trim()
                val parts = when {
                    cleanStr.contains(".") -> {
                        val p = cleanStr.split(".")
                        if (p.size == 3) listOf(p[2].toIntOrNull(), p[1].toIntOrNull(), p[0].toIntOrNull()) else null
                    }
                    cleanStr.contains("-") -> {
                        val p = cleanStr.split("-")
                        if (p.size == 3) listOf(p[0].toIntOrNull(), p[1].toIntOrNull(), p[2].toIntOrNull()) else null
                    }
                    else -> null
                } ?: return null

                val y = parts[0] ?: return null
                val m = parts[1] ?: return null
                val d = parts[2] ?: return null

                val monthDays = intArrayOf(0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
                val isLeap = (y % 4 == 0 && y % 100 != 0) || (y % 400 == 0)
                if (isLeap) monthDays[2] = 29

                var days = y * 365 + y / 4 - y / 100 + y / 400
                for (i in 1 until m.coerceIn(1, 12)) {
                    days += monthDays[i]
                }
                days += d
                return days
            }

            val day1 = parseEpochDays(checkIn) ?: return 1
            val day2 = parseEpochDays(checkOut) ?: return 1
            val diff = day2 - day1
            if (diff <= 0) 1 else diff
        } catch (e: Exception) {
            1
        }
    }
}
