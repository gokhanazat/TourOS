package com.mgacreative.touros.domain.model

import kotlinx.serialization.Serializable

/**
 * 2.3.4 Sezon Fiyat Matrisi Domain Modeli.
 */
@Serializable
data class HotelSeasonRate(
    val id: String = "",
    val hotelId: String = "",
    val roomTypeId: String? = null,
    val seasonName: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val singlePrice: Double = 0.0,
    val doublePrice: Double = 0.0,
    val triplePrice: Double = 0.0,
    val extraBedPrice: Double = 0.0,
    val childPrice: Double = 0.0,
    val currency: String = "TRY",
    val mealPlan: String = "BB", // BB, HB, FB, AI, RO
    val minStayDays: Int = 1,
    val isActive: Boolean = true,
    val tenantId: String = "",
    val createdAt: String = ""
)
