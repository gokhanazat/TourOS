package com.mgacreative.touros.domain.model.segmentation

import kotlinx.serialization.Serializable

enum class SegmentTier {
    VIP,
    FREQUENT_TRAVELER,
    CASUAL_EXPLORER,
    AT_RISK
}

@Serializable
data class CustomerSegment(
    val id: String,
    val customerId: String,
    val segmentTier: SegmentTier = SegmentTier.CASUAL_EXPLORER,
    val spendingScore: Double = 0.0,
    val travelFrequency: Int = 1,
    val preferredCategory: String = "Kültür",
    val loyaltyPoints: Int = 100,
    val customerNotes: String = "Segmentasyon analizi sonucu"
)

@Serializable
data class SegmentationAnalysisResult(
    val processedCount: Int = 0,
    val vipCount: Int = 0,
    val frequentCount: Int = 0,
    val casualCount: Int = 0
)
