package com.mgacreative.touros.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 4.2.3 Dijital QR Bilet Modeli.
 */
@Serializable
data class B2CQRTicket(
    @SerialName("ticket_id") val ticketId: String = "tkt-101",
    @SerialName("booking_code") val bookingCode: String = "MOB-2608-9900",
    @SerialName("ticket_hash") val ticketHash: String = "QR-TKT-9A8B7C6D5E4F",
    @SerialName("qr_payload") val qrPayload: String = "{\"ticket_hash\":\"QR-TKT-9A8B7C6D5E4F\",\"booking_code\":\"MOB-2608-9900\"}",
    @SerialName("passenger_name") val passengerName: String = "Elif Yılmaz",
    @SerialName("tour_title") val tourTitle: String = "Kapadokya Balon & Vadi Turu",
    @SerialName("pax_count") val paxCount: Int = 2,
    @SerialName("checkin_status") val checkinStatus: String = "PENDING", // PENDING, CHECKED_IN
    @SerialName("checked_in_at") val checkedInAt: String? = null
)

/**
 * 4.2.3 QR Tarama ve Giriş Kontrol Sonuç Modeli.
 */
@Serializable
data class QRCheckInResult(
    @SerialName("validation_status") val validationStatus: String = "VALID",
    @SerialName("booking_code") val bookingCode: String = "MOB-2608-9900",
    @SerialName("passenger_name") val passengerName: String = "Elif Yılmaz",
    @SerialName("pax_count") val paxCount: Int = 2,
    val message: String = "✅ QR Bilet Doğrulandı! Otobüs/Tur Girişi Onaylandı.",
    @SerialName("checkin_time") val checkinTime: String = "2026-08-06 14:23"
)
