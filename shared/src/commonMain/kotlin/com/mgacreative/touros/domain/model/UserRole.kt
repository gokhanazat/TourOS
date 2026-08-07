package com.mgacreative.touros.domain.model

import kotlinx.serialization.Serializable

/**
 * TourOS kullanıcı rolleri.
 * RBAC (Role-Based Access Control) sistemi bu enum üzerinden çalışır.
 * Her rol farklı ekran ve yetkilere sahiptir.
 */
@Serializable
enum class UserRole(val displayName: String) {
    SYSTEM_ADMIN("Sistem Yöneticisi"),
    TOUR_OPERATOR("Tur Operatörü"),
    SALES("Satış Personeli"),
    GUIDE("Rehber"),
    DRIVER("Şoför"),
    ACCOUNTING("Muhasebe"),
    AGENT("Acente"),
    CUSTOMER("Müşteri");

    companion object {
        fun fromString(value: String): UserRole =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: CUSTOMER
    }
}
