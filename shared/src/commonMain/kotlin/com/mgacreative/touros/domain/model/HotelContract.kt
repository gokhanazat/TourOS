package com.mgacreative.touros.domain.model

import kotlinx.serialization.Serializable

/**
 * 2.3.3 Otel Kontrat Yönetimi Domain Modeli.
 */
@Serializable
data class HotelContract(
    val id: String = "",
    val hotelId: String = "",
    val roomTypeId: String? = null,
    val seasonName: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val pricePerNight: Double = 0.0,
    val currency: String = "TRY",
    val allotment: Int = 0,
    val releaseDays: Int = 7,
    val mealPlan: String = "BB", // BB (Oda Kahvaltı), HB (Yarım Pansiyon), FB (Tam Pansiyon), AI (Her Şey Dahil), RO (Sadece Oda)
    val notes: String? = null,
    val isActive: Boolean = true,
    val tenantId: String = "",
    val createdAt: String = ""
)
