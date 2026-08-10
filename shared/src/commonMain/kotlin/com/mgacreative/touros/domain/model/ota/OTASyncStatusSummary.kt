package com.mgacreative.touros.domain.model.ota

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
