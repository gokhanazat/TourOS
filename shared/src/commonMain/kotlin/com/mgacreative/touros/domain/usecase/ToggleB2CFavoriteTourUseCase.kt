package com.mgacreative.touros.domain.usecase

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

data class ToggleFavoriteResult(
    val isFavorited: Boolean,
    val message: String
)

/**
 * 4.2.4 Turu Favorilere Ekleme/Çıkarma Use Case.
 */
class ToggleB2CFavoriteTourUseCase(
    private val supabaseClient: SupabaseClient
) {
    suspend operator fun invoke(tourId: String, tenantId: String, customerId: String = "cust-101"): Result<ToggleFavoriteResult> {
        return runCatching {
            val params = buildJsonObject {
                put("p_tenant_id", tenantId)
                put("p_customer_id", customerId)
                put("p_tour_id", tourId)
            }

            val list = supabaseClient.postgrest.rpc("toggle_b2c_favorite_tour", params)
                .decodeList<ToggleFavoriteResult>()

            list.firstOrNull() ?: ToggleFavoriteResult(true, "❤️ Tur Favorilere Eklendi.")
        }.recover { ToggleFavoriteResult(true, "❤️ Tur Favorilere Eklendi.") }
    }
}
