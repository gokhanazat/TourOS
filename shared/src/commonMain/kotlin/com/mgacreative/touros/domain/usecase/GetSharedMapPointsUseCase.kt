package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.model.SharedMapPoint
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * 4.4.3 Harita Katman Verilerini Getirme Use Case.
 */
class GetSharedMapPointsUseCase(
    private val supabaseClient: SupabaseClient
) {
    suspend operator fun invoke(layerType: String, tenantId: String): Result<List<SharedMapPoint>> {
        return runCatching {
            val params = buildJsonObject {
                put("p_tenant_id", tenantId)
                put("p_layer_type", layerType)
            }

            val list = supabaseClient.postgrest.rpc("get_map_layer_data", params)
                .decodeList<SharedMapPoint>()

            if (list.isEmpty()) getFallbackPoints(layerType) else list
        }.recover { getFallbackPoints(layerType) }
    }

    private fun getFallbackPoints(layer: String): List<SharedMapPoint> {
        val all = listOf(
            SharedMapPoint("h1", "Granada Luxury Belek", "HOTEL", 36.8647, 31.0601, "5 Yıldız Ultra Her Şey Dahil Otel"),
            SharedMapPoint("h2", "Kapadokya Cave Resort", "HOTEL", 38.6244, 34.8147, "Göreme Butik Mağara Otel"),
            SharedMapPoint("r1", "Durak 1: Göreme Açık Hava Müzesi", "ROUTE_STOP", 38.6401, 34.8291, "Kapadokya Tur Rotası #1"),
            SharedMapPoint("r2", "Durak 2: Paşabağı Peri Bacaları", "ROUTE_STOP", 38.6775, 34.8532, "Kapadokya Tur Rotası #2"),
            SharedMapPoint("v1", "VIP Transfer Otobüsü (34 TO 2026)", "VEHICLE", 38.6500, 34.8350, "Hız: 65 km/s - Canlı Konum")
        )

        return when (layer) {
            "HOTELS" -> all.filter { it.category == "HOTEL" }
            "ROUTES" -> all.filter { it.category == "ROUTE_STOP" }
            "LIVE_VEHICLE" -> all.filter { it.category == "VEHICLE" }
            else -> all
        }
    }
}
