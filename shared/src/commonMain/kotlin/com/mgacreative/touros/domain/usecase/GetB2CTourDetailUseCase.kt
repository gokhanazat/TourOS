package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.model.B2CTourDetail
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * 4.2.2 B2C Mobil Tur Detayını Getirme Use Case.
 */
class GetB2CTourDetailUseCase(
    private val supabaseClient: SupabaseClient
) {
    suspend operator fun invoke(tourId: String, tenantId: String): Result<B2CTourDetail> {
        return runCatching {
            val params = buildJsonObject {
                put("p_tenant_id", tenantId)
                put("p_tour_id", tourId)
            }

            val list = supabaseClient.postgrest.rpc("get_b2c_tour_detail", params)
                .decodeList<B2CTourDetail>()

            list.firstOrNull() ?: B2CTourDetail(tourId = tourId)
        }.recover { B2CTourDetail(tourId = tourId) }
    }
}
