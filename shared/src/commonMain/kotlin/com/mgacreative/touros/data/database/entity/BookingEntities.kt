package com.mgacreative.touros.data.database.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * bookings tablosu – Rezervasyon entity.
 */
@Serializable
data class BookingEntity(
    val id: String = "",
    @SerialName("booking_code") val bookingCode: String = "",
    @SerialName("departure_id") val departureId: String = "",
    @SerialName("customer_id") val customerId: String? = null,
    @SerialName("agency_id") val agencyId: String? = null,
    @SerialName("customer_name") val customerName: String = "",
    @SerialName("customer_email") val customerEmail: String? = null,
    @SerialName("customer_phone") val customerPhone: String? = null,
    @SerialName("total_price") val totalPrice: Double = 0.0,
    val currency: String = "TRY",
    @SerialName("pax_count") val paxCount: Int = 1,
    val status: String = "Bekliyor",
    val notes: String? = null,
    @SerialName("option_expiration") val optionExpiration: String? = null,
    @SerialName("confirmed_at") val confirmedAt: String? = null,
    @SerialName("cancelled_at") val cancelledAt: String? = null,
    @SerialName("tenant_id") val tenantId: String = "",
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = "",
    @SerialName("created_by") val createdBy: String? = null
)

/**
 * booking_items tablosu – Rezervasyon kalem detayı entity.
 */
@Serializable
data class BookingItemEntity(
    val id: String = "",
    @SerialName("booking_id") val bookingId: String = "",
    val description: String = "",
    val quantity: Int = 1,
    @SerialName("unit_price") val unitPrice: Double = 0.0,
    @SerialName("total_price") val totalPrice: Double = 0.0,
    @SerialName("item_type") val itemType: String = "service",
    val notes: String? = null,
    @SerialName("tenant_id") val tenantId: String = "",
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = "",
    @SerialName("created_by") val createdBy: String? = null
)

/**
 * passengers tablosu – Yolcu bilgileri entity.
 */
@Serializable
data class PassengerEntity(
    val id: String = "",
    @SerialName("booking_id") val bookingId: String = "",
    @SerialName("full_name") val fullName: String = "",
    @SerialName("tc_no") val tcNo: String? = null,
    @SerialName("passport_no") val passportNo: String? = null,
    @SerialName("birth_date") val birthDate: String? = null,
    val gender: String? = null,
    val phone: String? = null,
    val email: String? = null,
    @SerialName("is_lead") val isLead: Boolean = false,
    val notes: String? = null,
    @SerialName("tenant_id") val tenantId: String = "",
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = "",
    @SerialName("created_by") val createdBy: String? = null
)
