package com.mgacreative.touros.domain.usecase.recommendation

import com.mgacreative.touros.domain.model.recommendation.TourRecommendation
import com.mgacreative.touros.domain.repository.RecommendationRepository

class GetPersonalizedRecommendationsUseCase(
    private val repository: RecommendationRepository
) {
    suspend operator fun invoke(customerId: String, tenantId: String, limit: Int = 5): Result<List<TourRecommendation>> {
        return repository.getPersonalizedRecommendations(customerId, tenantId, limit)
    }
}
