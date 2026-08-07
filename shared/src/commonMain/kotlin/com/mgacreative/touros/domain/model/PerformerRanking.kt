package com.mgacreative.touros.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 3.3.3 Personel / Acente / Rehber Performansı Modeli.
 */
@Serializable
data class PerformerRanking(
    @SerialName("performer_name") val performerName: String = "",
    @SerialName("performer_type") val performerType: String = "", // Personel, Acente, Rehber
    @SerialName("completed_jobs") val completedJobs: Int = 0,
    @SerialName("total_revenue") val totalRevenue: Double = 0.0,
    @SerialName("avg_rating") val avgRating: Double = 0.0
)
