package com.mgacreative.touros.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Tur Kalkış Tarihi Seçenek Modeli.
 */
@Serializable
data class DepartureOption(
    val id: String = "",
    @SerialName("departure_date") val departureDate: String = "",
    @SerialName("return_date") val returnDate: String? = null,
    val price: Double? = null,
    val status: String = "planned"
)

/**
 * 4.2.2 B2C Tur Detay Modeli.
 */
@Serializable
data class B2CTourDetail(
    @SerialName("tour_id") val tourId: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    @SerialName("destination_country") val destinationCountry: String = "",
    @SerialName("duration_days") val durationDays: Int = 1,
    val price: Double = 0.0,
    val currency: String = "TRY",
    val rating: Double = 0.0,
    @SerialName("cover_image_url") val coverImageUrl: String? = null,
    @SerialName("included_services") val includedServices: List<String> = emptyList(),
    @SerialName("excluded_services") val excludedServices: List<String> = emptyList(),
    @SerialName("itinerary_summary") val itinerarySummary: String = "",
    @SerialName("agency_name") val agencyName: String = "",
    @SerialName("bank_name") val bankName: String? = null,
    val iban: String? = null,
    @SerialName("account_holder") val accountHolder: String? = null,
    @SerialName("paypal_email") val paypalEmail: String? = null,
    @SerialName("paypal_me_url") val paypalMeUrl: String? = null,
    @SerialName("available_departures") val availableDepartures: List<DepartureOption> = emptyList()
)
