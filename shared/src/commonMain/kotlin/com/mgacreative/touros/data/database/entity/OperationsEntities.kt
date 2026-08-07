package com.mgacreative.touros.data.database.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * vehicles tablosu – Araç parkı entity.
 */
@Serializable
data class VehicleEntity(
    val id: String = "",
    @SerialName("plate_number") val plateNumber: String = "",
    val brand: String? = null,
    val model: String? = null,
    val year: Int? = null,
    val capacity: Int = 0,
    @SerialName("vehicle_type") val vehicleType: String = "minibus",
    val color: String? = null,
    @SerialName("is_owned") val isOwned: Boolean = true,
    @SerialName("owner_info") val ownerInfo: String? = null,
    @SerialName("insurance_expiry") val insuranceExpiry: String? = null,
    @SerialName("inspection_expiry") val inspectionExpiry: String? = null,
    @SerialName("last_maintenance_date") val lastMaintenanceDate: String? = null,
    @SerialName("next_maintenance_date") val nextMaintenanceDate: String? = null,
    @SerialName("maintenance_notes") val maintenanceNotes: String? = null,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("tenant_id") val tenantId: String = "",
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = "",
    @SerialName("created_by") val createdBy: String? = null
)

/**
 * drivers tablosu – Şoför entity.
 */
@Serializable
data class DriverEntity(
    val id: String = "",
    @SerialName("full_name") val fullName: String = "",
    val phone: String? = null,
    val email: String? = null,
    @SerialName("license_class") val licenseClass: String? = null,
    @SerialName("license_expiry") val licenseExpiry: String? = null,
    @SerialName("tc_no") val tcNo: String? = null,
    @SerialName("birth_date") val birthDate: String? = null,
    val address: String? = null,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("tenant_id") val tenantId: String = "",
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = "",
    @SerialName("created_by") val createdBy: String? = null
)

/**
 * guides tablosu – Rehber entity.
 */
@Serializable
data class GuideEntity(
    val id: String = "",
    @SerialName("full_name") val fullName: String = "",
    val phone: String? = null,
    val email: String? = null,
    @SerialName("license_number") val licenseNumber: String? = null,
    val languages: List<String>? = null,
    val specialization: String? = null,
    @SerialName("tc_no") val tcNo: String? = null,
    @SerialName("birth_date") val birthDate: String? = null,
    val rating: Double = 5.0,
    @SerialName("total_tours_completed") val totalToursCompleted: Int = 0,
    val notes: String? = null,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("tenant_id") val tenantId: String = "",
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = "",
    @SerialName("created_by") val createdBy: String? = null
)

/**
 * guide_reviews tablosu – Rehber değerlendirme entity.
 */
@Serializable
data class GuideReviewEntity(
    val id: String = "",
    @SerialName("guide_id") val guideId: String = "",
    @SerialName("departure_id") val departureId: String = "",
    @SerialName("booking_id") val bookingId: String? = null,
    @SerialName("customer_name") val customerName: String = "",
    val rating: Int = 5,
    val comment: String? = null,
    @SerialName("tenant_id") val tenantId: String = "",
    @SerialName("created_at") val createdAt: String = ""
)

/**
 * transfers tablosu – Transfer operasyonu entity.
 */
@Serializable
data class TransferEntity(
    val id: String = "",
    @SerialName("booking_id") val bookingId: String? = null,
    @SerialName("departure_id") val departureId: String? = null,
    @SerialName("vehicle_id") val vehicleId: String? = null,
    @SerialName("driver_id") val driverId: String? = null,
    @SerialName("guide_id") val guideId: String? = null,
    @SerialName("transfer_type") val transferType: String = "tour",
    val origin: String = "",
    val destination: String = "",
    @SerialName("pickup_time") val pickupTime: String? = null,
    @SerialName("dropoff_time") val dropoffTime: String? = null,
    @SerialName("pax_count") val paxCount: Int = 0,
    val status: String = "planned",
    val price: Double = 0.0,
    val currency: String = "TRY",
    val notes: String? = null,
    @SerialName("tenant_id") val tenantId: String = "",
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = "",
    @SerialName("created_by") val createdBy: String? = null
)

/**
 * transfer_pickups tablosu – Şoför pickup noktası entity.
 */
@Serializable
data class TransferPickupEntity(
    val id: String = "",
    @SerialName("transfer_id") val transferId: String = "",
    @SerialName("passenger_name") val passengerName: String = "",
    @SerialName("passenger_phone") val passengerPhone: String? = null,
    @SerialName("hotel_name") val hotelName: String = "",
    @SerialName("location_name") val locationName: String = "",
    val latitude: Double = 41.0082,
    val longitude: Double = 28.9784,
    @SerialName("scheduled_time") val scheduledTime: String = "",
    val status: String = "pending",
    @SerialName("pax_count") val paxCount: Int = 1,
    @SerialName("room_number") val roomNumber: String? = null,
    val notes: String? = null,
    @SerialName("tenant_id") val tenantId: String = "",
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = "",
    @SerialName("created_by") val createdBy: String? = null
)
