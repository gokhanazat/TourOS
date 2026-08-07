package com.mgacreative.touros.domain.model.feedback

import kotlinx.serialization.Serializable

enum class EntityType {
    GUIDE,
    HOTEL,
    SUPPLIER
}

@Serializable
data class VendorPerformanceImpact(
    val entityName: String,
    val entityType: EntityType = EntityType.SUPPLIER,
    val complaintCount: Int = 10,
    val trendSpikePercent: Double = 25.0,
    val performanceScore: Double = 3.8,
    val alertMessage: String
)
