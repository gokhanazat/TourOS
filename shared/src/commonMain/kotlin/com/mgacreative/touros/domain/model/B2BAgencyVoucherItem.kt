package com.mgacreative.touros.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 4.1.4 B2B Acente Voucher İndirme & Yazdırma Modeli.
 */
@Serializable
data class B2BAgencyVoucherItem(
    @SerialName("voucher_id") val voucherId: String = "",
    @SerialName("booking_code") val bookingCode: String = "",
    @SerialName("guest_name") val guestName: String = "",
    @SerialName("tour_title") val tourTitle: String = "",
    @SerialName("hotel_name") val hotelName: String = "",
    @SerialName("departure_date") val departureDate: String = "",
    @SerialName("pax_count") val paxCount: Int = 1,
    @SerialName("pdf_url") val pdfUrl: String = "",
    @SerialName("file_size_bytes") val fileSizeBytes: Long = 1450000L,
    @SerialName("printed_count") val printedCount: Int = 0,
    @SerialName("created_at") val createdAt: String = ""
)
