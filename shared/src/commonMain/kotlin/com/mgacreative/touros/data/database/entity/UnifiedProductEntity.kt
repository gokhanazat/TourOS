package com.mgacreative.touros.data.database.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * public.marketplace_products tablosu – Operatör API/SOAP ve Manuel yüklemeler için Ortak Ürün Entity.
 */
@Serializable
data class UnifiedProductEntity(
    val id: String = "",
    @SerialName("product_type") val productType: String = "PACKAGE_TOUR",
    @SerialName("tour_name") val tourName: String = "",
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
    @SerialName("departure_date") val departureDate: String? = null,
    val nights: Int = 7,
    val adults: Int = 2,
    val childs: Int = 0,
    @SerialName("is_charter") val isCharter: Boolean = true,
    @SerialName("is_promo") val isPromo: Boolean = false,
    @SerialName("airline_name") val airlineName: String = "",
    @SerialName("flight_number") val flightNumber: String = "",
    @SerialName("baggage_kg") val baggageKg: Int = 20,
    @SerialName("picture_url") val pictureUrl: String = "",
    val picture: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    @SerialName("hotel_rating") val hotelRating: Double = 8.0,
    @SerialName("beach_line") val beachLine: Int = 1,
    @SerialName("is_instant_confirmation") val isInstantConfirmation: Boolean = true,
    @SerialName("has_transfer") val hasTransfer: Boolean = true,
    @SerialName("is_direct_flight") val isDirectFlight: Boolean = true,
    val amenities: List<String> = emptyList(),
    @SerialName("is_published") val isPublished: Boolean = true,
    @SerialName("custom_price_override") val customPriceOverride: Double? = null,
    @SerialName("created_at") val createdAt: String? = null
) {
    val safeProductType: String get() = productType
    val safeTourName: String get() = tourName
    val safeOperatorId: Int get() = operatorId
    val safeOperatorName: String get() = operatorName
    val safeOperatorLink: String get() = operatorLink
    val safePrice: Double get() = price
    val safeFuelCharge: Double get() = fuelCharge
    val safeCurrency: String get() = currency
    val safeHotelId: Int get() = hotelId
    val safeHotelName: String get() = hotelName
    val safeHotelCategory: Int get() = hotelCategory
    val safeCountry: String get() = country
    val safeRegion: String get() = region
    val safeSubRegion: String get() = subRegion
    val safeRoomType: String get() = roomType
    val safeMealType: String get() = mealType
    val safeDepartureCity: String get() = departureCity
    val safeNights: Int get() = nights
    val safeAdults: Int get() = adults
    val safeChilds: Int get() = childs
    val safeIsCharter: Boolean get() = isCharter
    val safeIsPromo: Boolean get() = isPromo
    val safeAirlineName: String get() = airlineName
    val safeFlightNumber: String get() = flightNumber
    val safeBaggageKg: Int get() = baggageKg
    val safePictureUrl: String get() = pictureUrl.ifBlank { picture }
}
