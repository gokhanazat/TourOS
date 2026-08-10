package com.mgacreative.touros.data.database.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * public.marketplace_products tablosu – Operatör API/SOAP ve Manuel yüklemeler için Ortak Ürün Entity.
 */
@Serializable
data class UnifiedProductEntity(
    val id: String = "",
    @SerialName("product_type") val productType: String = "PACKAGE_TOUR", // 'PACKAGE_TOUR', 'HOTEL', 'FLIGHT'
    @SerialName("tour_name") val tourName: String = "",                   // Örn: "Moscow Antalya PROMO"
    @SerialName("operator_id") val operatorId: Int = 0,
    @SerialName("operator_name") val operatorName: String = "",
    @SerialName("operator_link") val operatorLink: String = "",
    val price: Double = 0.0,
    @SerialName("fuel_charge") val fuelCharge: Double = 0.0,
    val currency: String = "RUB",
    @SerialName("hotel_id") val hotelId: Int = 0,
    @SerialName("hotel_name") val hotelName: String = "",
    @SerialName("hotel_category") val hotelCategory: Int = 5,
    val country: String = "",
    val region: String = "",
    @SerialName("sub_region") val subRegion: String = "",
    @SerialName("room_type") val roomType: String = "",
    @SerialName("meal_type") val mealType: String = "",
    @SerialName("departure_city") val departureCity: String = "",
    @SerialName("departure_date") val departureDate: String? = null,     // Nullable String to prevent Postgres DATE format errors
    val nights: Int = 7,
    val adults: Int = 2,
    val childs: Int = 0,
    @SerialName("is_charter") val isCharter: Boolean = true,
    @SerialName("is_promo") val isPromo: Boolean = false,
    @SerialName("airline_name") val airlineName: String = "",
    @SerialName("flight_number") val flightNumber: String = "",
    @SerialName("baggage_kg") val baggageKg: Int = 20,
    @SerialName("picture_url") val pictureUrl: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    @SerialName("created_at") val createdAt: String? = null              // Nullable so Postgres uses default now()
)
