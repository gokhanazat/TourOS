package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.data.database.entity.BookingEntity
import com.mgacreative.touros.data.database.entity.UnifiedProductEntity
import com.mgacreative.touros.data.util.generateUuid
import com.mgacreative.touros.domain.model.Booking
import com.mgacreative.touros.domain.model.BookingItem
import com.mgacreative.touros.domain.model.BookingStatus
import com.mgacreative.touros.domain.model.Passenger
import com.mgacreative.touros.domain.repository.BookingRepository
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
    var parentIndex: Int? = null,
    var isInfantSeatRequested: Boolean = false
)

@kotlinx.serialization.Serializable
data class SearchFilterMetadataDto(
    val departure_cities: List<String> = emptyList(),
    val countries: List<String> = emptyList(),
    val regions: List<String> = emptyList(),
    val operators: List<String> = emptyList(),
    val currencies: List<String> = emptyList(),
    val min_price: Double = 0.0,
    val max_price: Double = 500000.0
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
    private val supabaseClient: SupabaseClient,
    private val bookingRepository: BookingRepository,
    private val hotelRepository: com.mgacreative.touros.domain.repository.HotelRepository? = null,
    private val tourRepository: com.mgacreative.touros.domain.repository.TourRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow<B2BTourSearchUiState>(B2BTourSearchUiState.Loading)
    val uiState: StateFlow<B2BTourSearchUiState> = _uiState.asStateFlow()
    val searchFilterMetadata = MutableStateFlow<SearchFilterMetadataDto?>(null)

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

    // ─── Acente Sorgu Kotası State (Quota Guard) ─────────────────────────────
    val monthlyQuota = MutableStateFlow(5000)
    val currentQueries = MutableStateFlow(1420)
    val isQuotaExceeded = MutableStateFlow(false)
    val quotaErrorMessage = MutableStateFlow<String?>(null)

    init {
        performSearch()
    }

    fun performSearch() {
        viewModelScope.launch {
            // 1. KOTA AŞIM KONTROLÜ (Güvenlik Kalkanı)
            if (monthlyQuota.value > 0 && currentQueries.value >= monthlyQuota.value) {
                isQuotaExceeded.value = true
                quotaErrorMessage.value = "⛔ Aylık arama ve sorgu kotanız dolmuştur (${currentQueries.value} / ${monthlyQuota.value})."
                _uiState.value = B2BTourSearchUiState.Error("Aylık sorgu kotanız dolmuştur. Lütfen yöneticiniz ile iletişime geçiniz.")
                return@launch
            }

            // Kota aşılmamışsa aramayı say ve devam et
            currentQueries.value += 1
            isQuotaExceeded.value = false
            quotaErrorMessage.value = null
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

            var localHotelProducts = emptyList<UnifiedProductEntity>()
            if (hotelRepository != null) {
                runCatching {
                    hotelRepository.getHotels(tenantId = "", city = null).getOrNull() ?: emptyList()
                }.onSuccess { hotels ->
                    localHotelProducts = hotels.map { h ->
                        UnifiedProductEntity(
                            id = "local-hotel-${h.id}",
                            hotelName = h.name,
                            tourName = "${h.name} - Yerel Otel",
                            country = (h.country ?: "").ifBlank { "Türkiye" },
                            region = (h.city ?: "").ifBlank { "Yerel Bölge" },
                            departureCity = "Yerel Otel",
                            hotelCategory = h.starRating ?: 4,
                            price = 120.0,
                            currency = "EUR",
                            operatorName = "Yerel Oteller",
                            productType = "LOCAL_HOTEL",
                            roomType = "Standart Oda",
                            mealType = "Her Şey Dahil (AI)",
                            pictureUrl = h.coverImageUrl ?: ""
                        )
                    }
                }
            }

            var localTourProducts = emptyList<UnifiedProductEntity>()
            if (tourRepository != null) {
                runCatching {
                    tourRepository.getTours(tenantId = "").getOrNull() ?: emptyList()
                }.onSuccess { tours ->
                    localTourProducts = tours.map { t ->
                        UnifiedProductEntity(
                            id = "local-tour-${t.id}",
                            hotelName = "${t.title} (Yerel Tur)",
                            tourName = t.title,
                            country = t.country.ifBlank { "Türkiye" },
                            region = t.city.ifBlank { "Yerel Bölge" },
                            departureCity = "Yerel Çıkış",
                            hotelCategory = 5,
                            price = if (t.basePrice > 0) t.basePrice else 95.0,
                            currency = "EUR",
                            operatorName = "Yerel Turlar",
                            productType = "LOCAL_TOUR",
                            nights = t.durationDays,
                            roomType = "Tur Paketi",
                            mealType = "Tam Pansiyon (FB)",
                            pictureUrl = t.coverImageUrl ?: ""
                        )
                    }
                }
            }

            val sampleFlightProducts = listOf(
                UnifiedProductEntity(
                    id = "flight-sample-1",
                    productType = "FLIGHT",
                    tourName = "Moskova (SVO) - Antalya (AYT) Uçuş Seferi",
                    operatorName = "Turkish Airlines (THY)",
                    departureCity = "Moskova",
                    country = "Türkiye",
                    region = "Antalya",
                    flightNumber = "TK-3701",
                    airlineName = "Turkish Airlines",
                    isCharter = false,
                    price = 240.0,
                    currency = "EUR",
                    pictureUrl = "https://images.unsplash.com/photo-1436491865332-7a61a109cc05?w=800"
                ),
                UnifiedProductEntity(
                    id = "flight-sample-2",
                    productType = "FLIGHT",
                    tourName = "Moskova (DME) - Antalya (AYT) Direk Charter Uçuş",
                    operatorName = "Pegas Touristik (Nordwind)",
                    departureCity = "Moskova",
                    country = "Türkiye",
                    region = "Antalya",
                    flightNumber = "N4-5821",
                    airlineName = "Nordwind Airlines",
                    isCharter = true,
                    price = 185.0,
                    currency = "EUR",
                    pictureUrl = "https://images.unsplash.com/photo-1540959733332-eab4deabeeaf?w=800"
                ),
                UnifiedProductEntity(
                    id = "flight-sample-3",
                    productType = "FLIGHT",
                    tourName = "Moskova (VKO) - İstanbul (IST) Tarifeli Uçuş",
                    operatorName = "Aeroflot",
                    departureCity = "Moskova",
                    country = "Türkiye",
                    region = "İstanbul",
                    flightNumber = "SU-2134",
                    airlineName = "Aeroflot",
                    isCharter = false,
                    price = 210.0,
                    currency = "EUR",
                    pictureUrl = "https://images.unsplash.com/photo-1506015391300-4802dc74de2e?w=800"
                ),
                UnifiedProductEntity(
                    id = "flight-sample-4",
                    productType = "FLIGHT",
                    tourName = "İstanbul (SAW) - Antalya (AYT) Direkt Uçuş",
                    operatorName = "Pegasus Airlines",
                    departureCity = "İstanbul",
                    country = "Türkiye",
                    region = "Antalya",
                    flightNumber = "PC-2014",
                    airlineName = "Pegasus Airlines",
                    isCharter = false,
                    price = 85.0,
                    currency = "EUR",
                    pictureUrl = "https://images.unsplash.com/photo-1519074069444-1ba4eff56b61?w=800"
                )
            )

            // Hem Supabase'den gelenleri, Yerel Otelleri, Uçuşları hem de hafızadaki ürünleri birleştir
            val memoryItems = AgencyProductPublishingViewModel.getPersistentProducts()
            val combined = (items + localHotelProducts + localTourProducts + sampleFlightProducts + memoryItems).distinctBy { it.id }
            val filtered = filterProducts(combined)

            val dbDepartureCities = combined.map { it.departureCity }.filter { it.isNotBlank() && it != "Yerel Otel" }.distinct().sorted()
            val dbCountries = combined.map { it.country }.filter { it.isNotBlank() }.distinct().sorted()
            val dbRegions = combined.map { it.region }.filter { it.isNotBlank() }.distinct().sorted()
            val dbOperators = combined.map { it.operatorName }.filter { it.isNotBlank() }.distinct().sorted()
            val dbCurrencies = combined.map { it.currency }.filter { it.isNotBlank() }.distinct().sorted()

            searchFilterMetadata.value = SearchFilterMetadataDto(
                departure_cities = dbDepartureCities.ifEmpty { listOf("Moskova", "Saint Petersburg", "Kazan", "Yekaterinburg", "İstanbul") },
                countries = dbCountries.ifEmpty { listOf("Türkiye", "Mısır", "BAE", "Rusya", "Tayland") },
                regions = dbRegions.ifEmpty { listOf("Alanya", "Antalya", "Belek", "Kemer", "Side", "Marmaris", "Bodrum") },
                operators = dbOperators.ifEmpty { listOf("Coral Travel", "Pegas Touristik", "Anex Tour", "Biblio Globus", "Fun & Sun", "Tez Tour") },
                currencies = dbCurrencies.ifEmpty { listOf("RUB", "TRY", "EUR", "USD") }
            )

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

    fun selectProductById(productId: String) {
        if (productId.isBlank()) return
        if (selectedProduct.value?.id == productId) return

        viewModelScope.launch {
            var matched: UnifiedProductEntity? = null
            runCatching {
                supabaseClient.postgrest["marketplace_products"]
                    .select { filter { eq("id", productId) } }
                    .decodeSingleOrNull<UnifiedProductEntity>()
            }.onSuccess {
                matched = it
            }

            if (matched == null) {
                val defaultOffer = com.mgacreative.touros.ui.screens.getInitialDefaultOffers().find { 
                    it.id == productId || it.id.equals(productId, ignoreCase = true) || productId.contains(it.id, ignoreCase = true)
                }
                if (defaultOffer != null) {
                    matched = UnifiedProductEntity(
                        id = defaultOffer.id,
                        hotelName = defaultOffer.hotelName,
                        region = defaultOffer.location,
                        price = defaultOffer.minPrice,
                        currency = defaultOffer.currency,
                        nights = defaultOffer.nights,
                        mealType = defaultOffer.mealType,
                        roomType = defaultOffer.roomType,
                        flightNumber = defaultOffer.flightCode,
                        hotelCategory = defaultOffer.stars,
                        operatorName = defaultOffer.operatorName,
                        pictureUrl = defaultOffer.imageUrl,
                        productType = defaultOffer.category
                    )
                }
            }

            if (matched == null && _uiState.value is B2BTourSearchUiState.Success) {
                matched = (_uiState.value as B2BTourSearchUiState.Success).allProducts.find { it.id == productId }
            }

            if (matched == null) {
                val firstDefault = com.mgacreative.touros.ui.screens.getInitialDefaultOffers().firstOrNull()
                if (firstDefault != null) {
                    matched = UnifiedProductEntity(
                        id = firstDefault.id,
                        hotelName = firstDefault.hotelName,
                        region = firstDefault.location,
                        price = firstDefault.minPrice,
                        currency = firstDefault.currency,
                        nights = firstDefault.nights,
                        mealType = firstDefault.mealType,
                        roomType = firstDefault.roomType,
                        flightNumber = firstDefault.flightCode,
                        hotelCategory = firstDefault.stars,
                        operatorName = firstDefault.operatorName,
                        pictureUrl = firstDefault.imageUrl,
                        productType = firstDefault.category
                    )
                }
            }

            matched?.let { selectProductForBooking(it) }
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

            val bookingId = generateUuid()

            // 1. ZENGİN YOLCU LİSTESİ OLUŞTURMA (Cinsiyet, Pasaport, SKT, Sorumlu Yetişkin, İnfant Koltuk)
            val domainPassengers = pList.mapIndexed { idx, p ->
                val fullName = "${p.firstName} ${p.lastName}".trim().ifBlank { "Turist ${idx + 1}" }
                val extraInfo = buildString {
                    if (p.isInfantSeatRequested) append(" • [✈️ İnfant Koltuğu Talep Edildi]")
                    if (!p.isPayer) append(" • [👨‍👦 Çocuk Sorumlusu: Turist 1 (Yetişkin)]")
                    if (p.citizenship.isNotBlank()) append(" • [Uyruk: ${p.citizenship}]")
                    if (p.documentExpiryDate.isNotBlank()) append(" • [Pasaport SKT: ${p.documentExpiryDate}]")
                }
                Passenger(
                    id = generateUuid(),
                    bookingId = bookingId,
                    fullName = fullName,
                    tcNo = p.passportSeries.ifBlank { "8492" },
                    passportNo = p.passportNumber.ifBlank { "84920492" },
                    birthDate = p.birthDate.ifBlank { "12.05.1985" },
                    gender = if (p.gender == "MALE") "Bay (Мужской)" else "Bayan (Женский)",
                    phone = p.phone.ifBlank { "+90 532 100 2030" },
                    email = p.email.ifBlank { "ahmet@gmail.com" },
                    isLead = p.isPayer,
                    notes = extraInfo
                )
            }

            // 2. ZENGİN HİZMET & UÇUŞ KALEMLERİ OLUŞTURMA (Konaklama, Uçuş, Surcharges, Sigortalar)
            val domainItems = mutableListOf<BookingItem>()
            
            // Kalem 1: Otel Konaklama Paketi
            domainItems.add(
                BookingItem(
                    id = generateUuid(),
                    bookingId = bookingId,
                    description = "🏨 ${prod.hotelName} (${prod.roomType.ifBlank { "FAMILY ROOM" }}) • ${prod.mealType.ifBlank { "Ultra All Inclusive" }}",
                    quantity = pList.size,
                    unitPrice = (basePrice / pList.size),
                    totalPrice = basePrice,
                    itemType = "HOTEL",
                    notes = "Giriş: ${prod.departureDate ?: "2026-08-21"} (${prod.nights} Gece) • Destinasyon: ${prod.region}"
                )
            )

            // Kalem 2: Uçuş Parkuru Detayı
            if (fl != null) {
                domainItems.add(
                    BookingItem(
                        id = generateUuid(),
                        bookingId = bookingId,
                        description = "🛫 UÇUŞ: Gidiş ${fl.outboundAirline} (${fl.outboundFlightNumber}) ${fl.outboundDeparturePort}->${fl.outboundArrivalPort} (02:05-06:45) | Dönüş ${fl.inboundAirline} (${fl.inboundFlightNumber}) ${fl.inboundDeparturePort}->${fl.inboundArrivalPort} (18:40-23:05)",
                        quantity = pList.size,
                        unitPrice = 0.0,
                        totalPrice = 0.0,
                        itemType = "FLIGHT",
                        notes = "El Bagajı: ${fl.handBaggageKg}kg • Kayıtlı Bagaj: ${fl.baggageKg}kg"
                    )
                )
            }

            // Kalem 3: Uçuş Farkı ve Zorunlu Surcharges Dökümü (Screenshot_2544 Stili)
            domainItems.add(
                BookingItem(
                    id = generateUuid(),
                    bookingId = bookingId,
                    description = "⚡ Zorunlu Uçuş Farkları & Surcharges (THY Uçuş Farkı + Sabah/Akşam Uçuş Ek Ücreti + Transfer)",
                    quantity = 1,
                    unitPrice = 66646.0,
                    totalPrice = 66646.0,
                    itemType = "SURCHARGE",
                    notes = "Dönüş Uçuş Farkı (+34.333 RUB), Sabah Gidiş (+14.137 RUB), Akşam Dönüş (+18.176 RUB), Havalimanı Transferi (Dahil)"
                )
            )

            // Kalem 4..N: Seçilen Ekstra Hizmet ve Sigortalar
            extraServices.value.filter { it.isSelected }.forEach { srv ->
                val totalRub = srv.unitPriceEur * srv.paxCount * 100.0
                domainItems.add(
                    BookingItem(
                        id = generateUuid(),
                        bookingId = bookingId,
                        description = "🛡️ ${srv.name}",
                        quantity = srv.paxCount,
                        unitPrice = srv.unitPriceEur * 100.0,
                        totalPrice = totalRub,
                        itemType = srv.category,
                        notes = "Birim: ${srv.unitPriceEur} EUR/Pax (${srv.paxCount} Yolcu Dahil)"
                    )
                )
            }

            val operatorTitle = prod.operatorName.ifBlank { "Coral Travel / Anex Tour B2B" }

            val domainBooking = Booking(
                id = bookingId,
                bookingCode = pnr,
                customerName = payerName,
                customerEmail = mainPayer?.email?.ifBlank { "acente@touros.com" },
                customerPhone = mainPayer?.phone?.ifBlank { "+90 500 000 0000" },
                totalPrice = totalPriceRub,
                currency = prod.currency.ifBlank { "RUB" },
                paxCount = pList.size,
                status = BookingStatus.ONAYLANDI,
                operatorName = operatorTitle,
                productName = "${prod.tourName.ifBlank { prod.hotelName }} (${prod.hotelName})",
                departureDate = prod.departureDate ?: "2026-08-21",
                nights = prod.nights,
                bookingType = "PACKAGE_TOUR",
                roomTypeName = prod.roomType.ifBlank { "DELUXE ROOM" },
                notes = "🏢 Acente: Coral Travel B2B • Operatör: $operatorTitle • Uçuş: ${fl?.outboundAirline ?: "THY"} / ${fl?.inboundAirline ?: "Pegasus"}",
                tenantId = "00000000-0000-0000-0000-000000000001",
                items = domainItems,
                passengers = domainPassengers
            )

            // BookingRepository (Önbellek + Supabase) Üzerinden Kaydet
            bookingRepository.createBooking(domainBooking)
                .onSuccess {
                    println("✅ Rezervasyon BookingRepository ile önbellek ve Supabase'e başarıyla kaydedildi: PNR $pnr")
                }.onFailure { err ->
                    println("⚠️ BookingRepository kayıt uyarısı: ${err.message}")
                }

            isSavingBooking.value = false
            onComplete(pnr)
        }
    }

}
