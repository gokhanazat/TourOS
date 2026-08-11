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
    @SerialName("departure_id") val departureId: String? = null,
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
    @SerialName("operator_name") val operatorName: String? = "MGA Creative",
    @SerialName("product_name") val productName: String? = "Kapadokya Turu",
    @SerialName("departure_date") val departureDate: String? = "2026-09-01",
    @SerialName("hotel_id") val hotelId: String? = null,
    @SerialName("check_in_date") val checkInDate: String? = null,
    @SerialName("check_out_date") val checkOutDate: String? = null,
    @SerialName("room_type_name") val roomTypeName: String? = null,
    val nights: Int = 1,
    @SerialName("booking_type") val bookingType: String? = "TOUR",
    @SerialName("payment_method") val paymentMethod: String? = "CREDIT_CARD",
    @SerialName("net_cost") val netCost: Double = 0.0,
    @SerialName("gross_sales") val grossSales: Double = 0.0,
    @SerialName("profit_margin") val profitMargin: Double = 0.0,
    @SerialName("commission_rate") val commissionRate: Double = 0.0,
    @SerialName("incoming_voucher_code") val incomingVoucherCode: String? = null,
    @SerialName("is_bsp") val isBsp: Boolean = false,
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
    @SerialName("parent_passenger_id") val parentPassengerId: String? = null,
    @SerialName("is_infant_seat_requested") val isInfantSeatRequested: Boolean = false,
    @SerialName("country_of_birth") val countryOfBirth: String? = null,
    @SerialName("document_issue_date") val documentIssueDate: String? = null,
    @SerialName("document_expire_date") val documentExpireDate: String? = null,
    val notes: String? = null,
    @SerialName("tenant_id") val tenantId: String = "",
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = "",
    @SerialName("created_by") val createdBy: String? = null
)
