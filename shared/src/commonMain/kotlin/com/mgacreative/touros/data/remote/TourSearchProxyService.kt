package com.mgacreative.touros.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.functions.functions
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Serializable
data class ProxySearchPayload(
    val action: String, // "LIST_METADATA" or "SEARCH_TOURS"
    val type: String? = null,
    val departureCityId: String? = null,
    val countryId: String? = null,
    val nightsFrom: Int? = null,
    val nightsTo: Int? = null,
    val adults: Int? = null,
    val children: Int? = null
)

/**
 * Güvenli Dış Operatör (TourVisor/Paximum) API Köprüsü İstemcisi.
 * Hiçbir API anahtarı veya operatör şifresi bu kodda yer almaz;
 * tüm çağrılar Supabase Edge Functions sunucu ortamından maskeli olarak yürütülür.
 */
class TourSearchProxyService(
    private val supabaseClient: SupabaseClient
) {
    /**
     * Dış Operatörden (TourVisor) şehir, ülke ve operatör listesi çeker.
     */
    suspend fun fetchMetadataList(listType: String): Result<String> {
        return runCatching {
            val payload = buildJsonObject {
                put("action", "LIST_METADATA")
                put("type", listType)
            }
            
            val response = supabaseClient.functions.invoke(
                function = "tour-search-proxy",
                body = payload
            )
            response.bodyAsText()
        }
    }

    /**
     * Güvenli Canlı Tur Araması Gerçekleştirir.
     */
    suspend fun searchTours(payload: ProxySearchPayload): Result<String> {
        return runCatching {
            val response = supabaseClient.functions.invoke(
                function = "tour-search-proxy",
                body = payload
            )
            response.bodyAsText()
        }
    }
}
