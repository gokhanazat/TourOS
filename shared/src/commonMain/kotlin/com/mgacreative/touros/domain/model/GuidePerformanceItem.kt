package com.mgacreative.touros.domain.model

import kotlinx.serialization.Serializable

/**
 * 2.5.5 Rehber Performans Rapor Öğesi Domain Modeli.
 */
@Serializable
data class GuidePerformanceItem(
    val guideId: String = "",
    val fullName: String = "",
    val licenseNumber: String? = null,
    val languages: List<String>? = null,
    val specialization: String? = null,
    val rating: Double = 5.0,
    val totalToursCompleted: Int = 0,
    val totalReviews: Int = 0,
    val fiveStarReviews: Int = 0,
    val performanceLevel: String = "Standart Performans"
)

/**
 * Rehber Genel Kadro Performans Özeti.
 */
@Serializable
data class GuidePerformanceSummary(
    val totalActiveGuides: Int = 0,
    val avgFleetRating: Double = 5.0,
    val totalToursExecuted: Int = 0,
    val topRatedGuideName: String = "-",
    val guides: List<GuidePerformanceItem> = emptyList()
)
