package com.mgacreative.touros.domain.engine

import com.mgacreative.touros.domain.model.forecast.AlertSeverity
import com.mgacreative.touros.domain.model.forecast.LowOccupancyAlert

class LowOccupancyAlertRuleEngine {

    fun evaluateOccupancy(
        tourId: String,
        tourName: String,
        departureDate: String,
        capacity: Int,
        booked: Int,
        daysUntilDeparture: Int
    ): LowOccupancyAlert? {
        val occupancyRate = (booked.toDouble() / capacity.toDouble()) * 100.0
        
        if (daysUntilDeparture <= 7 && occupancyRate < 50.0) {
            val severity = if (occupancyRate < 30.0) AlertSeverity.CRITICAL else AlertSeverity.WARNING
            val campaignSuggestion = when {
                occupancyRate < 20.0 -> "%25 Son Dakika Flaş İndirim & Sosyal Medya Kampanyası"
                occupancyRate < 35.0 -> "%15 Erken/Geç Rezervasyon Fırsat İndirimi"
                else -> "B2B Acentelerine Özel +%5 Ek Komisyon Teşviki"
            }

            return LowOccupancyAlert(
                alertId = "alert-${tourId.take(4)}-$daysUntilDeparture",
                tourId = tourId,
                tourName = tourName,
                departureDate = departureDate,
                currentCapacity = capacity,
                bookedCount = booked,
                occupancyRate = occupancyRate,
                suggestedCampaign = campaignSuggestion,
                severity = severity
            )
        }

        return null
    }
}
