package com.mgacreative.touros.domain.model

import kotlinx.serialization.Serializable

/**
 * 2.4.4 Araç Bakım/Sigorta/Muayene Uyarı Domain Modeli.
 */
@Serializable
data class VehicleMaintenanceAlert(
    val vehicleId: String = "",
    val plateNumber: String = "",
    val brandModel: String = "",
    val alertType: String = "INSURANCE_EXPIRING", // INSURANCE_EXPIRING, INSPECTION_EXPIRING, MAINTENANCE_DUE
    val expiryDate: String = "",
    val daysLeft: Int = 0,
    val severity: String = "WARNING" // CRITICAL (<=7 gün / süresi dolmuş), WARNING (<=30 gün)
)
