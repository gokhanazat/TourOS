package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.model.GuideRecommendation
import com.mgacreative.touros.domain.repository.GuideRepository

/**
 * 2.5.2 Akıllı Rehber Öneri ve Müsaitlik Filtreleme Use Case.
 */
class GetRecommendedGuidesUseCase(
    private val guideRepository: GuideRepository
) {
    suspend operator fun invoke(
        tenantId: String,
        requiredLanguage: String? = null,
        onlyAvailable: Boolean = false
    ): Result<List<GuideRecommendation>> {
        return runCatching {
            val guidesRes = guideRepository.getGuides(tenantId)
            val guides = guidesRes.getOrDefault(emptyList())

            val recommendations = guides.map { guide ->
                val hasLangMatch = requiredLanguage == null || guide.languages?.any { it.contains(requiredLanguage, ignoreCase = true) } == true
                val isAvailable = guide.isActive

                var score = (guide.rating * 10).toInt() // Max 50
                if (hasLangMatch) score += 50 else score += 10
                if (isAvailable) score += 20

                val reason = when {
                    hasLangMatch && isAvailable -> "🌐 Tur Dili (${requiredLanguage ?: "Türkçe"}) Birebir Eşleşiyor & Müsait"
                    hasLangMatch -> "🌐 Tur Dili Eşleşiyor (Görev Durumuna Dikkat Ediniz)"
                    else -> "⭐ Yüksek Rehber Puanı (${guide.rating})"
                }

                GuideRecommendation(
                    guide = guide,
                    isAvailable = isAvailable,
                    matchScore = score.coerceIn(10, 100),
                    languageMatch = hasLangMatch,
                    recommendationReason = reason
                )
            }

            var result = recommendations.sortedByDescending { it.matchScore }
            if (onlyAvailable) {
                result = result.filter { it.isAvailable }
            }
            result
        }
    }
}
