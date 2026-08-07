package com.mgacreative.touros.data.dto.ota

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OtaBookingDto(
    @SerialName("booking_ref") val bookingRef: String = "",
    @SerialName("product_code") val productCode: String = "",
    @SerialName("status") val status: String = "CONFIRMED",
    @SerialName("total_price") val totalPrice: Double = 0.0,
    @SerialName("currency") val currency: String = "EUR",
    @SerialName("pax_count") val paxCount: Int = 1,
    @SerialName("created_at") val createdAt: String = ""
)

@Serializable
data class OtaProductDto(
    @SerialName("product_code") val productCode: String = "",
    @SerialName("title") val title: String = "",
    @SerialName("description") val description: String? = null
)

@Serializable
data class OtaPriceDto(
    @SerialName("product_code") val productCode: String = "",
    @SerialName("currency") val currency: String = "EUR",
    @SerialName("adult_price") val adultPrice: Double = 0.0,
    @SerialName("child_price") val childPrice: Double = 0.0
)

@Serializable
data class OtaAvailabilityDto(
    @SerialName("product_code") val productCode: String = "",
    @SerialName("total_quota") val totalQuota: Int = 30,
    @SerialName("booked_quota") val bookedQuota: Int = 0
)

@Serializable
data class OtaErrorDto(
    @SerialName("error_code") val errorCode: String = "UNKNOWN",
    @SerialName("message") val message: String = "An OTA network error occurred"
)
