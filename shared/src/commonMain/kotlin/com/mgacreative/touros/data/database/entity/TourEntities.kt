package com.mgacreative.touros.data.database.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * tour_categories tablosu – Tur kategorileri entity.
 */
@Serializable
data class TourCategoryEntity(
    val id: String = "",
    val name: String = "",
    val slug: String = "",
    val description: String? = null,
    @SerialName("icon_url") val iconUrl: String? = null,
    @SerialName("sort_order") val sortOrder: Int = 0,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("tenant_id") val tenantId: String = "",
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = "",
    @SerialName("created_by") val createdBy: String? = null
)

/**
 * tours tablosu – Tur tanımları entity.
 */
@Serializable
data class TourEntity(
    val id: String = "",
    val code: String = "",
    val title: String = "",
    val category: String = "",
    val country: String = "",
    val city: String = "",
    @SerialName("category_id") val categoryId: String? = null,
    val slug: String = "",
    val description: String? = null,
    @SerialName("cover_image_url") val coverImageUrl: String? = null,
    @SerialName("duration_days") val durationDays: Int = 1,
    @SerialName("base_price") val basePrice: Double = 0.0,
    val currency: String = "TRY",
    val capacity: Int = 20,
    @SerialName("min_participants") val minParticipants: Int = 1,
    @SerialName("max_participants") val maxParticipants: Int = 30,
    @SerialName("cancellation_policy") val cancellationPolicy: String? = null,
    @SerialName("insurance_details") val insuranceDetails: String? = null,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("tenant_id") val tenantId: String = "",
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = "",
    @SerialName("created_by") val createdBy: String? = null
)

/**
 * departures tablosu – Tur çıkış tarihleri entity.
 */
@Serializable
data class DepartureEntity(
    val id: String = "",
    @SerialName("tour_id") val tourId: String = "",
    @SerialName("departure_date") val departureDate: String = "",
    @SerialName("return_date") val returnDate: String? = null,
    @SerialName("price_override") val priceOverride: Double? = null,
    @SerialName("child_price_override") val childPriceOverride: Double? = null,
    @SerialName("infant_price_override") val infantPriceOverride: Double? = null,
    val currency: String = "TRY",
    val capacity: Int? = null,
    @SerialName("booked_count") val bookedCount: Int = 0,
    @SerialName("option_deadline_days") val optionDeadlineDays: Int = 7,
    @SerialName("is_guaranteed") val isGuaranteed: Boolean = false,
    val status: String = "planned",
    val notes: String? = null,
    @SerialName("tenant_id") val tenantId: String = "",
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = "",
    @SerialName("created_by") val createdBy: String? = null
)

@Serializable
data class DepartureHotelEntity(
    val id: String = "",
    @SerialName("departure_id") val departureId: String = "",
    @SerialName("hotel_id") val hotelId: String = "",
    @SerialName("night_count") val nightCount: Int = 1,
    @SerialName("sort_order") val sortOrder: Int = 1,
    @SerialName("tenant_id") val tenantId: String = ""
)

/**
 * itineraries tablosu – Gün bazlı program entity.
 */
@Serializable
data class ItineraryEntity(
    val id: String = "",
    @SerialName("tour_id") val tourId: String = "",
    @SerialName("day_number") val dayNumber: Int = 1,
    val title: String = "",
    val description: String? = null,
    val location: String? = null,
    @SerialName("start_time") val startTime: String? = null,
    @SerialName("end_time") val endTime: String? = null,
    @SerialName("sort_order") val sortOrder: Int = 0,
    @SerialName("tenant_id") val tenantId: String = "",
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = "",
    @SerialName("created_by") val createdBy: String? = null
)
