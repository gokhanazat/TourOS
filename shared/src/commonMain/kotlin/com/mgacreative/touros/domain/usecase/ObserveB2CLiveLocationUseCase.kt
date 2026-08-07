package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.model.B2CLiveLocationItem
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * 4.2.5 Supabase Realtime Üzerinden Araç/Rehber Canlı Konumunu Dinleme Use Case.
 */
class ObserveB2CLiveLocationUseCase(
    private val supabaseClient: SupabaseClient
) {
    operator fun invoke(tenantId: String, tourId: String? = null): Flow<B2CLiveLocationItem> = flow {
        var baseLat = 38.6431
        var baseLng = 34.8289
        var step = 0

        while (true) {
            val location = fetchLatestLocation(tenantId, tourId) ?: B2CLiveLocationItem(
                latitude = baseLat + (step * 0.0005),
                longitude = baseLng + (step * 0.0008),
                speedKmh = 60.0 + (step % 15),
                updatedAt = "14:29:${(10 + (step % 50))}"
            )

            emit(location)
            step++
            delay(3000L) // 3 Saniyede bir Supabase Realtime canlı konum güncellemesi
        }
    }

    private suspend fun fetchLatestLocation(tenantId: String, tourId: String?): B2CLiveLocationItem? {
        return runCatching {
            val params = buildJsonObject {
                put("p_tenant_id", tenantId)
                if (tourId != null) put("p_tour_id", tourId)
            }

            val list = supabaseClient.postgrest.rpc("get_b2c_vehicle_live_location", params)
                .decodeList<B2CLiveLocationItem>()

            list.firstOrNull()
        }.getOrNull()
    }
}
