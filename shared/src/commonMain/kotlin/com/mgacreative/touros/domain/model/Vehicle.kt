package com.mgacreative.touros.domain.model

import kotlinx.serialization.Serializable

/**
 * 2.4.1 Araç Parkı ve Filo Yönetimi Domain Modeli.
 */
@Serializable
data class Vehicle(
    val id: String = "",
    val plateNumber: String = "",
    val brand: String? = null,
    val model: String? = null,
    val year: Int? = null,
    val capacity: Int = 46, // Koltuk Sayısı
    val vehicleType: String = "bus", // bus (Otobüs), minibus (Minibüs), vip (VIP Araç)
    val color: String? = null,
    val isOwned: Boolean = true,
    val ownerInfo: String? = null,
    val insuranceExpiry: String? = null, // Sigorta Bitiş Tarihi
    val inspectionExpiry: String? = null, // Muayene Bitiş Tarihi
    val lastMaintenanceDate: String? = null, // Son Bakım Tarihi
    val nextMaintenanceDate: String? = null, // Gelecek Bakım Tarihi
    val maintenanceNotes: String? = null, // Bakım Notları
    val isActive: Boolean = true,
    val tenantId: String = ""
)
