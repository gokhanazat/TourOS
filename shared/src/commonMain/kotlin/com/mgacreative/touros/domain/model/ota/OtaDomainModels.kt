package com.mgacreative.touros.domain.model.ota

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 4.5.1 OTA Kanal Sağlayıcı Modeli.
 */
@Serializable
data class OTAProvider(
    @SerialName("provider_id") val providerId: String = "p-viator",
    @SerialName("provider_name") val providerName: String = "Viator / TripAdvisor",
    @SerialName("api_endpoint") val apiEndpoint: String = "https://api.viator.com/partner/v1",
    @SerialName("supports_webhooks") val supportsWebhooks: Boolean = true,
    @SerialName("is_active") val isActive: Boolean = true
)

/**
 * 4.5.1 OTA Hesabı Modeli.
 */
@Serializable
data class OTAAccount(
    @SerialName("account_id") val accountId: String = "acc-001",
    @SerialName("provider_id") val providerId: String = "p-viator",
    @SerialName("account_name") val accountName: String = "Viator EU Partner Account",
    @SerialName("api_key") val apiKey: String = "vtr_live_key_9988",
    @SerialName("api_secret") val apiSecret: String = "vtr_sec_****",
    @SerialName("tenant_id") val tenantId: String = "tenant-001",
    val status: String = "ACTIVE"
)

/**
 * 4.5.1 OTA Bağlantı Sağlığı Modeli.
 */
@Serializable
data class OTAConnection(
    @SerialName("connection_id") val connectionId: String = "conn-001",
    @SerialName("account_id") val accountId: String = "acc-001",
    val status: String = "CONNECTED",
    @SerialName("last_synced_at") val lastSyncedAt: String = "2026-08-06T14:00:00Z",
    @SerialName("error_count") val errorCount: Int = 0
)

/**
 * 4.5.1 OTA Rezervasyon Statü Enum.
 */
enum class OTABookingStatus {
    PENDING, CONFIRMED, CANCELLED, MODIFIED, FAILED
}

/**
 * 4.5.1 OTA Rezervasyon Modeli.
 */
@Serializable
data class OTABooking(
    @SerialName("ota_booking_id") val otaBookingId: String = "ota-bkg-101",
    @SerialName("account_id") val accountId: String = "acc-001",
    @SerialName("ota_reference") val otaReference: String = "VTR-88776655",
    @SerialName("booking_id") val bookingId: String? = "bkg-5501",
    val status: OTABookingStatus = OTABookingStatus.CONFIRMED,
    @SerialName("total_amount") val totalAmount: Double = 350.0,
    val currency: String = "EUR",
    @SerialName("pax_count") val paxCount: Int = 2,
    @SerialName("raw_payload") val rawPayload: String = "{}"
)

/**
 * 4.5.1 OTA Ürün Eşleme Modeli.
 */
@Serializable
data class OTAProduct(
    @SerialName("ota_product_id") val otaProductId: String = "ota-prd-301",
    @SerialName("tour_id") val tourId: String = "tour-kapadokya",
    @SerialName("account_id") val accountId: String = "acc-001",
    @SerialName("external_product_code") val externalProductCode: String = "VTR-CAP-01",
    val title: String = "Kapadokya Balon & Mağara Turu",
    @SerialName("mapped_tour_id") val mappedTourId: String = "tour-kapadokya"
)

/**
 * 4.5.1 OTA Müsaitlik Modeli.
 */
@Serializable
data class OTAAvailability(
    @SerialName("availability_id") val availabilityId: String = "avail-401",
    @SerialName("ota_product_id") val otaProductId: String = "ota-prd-301",
    @SerialName("departure_id") val departureId: String = "dep-2026-08-10",
    val date: String = "2026-08-10",
    @SerialName("available_capacity") val availableCapacity: Int = 18,
    val price: Double = 175.0
)

/**
 * 4.5.1 OTA Fiyatlandırma Modeli.
 */
@Serializable
data class OTAPrice(
    @SerialName("price_id") val priceId: String = "prc-501",
    @SerialName("ota_product_id") val otaProductId: String = "ota-prd-301",
    val currency: String = "EUR",
    @SerialName("adult_price") val adultPrice: Double = 175.0,
    @SerialName("child_price") val childPrice: Double = 120.0,
    @SerialName("infant_price") val infantPrice: Double = 0.0,
    @SerialName("valid_from") val validFrom: String = "2026-01-01",
    @SerialName("valid_to") val validTo: String = "2026-12-31"
)

/**
 * 4.5.1 OTA Yolcu / Müşteri Kayıt Modeli.
 */
@Serializable
data class OTAReservation(
    @SerialName("reservation_id") val reservationId: String = "res-601",
    @SerialName("ota_booking_id") val otaBookingId: String = "ota-bkg-101",
    @SerialName("customer_id") val customerId: String = "cust-701",
    @SerialName("passenger_name") val passengerName: String = "Hans Müller",
    @SerialName("passenger_email") val passengerEmail: String = "hans.muller@example.de",
    @SerialName("passport_no") val passportNo: String = "C99887711"
)

/**
 * 4.5.1 OTA Webhook Olay Modeli.
 */
@Serializable
data class OTAWebhook(
    @SerialName("webhook_id") val webhookId: String = "wh-701",
    @SerialName("provider_id") val providerId: String = "p-viator",
    @SerialName("event_type") val eventType: String = "BOOKING_CREATED",
    val payload: String = "{}",
    @SerialName("received_at") val receivedAt: String = "2026-08-06T14:10:00Z",
    val processed: Boolean = true
)

/**
 * 4.5.1 OTA Hata Modeli.
 */
@Serializable
data class OTAError(
    @SerialName("error_code") val errorCode: String = "OTA_SYNC_TIMEOUT",
    @SerialName("error_message") val errorMessage: String = "Viator API yanıt vermedi.",
    @SerialName("ota_provider") val otaProvider: String = "Viator",
    val timestamp: String = "2026-08-06T14:15:00Z"
)

/**
 * 4.5.1 OTA Kanal Modeli.
 */
@Serializable
data class OTAChannel(
    @SerialName("channel_id") val channelId: String = "chn-01",
    @SerialName("channel_name") val channelName: String = "GetYourGuide Direct Channel",
    val code: String = "GYG",
    @SerialName("commission_rate") val commissionRate: Double = 20.0
)

/**
 * 4.5.1 OTA Envanter Modeli.
 */
@Serializable
data class OTAInventory(
    @SerialName("inventory_id") val inventoryId: String = "inv-801",
    @SerialName("ota_product_id") val otaProductId: String = "ota-prd-301",
    @SerialName("total_quota") val totalQuota: Int = 30,
    @SerialName("booked_quota") val bookedQuota: Int = 12,
    @SerialName("remaining_quota") val remainingQuota: Int = 18
)

/**
 * 4.5.1 OTA Sonuç Mühürlü Sınıfı (Sealed Class).
 */
sealed class OTAResult<out T> {
    data class Success<out T>(val data: T) : OTAResult<T>()
    data class Failure(val error: OTAError) : OTAResult<Nothing>()
}
