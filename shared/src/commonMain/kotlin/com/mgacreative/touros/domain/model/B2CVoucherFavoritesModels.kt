package com.mgacreative.touros.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 4.2.4 B2C Müşteri Voucher Belge Modeli.
 */
@Serializable
data class B2CCustomerVoucherItem(
    @SerialName("voucher_id") val voucherId: String = "v101",
    @SerialName("booking_code") val bookingCode: String = "MOB-2608-9900",
    @SerialName("tour_title") val tourTitle: String = "Kapadokya Balon & Vadi Turu",
    @SerialName("hotel_name") val hotelName: String = "Cave Hotel & Spa",
    @SerialName("departure_date") val departureDate: String = "15.08.2026",
    @SerialName("pax_count") val paxCount: Int = 2,
    @SerialName("pdf_url") val pdfUrl: String = "https://touros.storage.supabase.co/documents/voucher/v101.pdf",
    @SerialName("created_at") val createdAt: String = "2026-08-06 14:20"
)

/**
 * 4.2.4 B2C Müşteri Favori Tur Modeli.
 */
@Serializable
data class B2CFavoriteTourItem(
    val tourId: String = "t101",
    val tourTitle: String = "Kapadokya Balon & Vadi Turu",
    val category: String = "Kültür Turu",
    val price: Double = 2500.0,
    val rating: Double = 4.90,
    val isFavorited: Boolean = true
)
