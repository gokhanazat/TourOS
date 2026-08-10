package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.data.database.entity.BookingEntity
import com.mgacreative.touros.data.database.entity.UnifiedProductEntity
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

data class FlightOption(
    val id: String,
    val outboundAirline: String,
    val outboundFlightNumber: String,
    val outboundDeparturePort: String,
    val outboundArrivalPort: String,
    val outboundDepartureTime: String = "02:05",
    val outboundArrivalTime: String = "06:45",
    val outboundDuration: String = "4s 40d",
    val inboundAirline: String,
    val inboundFlightNumber: String,
    val inboundDeparturePort: String,
    val inboundArrivalPort: String,
    val inboundDepartureTime: String = "18:40",
    val inboundArrivalTime: String = "23:05",
    val inboundDuration: String = "4s 25d",
    val baggageKg: Int = 20,
    val handBaggageKg: Int = 8,
    val priceDeltaRub: Double = 0.0
)

data class ExtraService(
    val id: String,
    val name: String,
    val category: String, // "TRANSFER", "INSURANCE", "EXTRA"
    val unitPriceEur: Double,
    val isMandatory: Boolean = false,
    var isSelected: Boolean = false,
    val paxCount: Int = 1
)

data class PassengerInfo(
    val index: Int,
    var gender: String = "MALE", // "MALE", "FEMALE"
    var firstName: String = "",
    var lastName: String = "",
    var birthDate: String = "",
    var citizenship: String = "Türkiye",
    var documentType: String = "Pasaport",
    var passportSeries: String = "",
    var passportNumber: String = "",
    var documentExpiryDate: String = "",
    var isPayer: Boolean = false,
    var phone: String = "",
    var email: String = "",
    var address: String = "",
    var parentIndex: Int? = null
)

sealed class B2BTourSearchUiState {
    data object Loading : B2BTourSearchUiState()
    data class Success(
        val allProducts: List<UnifiedProductEntity>,
        val filteredProducts: List<UnifiedProductEntity>,
        val totalFoundCount: Int
    ) : B2BTourSearchUiState()
    data class Error(val message: String) : B2BTourSearchUiState()
}

class B2BTourSearchViewModel(
    private val supabaseClient: SupabaseClient
) : ViewModel() {

    private val _uiState = MutableStateFlow<B2BTourSearchUiState>(B2BTourSearchUiState.Loading)
    val uiState: StateFlow<B2BTourSearchUiState> = _uiState.asStateFlow()

    // Arama Parametreleri State
    var departureCity = MutableStateFlow("Moskova")
    var destinationCountry = MutableStateFlow("Türkiye")
    var selectedRegion = MutableStateFlow("Antalya")
    var nights = MutableStateFlow(7)
    var adults = MutableStateFlow(2)
    var childs = MutableStateFlow(0)
    var selectedStars = MutableStateFlow(setOf(3, 4, 5))
    var selectedMealTypes = MutableStateFlow(setOf<String>())
    var selectedRoomTypes = MutableStateFlow(setOf<String>())
    var isInstantConfirmationOnly = MutableStateFlow(false)
    var isPromoOnly = MutableStateFlow(false)
    var searchQuery = MutableStateFlow("")

    // Seçili Tur / Rezervasyon Akışı State (Kullanıcı seçene kadar null)
    val selectedProduct = MutableStateFlow<UnifiedProductEntity?>(null)
    val availableFlightOptions = MutableStateFlow<List<FlightOption>>(emptyList())
    val selectedFlightOption = MutableStateFlow<FlightOption?>(null)
    val extraServices = MutableStateFlow<List<ExtraService>>(emptyList())
    val passengers = MutableStateFlow<List<PassengerInfo>>(emptyList())

    val isSavingBooking = MutableStateFlow(false)
    val createdPnrCode = MutableStateFlow("")

    init {
        performSearch()
    }

    fun performSearch() {
        viewModelScope.launch {
            _uiState.value = B2BTourSearchUiState.Loading

            var items = emptyList<UnifiedProductEntity>()
            runCatching {
                supabaseClient.postgrest["marketplace_products"]
                    .select()
                    .decodeList<UnifiedProductEntity>()
            }.onSuccess { list ->
                items = list
            }.onFailure { err ->
                println("⚠️ Supabase search load warning: ${err.message}")
            }

            // Hem Supabase'den gelenleri hem de oturum boyunca yüklenen hafızadaki ürünleri birleştir
            val memoryItems = AgencyProductPublishingViewModel.getPersistentProducts()
            val combined = (items + memoryItems).distinctBy { it.id }
            val filtered = filterProducts(combined)

            _uiState.value = B2BTourSearchUiState.Success(
                allProducts = combined,
                filteredProducts = filtered,
                totalFoundCount = filtered.size
            )
        }
    }

    private fun filterProducts(list: List<UnifiedProductEntity>): List<UnifiedProductEntity> {
        val q = searchQuery.value.trim().lowercase()
        val stars = selectedStars.value
        val meals = selectedMealTypes.value

        return list.filter { item ->
            val matchesSearch = q.isBlank() ||
                    item.hotelName.lowercase().contains(q) ||
                    item.tourName.lowercase().contains(q) ||
                    item.region.lowercase().contains(q) ||
                    item.country.lowercase().contains(q) ||
                    item.departureCity.lowercase().contains(q) ||
                    item.operatorName.lowercase().contains(q)

            val matchesStar = stars.isEmpty() || item.hotelCategory == 0 || stars.contains(item.hotelCategory)
            val matchesMeal = meals.isEmpty() || item.mealType.isBlank() || meals.contains(item.mealType)

            matchesSearch && matchesStar && matchesMeal
        }
    }

    fun selectProductForBooking(product: UnifiedProductEntity) {
        selectedProduct.value = product
        
        val airline = product.airlineName.ifBlank { "Charter Flight" }
        val flightNo = product.flightNumber.ifBlank { "CH-${(1000..9999).random()}" }
        val depCity = product.departureCity.ifBlank { "Kalkış Şehri" }
        val arrCity = product.region.ifBlank { "Varış Bölgesi" }
        val bagKg = if (product.baggageKg > 0) product.baggageKg else 20

        val flights = listOf(
            FlightOption(
                id = "fl-${product.id}-1",
                outboundAirline = airline,
                outboundFlightNumber = flightNo,
                outboundDeparturePort = "$depCity 02:05",
                outboundArrivalPort = "$arrCity 06:45",
                outboundDepartureTime = "02:05",
                outboundArrivalTime = "06:45",
                outboundDuration = "4s 40d",
                inboundAirline = airline,
                inboundFlightNumber = "${flightNo}R",
                inboundDeparturePort = "$arrCity 18:40",
                inboundArrivalPort = "$depCity 23:05",
                inboundDepartureTime = "18:40",
                inboundArrivalTime = "23:05",
                inboundDuration = "4s 25d",
                baggageKg = bagKg,
                handBaggageKg = 8,
                priceDeltaRub = 0.0
            )
        )

        availableFlightOptions.value = flights
        selectedFlightOption.value = flights.first()

        val paxCount = (adults.value + childs.value).coerceAtLeast(1)

        // Dinamik Ekstra Hizmetler (Yolcu Sayısına Bağlı)
        extraServices.value = listOf(
            ExtraService("srv-1", "SOGLASIE Medikal Sigorta 50.000 EUR", "INSURANCE", 16.25, isMandatory = true, isSelected = true, paxCount = paxCount),
            ExtraService("srv-2", "Seyahat İptal / Vize İptal Sigortası", "INSURANCE", 25.00, isMandatory = false, isSelected = true, paxCount = paxCount),
            ExtraService("srv-3", "Elite VIP Özel Havalimanı Transferi", "TRANSFER", 0.00, isMandatory = false, isSelected = true, paxCount = paxCount),
            ExtraService("srv-4", "Bebek Oto Koltuğu Ekstrası", "EXTRA", 15.00, isMandatory = false, isSelected = false, paxCount = 1)
        )

        // Dinamik Boş Yolcu Formu
        val initialPaxList = (1..paxCount).map { idx ->
            PassengerInfo(
                index = idx,
                gender = if (idx % 2 != 0) "MALE" else "FEMALE",
                firstName = "",
                lastName = "",
                birthDate = "",
                citizenship = "Türkiye",
                documentType = "Pasaport",
                passportSeries = "",
                passportNumber = "",
                documentExpiryDate = "",
                isPayer = (idx == 1),
                phone = "",
                email = ""
            )
        }
        passengers.value = initialPaxList
    }

    /**
     * Yolcu Ekleme (Dinamik Form)
     */
    fun addPassenger() {
        val current = passengers.value.toMutableList()
        val nextIdx = current.size + 1
        current.add(
            PassengerInfo(
                index = nextIdx,
                gender = "MALE",
                firstName = "",
                lastName = "",
                birthDate = "",
                citizenship = "Türkiye",
                documentType = "Pasaport",
                passportSeries = "",
                passportNumber = "",
                documentExpiryDate = "",
                isPayer = (current.isEmpty()),
                phone = "",
                email = ""
            )
        )
        passengers.value = current

        updateExtraServicesPaxCount(current.size)
    }

    /**
     * Yolcu Çıkarma (Dinamik Form)
     */
    fun removePassenger(paxIndex: Int) {
        val current = passengers.value.toMutableList()
        if (current.size > 1) {
            current.removeAll { it.index == paxIndex }
            val reindexed = current.mapIndexed { idx, p ->
                p.copy(index = idx + 1, isPayer = (idx == 0))
            }
            passengers.value = reindexed
            updateExtraServicesPaxCount(reindexed.size)
        }
    }

    private fun updateExtraServicesPaxCount(count: Int) {
        extraServices.value = extraServices.value.map { srv ->
            srv.copy(paxCount = count)
        }
    }

    fun toggleExtraService(serviceId: String) {
        extraServices.value = extraServices.value.map { srv ->
            if (srv.id == serviceId && !srv.isMandatory) {
                srv.copy(isSelected = !srv.isSelected)
            } else srv
        }
    }

    /**
     * Rezervasyonu Supabase public.bookings Tablosuna Gerçek Olarak Kaydeder
     */
    fun confirmBookingAndSaveToSupabase(onComplete: (pnrCode: String) -> Unit) {
        viewModelScope.launch {
            isSavingBooking.value = true
            val prod = selectedProduct.value ?: return@launch
            val fl = selectedFlightOption.value
            val pList = passengers.value

            val pnr = "B2B-PNR-${Random.nextInt(100000, 999999)}"
            createdPnrCode.value = pnr

            val mainPayer = pList.firstOrNull { it.isPayer } ?: pList.firstOrNull()
            val payerName = "${mainPayer?.firstName ?: ""} ${mainPayer?.lastName ?: ""}".trim().ifBlank { "Müşteri Yolcu" }

            val basePrice = prod.price * 1.125
            val flightDelta = fl?.priceDeltaRub ?: 0.0
            val extrasEur = extraServices.value.filter { it.isSelected }.sumOf { it.unitPriceEur * it.paxCount }
            val totalPriceRub = basePrice + flightDelta + (extrasEur * 100.0)

            val bookingRecord = BookingEntity(
                id = "bkg-${Random.nextInt(100000, 999999)}",
                bookingCode = pnr,
                customerName = payerName,
                customerEmail = mainPayer?.email?.ifBlank { "acente@touros.com" },
                customerPhone = mainPayer?.phone?.ifBlank { "+90 500 000 0000" },
                totalPrice = totalPriceRub,
                currency = prod.currency.ifBlank { "RUB" },
                paxCount = pList.size,
                status = "Onaylandı",
                operatorName = prod.operatorName.ifBlank { "Anex/Coral/Pegas" },
                productName = "${prod.tourName.ifBlank { prod.hotelName }} (${prod.hotelName})",
                departureDate = prod.departureDate ?: "2026-08-21",
                nights = prod.nights,
                bookingType = "PACKAGE_TOUR",
                roomTypeName = prod.roomType.ifBlank { "DELUXE ROOM" },
                notes = "Gidiş: ${fl?.outboundAirline ?: "TK"} (${fl?.outboundFlightNumber ?: "TK3015"}), Dönüş: ${fl?.inboundAirline ?: "2S"} (${fl?.inboundFlightNumber ?: "2S135"})"
            )

            // Supabase 'bookings' tablosuna kaydet
            runCatching {
                supabaseClient.postgrest["bookings"].insert(bookingRecord)
            }.onSuccess {
                println("✅ Rezervasyon Supabase bookings tablosuna başarıyla kaydedildi: PNR $pnr")
            }.onFailure { err ->
                println("⚠️ Supabase bookings insert uyarısı: ${err.message}")
            }

            isSavingBooking.value = false
            onComplete(pnr)
        }
    }

}
