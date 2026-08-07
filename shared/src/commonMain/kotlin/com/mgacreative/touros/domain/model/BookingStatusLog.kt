package com.mgacreative.touros.domain.model

data class BookingStatusLog(
    val id: String = "",
    val bookingId: String = "",
    val fromStatus: String? = null,
    val toStatus: String = "",
    val changedBy: String? = null,
    val notes: String? = null,
    val createdAt: String = ""
)
