package com.mgacreative.touros.data.tourvisor

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * public.tour_actualizations satırı.
 * Tourvisor tek tur sorgusunun (actualize.php + actdetail.php) sonucu; sunucudaki
 * scripts/tourvisor_actualize_worker.py tarafından doldurulur. Tutarlar RUB'dur.
 *
 * Bu dosya Search modülüne ait DEĞİLDİR; arama koduna dokunmaz.
 */
@Serializable
data class TourActualizationDto(
    @SerialName("tour_id") val tourId: String = "",
    val status: String = "pending",
    @SerialName("error_message") val errorMessage: String? = null,
    @SerialName("requested_at") val requestedAt: String? = null,
    @SerialName("fetched_at") val fetchedAt: String? = null,
    val price: Double? = null,
    val currency: String? = null,
    @SerialName("fuel_charge") val fuelCharge: Double? = null,
    /** KİŞİ BAŞI vize ücreti */
    @SerialName("visa_charge") val visaCharge: Double? = null,
    @SerialName("operator_price") val operatorPrice: Double? = null,
    @SerialName("operator_currency") val operatorCurrency: String? = null,
    @SerialName("operator_link") val operatorLink: String? = null,
    /** 1 = talep üzerine, 2 = anında onay */
    @SerialName("hotel_status") val hotelStatus: Int? = null,
    /** 1 = talep üzerine, 2 = az yer */
    @SerialName("flight_status") val flightStatus: Int? = null,
    @SerialName("on_request") val onRequest: Boolean? = null,
    @SerialName("night_flight") val nightFlight: Int? = null,
    val placement: String? = null,
    val adults: Int? = null,
    val child: Int? = null,
    val flights: List<TvFlightSet>? = null,
    @SerialName("add_payments") val addPayments: List<TvAddPayment>? = null,
    val contents: List<String>? = null,
    val flags: TvTourFlags? = null
) {
    val isOk: Boolean get() = status == "ok"
    val isPending: Boolean get() = status == "pending"
    val safeFlights: List<TvFlightSet> get() = flights.orEmpty()
    val safeAddPayments: List<TvAddPayment> get() = addPayments.orEmpty()
    val safeContents: List<String> get() = contents.orEmpty()
    val safeFlags: TvTourFlags get() = flags ?: TvTourFlags()

    /** Zorunlu ek ödemeler + vize, verilen kişi sayısı için (RUB). */
    fun mandatoryExtrasTotal(paxCount: Int): Double {
        val pax = paxCount.coerceAtLeast(1)
        val payments = safeAddPayments.sumOf { it.amount ?: 0.0 } * pax
        val visa = (visaCharge ?: 0.0) * pax
        return payments + visa
    }

    /**
     * Operatördeki güncel tur fiyatı (RUB). Ürün RUB değilse veya fiyat yoksa null döner;
     * bu durumda listedeki fiyat kullanılır (kur dönüşümü uydurulmaz).
     */
    fun livePriceFor(productCurrency: String): Double? =
        price?.takeIf { isOk && it > 0 && productCurrency.equals("RUB", ignoreCase = true) }

    /** Operatörün varsayılan uçuş seti (işaretli değilse ilk set). */
    val defaultFlightSet: TvFlightSet?
        get() = safeFlights.firstOrNull { it.isDefault } ?: safeFlights.firstOrNull()
}

@Serializable
data class TvFlightSet(
    @SerialName("is_default") val isDefault: Boolean = false,
    @SerialName("date_forward") val dateForward: String? = null,
    @SerialName("date_backward") val dateBackward: String? = null,
    /** Bu uçuş setiyle turun toplam fiyatı (oda başı) */
    val price: Double? = null,
    val currency: String? = null,
    @SerialName("fuel_charge") val fuelCharge: Double? = null,
    val forward: List<TvFlightLeg> = emptyList(),
    val backward: List<TvFlightLeg> = emptyList()
)

@Serializable
data class TvFlightLeg(
    @SerialName("airline_code") val airlineCode: String? = null,
    @SerialName("airline_name") val airlineName: String? = null,
    @SerialName("airline_logo") val airlineLogo: String? = null,
    val number: String? = null,
    val plane: String? = null,
    @SerialName("flight_class") val flightClass: String? = null,
    @SerialName("departure_date") val departureDate: String? = null,
    @SerialName("departure_time") val departureTime: String? = null,
    @SerialName("departure_port") val departurePort: String? = null,
    @SerialName("arrival_date") val arrivalDate: String? = null,
    @SerialName("arrival_time") val arrivalTime: String? = null,
    @SerialName("arrival_port") val arrivalPort: String? = null,
    /** Operatör kesin uçuşu henüz bildirmedi (Tourvisor "SU000 / 00:00" yer tutucusu) */
    @SerialName("is_placeholder") val isPlaceholder: Boolean = false,
    @SerialName("baggage_included") val baggageIncluded: Boolean? = null,
    @SerialName("baggage_kg") val baggageKg: Int? = null,
    /** Yerler talep üzerine */
    @SerialName("on_demand") val onDemand: Boolean = false,
    /** Yer yok */
    @SerialName("no_places") val noPlaces: Boolean = false
)

@Serializable
data class TvAddPayment(
    val name: String = "",
    /** KİŞİ BAŞI tutar */
    val amount: Double? = null
)

@Serializable
data class TvTourFlags(
    val notransfer: Boolean = false,
    val nomedinsurance: Boolean = false,
    val noflight: Boolean = false,
    val nomeal: Boolean = false
)

/** Panelde gösterilecek durum. */
sealed class TourActualizationState {
    /** Ürün Tourvisor'dan gelmiyor (yerel tur/otel vb.) — canlı sorgu yapılmaz. */
    data object NotApplicable : TourActualizationState()
    data object Loading : TourActualizationState()
    data class Ready(val data: TourActualizationDto) : TourActualizationState()
    /** Bilgi alınamadı; reason: disabled | quota_exceeded | error | timeout | network */
    data class Unavailable(val reason: String, val message: String? = null) : TourActualizationState()
}

/** marketplace_products.id = "tv-<tourid>" ise Tourvisor tur numarasını döndürür. */
fun tourvisorTourIdOf(productId: String?): String? {
    if (productId.isNullOrBlank()) return null
    val raw = productId.trim()
    if (!raw.startsWith("tv-", ignoreCase = true)) return null
    val digits = raw.substring(3)
    return if (digits.length in 5..30 && digits.all { it.isDigit() }) digits else null
}
