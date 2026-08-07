package com.mgacreative.touros.domain.model.recommendation

import kotlinx.serialization.Serializable

@Serializable
data class TourRecommendation(
    val recommendationId: String,
    val tourId: String,
    val tourName: String,
    val category: String,
    val price: Double,
    val matchScore: Double,
    val recommendationReason: String
)

@Serializable
data class CustomerPreference(
    val preferenceId: String,
    val customerId: String,
    val favoriteCategories: List<String> = emptyList(),
    val preferredLanguage: String = "tr",
    val avgBudgetMin: Double = 100.0,
    val avgBudgetMax: Double = 2000.0,
    val preferredDestinations: List<String> = emptyList()
)
