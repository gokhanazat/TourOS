package com.mgacreative.touros.domain.engine

import com.mgacreative.touros.domain.model.CommissionRule

/**
 * 3.1.6 Komisyon Hesaplama Motoru (Engine).
 * Acente bazlı, tur bazlı, % oran veya sabit tutar komisyon tutarını dinamik hesaplar.
 */
class CommissionCalculationEngine {

    fun calculateCommissionAmount(bookingTotalPrice: Double, rule: CommissionRule): Double {
        if (bookingTotalPrice <= 0) return 0.0
        return if (rule.calculationType == "percentage") {
            ((bookingTotalPrice * (rule.rateValue / 100.0)) * 100).toInt() / 100.0
        } else {
            rule.fixedAmount
        }
    }

    fun findBestApplicableRule(
        rules: List<CommissionRule>,
        agentId: String?,
        tourId: String?
    ): CommissionRule? {
        val activeRules = rules.filter { it.isActive }

        // Öncelik 1: Acente + Tur ikilisi eşleşen
        activeRules.firstOrNull { it.agentId == agentId && it.tourId == tourId && agentId != null && tourId != null }?.let { return it }

        // Öncelik 2: Sadece Acente eşleşen
        activeRules.firstOrNull { it.agentId == agentId && agentId != null && it.tourId == null }?.let { return it }

        // Öncelik 3: Sadece Tur eşleşen
        activeRules.firstOrNull { it.tourId == tourId && tourId != null && it.agentId == null }?.let { return it }

        // Öncelik 4: Genel kural (agentId == null && tourId == null)
        return activeRules.firstOrNull { it.agentId == null && it.tourId == null }
    }
}
