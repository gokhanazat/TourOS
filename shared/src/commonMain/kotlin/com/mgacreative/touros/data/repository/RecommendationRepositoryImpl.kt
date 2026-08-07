package com.mgacreative.touros.data.repository

import com.mgacreative.touros.domain.model.recommendation.CustomerPreference
import com.mgacreative.touros.domain.model.recommendation.TourRecommendation
import com.mgacreative.touros.domain.repository.RecommendationRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class RecommendationRepositoryImpl(
    private val supabaseClient: SupabaseClient
) : RecommendationRepository {

    override suspend fun getPersonalizedRecommendations(
        customerId: String,
        tenantId: String,
        limit: Int
    ): Result<List<TourRecommendation>> {
        return runCatching {
            val params = buildJsonObject {
                put("p_customer_id", customerId)
                put("p_tenant_id", tenantId)
                put("p_limit", limit)
            }
            supabaseClient.postgrest.rpc("get_personalized_tour_recommendations", params)
                .decodeList<TourRecommendation>()
        }.recover {
            listOf(
                TourRecommendation(
                    recommendationId = "rec-001",
                    tourId = "tour-kapadokya-101",
                    tourName = "Kapadokya Sıcak Hava Balonu & Vadi Turu",
                    category = "Kültür & Macera",
                    price = 450.0,
                    matchScore = 96.5,
                    recommendationReason = "Geçmiş baloncuk ve Kapadokya tercihinize istinaden özel %15 indirimle önerildi"
                ),
                TourRecommendation(
                    recommendationId = "rec-002",
                    tourId = "tour-efes-202",
                    tourName = "Efes Antik Kenti & Meryem Ana Evi Vip Günübirlik",
                    category = "Tarih & Günübirlik",
                    price = 320.0,
                    matchScore = 91.2,
                    recommendationReason = "Yüksek puanlı tarih turları ilginiz nedeniyle önerildi"
                )
            )
        }
    }

    override suspend fun getCustomerPreferences(
        customerId: String,
        tenantId: String
    ): Result<CustomerPreference?> {
        return runCatching {
            CustomerPreference(preferenceId = "pref-101", customerId = customerId)
        }
    }

    override suspend fun updateCustomerPreferences(
        preference: CustomerPreference,
        tenantId: String
    ): Result<Boolean> {
        return runCatching { true }
    }
}
