package com.mgacreative.touros.domain.model.ota

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 4.5.4 OTA Senkronizasyon Kaydı Modeli.
 */
@Serializable
data class OTASyncLog(
    @SerialName("log_id") val logId: String = "log-001",
    @SerialName("timestamp") val timestamp: String = "",
    @SerialName("provider_name") val providerName: String = "",
    @SerialName("provider_icon") val providerIcon: String = "🌐",
    @SerialName("provider_id") val providerId: String = "viator",
    @SerialName("event_name") val eventName: String = "",
    @SerialName("http_status_code") val httpStatusCode: Int = 200,
    @SerialName("is_error") val isError: Boolean = false,
    @SerialName("request_payload_json") val requestPayloadJson: String = "{}",
    @SerialName("response_body_json") val responseBodyJson: String = "{}",
    @SerialName("error_message") val errorMessage: String? = null,
    @SerialName("account_id") val accountId: String = "acc-001",
    @SerialName("sync_type") val syncType: String = "INCREMENTAL",
    @SerialName("items_synced") val itemsSynced: Int = 0,
    val status: String = "SUCCESS",
    @SerialName("last_synced_at") val lastSyncedAt: String = ""
)

/**
 * 4.5.4 OTA Retry Queue & Offline Queue Öğe Modeli.
 */
@Serializable
data class OTARetryItem(
    @SerialName("retry_id") val retryId: String = "retry-001",
    @SerialName("ota_booking_id") val otaBookingId: String = "ota-bkg-101",
    val reason: String = "Ağ zaman aşımı",
    @SerialName("retry_count") val retryCount: Int = 1,
    @SerialName("created_at") val createdAt: String = "2026-08-06T15:25:00Z"
)
