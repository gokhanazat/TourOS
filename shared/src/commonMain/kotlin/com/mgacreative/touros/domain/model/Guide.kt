package com.mgacreative.touros.domain.model

import kotlinx.serialization.Serializable

/**
 * 2.5.1 Rehber Yönetimi Domain Modeli.
 */
@Serializable
data class Guide(
    val id: String = "",
    val fullName: String = "",
    val phone: String? = null,
    val email: String? = null,
    val licenseNumber: String? = null, // Lisans / Kokart Numarası
    val languages: List<String>? = null, // Bildiği diller (Türkçe, İngilizce, Almanca, Fransızca vs.)
    val specialization: String? = null, // Uzmanlık Alanı (Kültür, Arkeoloji, Gastronomi vs.)
    val tcNo: String? = null,
    val birthDate: String? = null,
    val rating: Double = 5.0, // Puan (1.0 - 5.0)
    val totalToursCompleted: Int = 0, // Tamamlanan Tur Sayısı / Tur Geçmişi
    val notes: String? = null,
    val isActive: Boolean = true,
    val tenantId: String = ""
)
