package com.mgacreative.touros.domain.model.forecast

import kotlinx.serialization.Serializable

enum class AlertSeverity {
    CRITICAL,
    WARNING,
    INFO
}

@Serializable
data class LowOccupancyAlert(
    val alertId: String,
    val tourId: String,
    val tourName: String,
    val departureDate: String,
    val currentCapacity: Int = 30,
    val bookedCount: Int = 5,
    val occupancyRate: Double = 16.67,
    val suggestedCampaign: String = "%15 Son Dakika Erken Rezervasyon İndirimi",
    val severity: AlertSeverity = AlertSeverity.CRITICAL
)
