package com.mgacreative.touros.domain.model.ota

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 4.5.4 OTA Senkronizasyon Kaydı Modeli.
 */
@Serializable
data class OTASyncLog(
    @SerialName("log_id") val logId: String = "log-001",
    @SerialName("account_id") val accountId: String = "acc-001",
    @SerialName("sync_type") val syncType: String = "INCREMENTAL", // FULL, INCREMENTAL, OFFLINE_RETRY
    @SerialName("items_synced") val itemsSynced: Int = 5,
    val status: String = "SUCCESS",
    @SerialName("last_synced_at") val lastSyncedAt: String = "2026-08-06T15:20:00Z"
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

/**
 * 4.5.4 OTA Sync Engine Durum Özeti.
 */
@Serializable
data class OTASyncStatusSummary(
    @SerialName("is_polling_active") val isPollingActive: Boolean = true,
    @SerialName("total_synced_bookings") val totalSyncedBookings: Int = 142,
    @SerialName("retry_queue_count") val retryQueueCount: Int = 0,
    @SerialName("last_sync_timestamp") val lastSyncTimestamp: String = "2026-08-06T15:25:00Z"
)
