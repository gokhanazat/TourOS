package com.mgacreative.touros.domain.model

import kotlinx.serialization.Serializable

/**
 * 2.4.2 Transfer Görevi Atama Domain Modeli.
 */
@Serializable
data class TransferTask(
    val id: String = "",
    val bookingId: String? = null,
    val departureId: String? = null,
    val vehicleId: String? = null,
    val driverId: String? = null,
    val guideId: String? = null,
    val transferType: String = "tour", // tour, airport, intercity, custom
    val origin: String = "",
    val destination: String = "",
    val pickupTime: String? = null,
    val dropoffTime: String? = null,
    val paxCount: Int = 0,
    val status: String = "planned", // planned, assigned, in_progress, completed, cancelled
    val price: Double = 0.0,
    val currency: String = "TRY",
    val notes: String? = null,
    val tenantId: String = ""
)
