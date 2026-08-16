package com.mgacreative.touros.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * SaaS Admin - Merkezi API Data Besleme Kaynağı Modeli.
 */
@Serializable
data class DataFeedSource(
    @SerialName("id") val id: String = "",
    @SerialName("source_name") val sourceName: String = "",
    @SerialName("provider_type") val providerType: String = "PAXIMUM", // PAXIMUM, CORAL, SEJOUR, AMADEUS, CUSTOM_XML, CUSTOM_JSON
    @SerialName("logo_icon") val logoIcon: String = "🌐",
    @SerialName("endpoint_url") val endpointUrl: String = "",
    @SerialName("api_key") val apiKey: String = "",
    @SerialName("api_secret") val apiSecret: String = "",
    @SerialName("agency_code") val agencyCode: String = "",
    @SerialName("data_types") val dataTypes: List<String> = listOf("TOURS", "HOTELS"), // TOURS, HOTELS, FLIGHTS
    @SerialName("sync_interval") val syncInterval: String = "MANUAL", // 10_MIN, 30_MIN, 1_HOUR, 6_HOUR, 24_HOUR, MANUAL
    @SerialName("is_live") val isLive: Boolean = false, // false = BEKLEMEDE (Hazır), true = CANLI DEVREDE
    @SerialName("last_synced_at") val lastSyncedAt: String = "Henüz Veri Çekilmedi",
    @SerialName("synced_record_count") val syncedRecordCount: Int = 0,
    @SerialName("status_message") val statusMessage: String? = "Yapılandırıldı - Beklemede",
    @SerialName("created_at") val createdAt: String = ""
)
