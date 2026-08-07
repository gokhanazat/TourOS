package com.mgacreative.touros.domain.repository

import com.mgacreative.touros.domain.model.recommendation.CustomerPreference
import com.mgacreative.touros.domain.model.recommendation.TourRecommendation

interface RecommendationRepository {
    suspend fun getPersonalizedRecommendations(customerId: String, tenantId: String, limit: Int = 5): Result<List<TourRecommendation>>
    suspend fun getCustomerPreferences(customerId: String, tenantId: String): Result<CustomerPreference?>
    suspend fun updateCustomerPreferences(preference: CustomerPreference, tenantId: String): Result<Boolean>
}
