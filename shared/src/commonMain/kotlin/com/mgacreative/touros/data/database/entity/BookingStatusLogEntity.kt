package com.mgacreative.touros.data.database.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BookingStatusLogEntity(
    val id: String = "",
    @SerialName("booking_id") val bookingId: String = "",
    @SerialName("from_status") val fromStatus: String? = null,
    @SerialName("to_status") val toStatus: String = "",
    @SerialName("changed_by") val changedBy: String? = null,
    val notes: String? = null,
    @SerialName("tenant_id") val tenantId: String = "",
    @SerialName("created_at") val createdAt: String = ""
)
