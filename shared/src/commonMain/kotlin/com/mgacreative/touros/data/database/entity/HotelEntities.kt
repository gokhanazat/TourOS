package com.mgacreative.touros.data.database.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * hotels tablosu – Otel tanımları entity.
 */
@Serializable
data class HotelEntity(
    val id: String = "",
    val name: String = "",
    val slug: String = "",
    @SerialName("star_rating") val starRating: Int? = null,
    val address: String? = null,
    val city: String? = null,
    val country: String = "TR",
    val phone: String? = null,
    val email: String? = null,
    val website: String? = null,
    val description: String? = null,
    @SerialName("cover_image_url") val coverImageUrl: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("tenant_id") val tenantId: String = "",
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = "",
    @SerialName("created_by") val createdBy: String? = null
)

/**
 * room_types tablosu – Oda tipi + kontenjan entity.
 */
@Serializable
data class RoomTypeEntity(
    val id: String = "",
    @SerialName("hotel_id") val hotelId: String = "",
    val name: String = "",
    val description: String? = null,
    @SerialName("base_price_per_night") val basePricePerNight: Double = 0.0,
    val currency: String = "TRY",
    @SerialName("max_occupancy") val maxOccupancy: Int = 2,
    @SerialName("total_rooms") val totalRooms: Int = 0,
    val allotment: Int = 0,
    @SerialName("booked_rooms") val bookedRooms: Int = 0,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("tenant_id") val tenantId: String = "",
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = "",
    @SerialName("created_by") val createdBy: String? = null
)

/**
 * hotel_contracts tablosu – Sezonluk otel kontratı entity.
 */
@Serializable
data class HotelContractEntity(
    val id: String = "",
    @SerialName("hotel_id") val hotelId: String = "",
    @SerialName("room_type_id") val roomTypeId: String? = null,
    @SerialName("season_name") val seasonName: String = "",
    @SerialName("start_date") val startDate: String = "",
    @SerialName("end_date") val endDate: String = "",
    @SerialName("price_per_night") val pricePerNight: Double = 0.0,
    val currency: String = "TRY",
    val allotment: Int = 0,
    @SerialName("release_days") val releaseDays: Int = 7,
    @SerialName("meal_plan") val mealPlan: String = "BB",
    val notes: String? = null,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("tenant_id") val tenantId: String = "",
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = "",
    @SerialName("created_by") val createdBy: String? = null
)

/**
 * hotel_season_rates tablosu – Sezon Fiyat Matrisi entity.
 */
@Serializable
data class HotelSeasonRateEntity(
    val id: String = "",
    @SerialName("hotel_id") val hotelId: String = "",
    @SerialName("room_type_id") val roomTypeId: String? = null,
    @SerialName("room_type_name") val roomTypeName: String? = null,
    @SerialName("season_name") val seasonName: String = "",
    @SerialName("start_date") val startDate: String = "",
    @SerialName("end_date") val endDate: String = "",
    @SerialName("single_price") val singlePrice: Double = 0.0,
    @SerialName("double_price") val doublePrice: Double = 0.0,
    @SerialName("triple_price") val triplePrice: Double = 0.0,
    @SerialName("extra_bed_price") val extraBedPrice: Double = 0.0,
    @SerialName("child_price") val childPrice: Double = 0.0,
    @SerialName("cost_price") val costPrice: Double = 0.0,
    @SerialName("sale_price") val salePrice: Double = 0.0,
    val allotment: Int = 10,
    val currency: String = "TRY",
    @SerialName("meal_plan") val mealPlan: String = "BB",
    @SerialName("min_stay_days") val minStayDays: Int = 1,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("tenant_id") val tenantId: String = "",
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = "",
    @SerialName("created_by") val createdBy: String? = null
)

/**
 * hotel_stop_sales tablosu – Stop Sale & Release kayıtları entity.
 */
@Serializable
data class HotelStopSaleEntity(
    val id: String = "",
    @SerialName("hotel_id") val hotelId: String = "",
    @SerialName("room_type_id") val roomTypeId: String? = null,
    @SerialName("action_type") val actionType: String = "STOP_SALE", // STOP_SALE | RELEASE
    @SerialName("start_date") val startDate: String = "",
    @SerialName("end_date") val endDate: String = "",
    val reason: String? = null,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("tenant_id") val tenantId: String = "",
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = "",
    @SerialName("created_by") val createdBy: String? = null
)


