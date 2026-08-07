package com.mgacreative.touros.data.database.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * customers tablosu – Müşteri kartı entity.
 */
@Serializable
data class CustomerEntity(
    val id: String = "",
    @SerialName("full_name") val fullName: String = "",
    val email: String? = null,
    val phone: String? = null,
    @SerialName("tc_no") val tcNo: String? = null,
    @SerialName("passport_no") val passportNo: String? = null,
    @SerialName("birth_date") val birthDate: String? = null,
    val gender: String? = null,
    val address: String? = null,
    val city: String? = null,
    val country: String = "TR",
    val source: String = "direct",
    val tags: List<String>? = null,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("tenant_id") val tenantId: String = "",
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = "",
    @SerialName("created_by") val createdBy: String? = null
)

/**
 * agencies tablosu – Acente entity.
 */
@Serializable
data class AgencyEntity(
    val id: String = "",
    val name: String = "",
    @SerialName("contact_person") val contactPerson: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val city: String? = null,
    val country: String = "TR",
    @SerialName("tax_no") val taxNo: String? = null,
    @SerialName("commission_rate") val commissionRate: Double = 0.0,
    val balance: Double = 0.0,
    val currency: String = "TRY",
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("tenant_id") val tenantId: String = "",
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = "",
    @SerialName("created_by") val createdBy: String? = null
)

/**
 * loyalty_points tablosu – Puan hareketi entity.
 */
@Serializable
data class LoyaltyPointEntity(
    val id: String = "",
    @SerialName("customer_id") val customerId: String = "",
    @SerialName("booking_id") val bookingId: String? = null,
    val points: Int = 0,
    @SerialName("transaction_type") val transactionType: String = "earn",
    val description: String? = null,
    @SerialName("expires_at") val expiresAt: String? = null,
    @SerialName("tenant_id") val tenantId: String = "",
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = "",
    @SerialName("created_by") val createdBy: String? = null
)

/**
 * customer_notes tablosu – Müşteri notu entity.
 */
@Serializable
data class CustomerNoteEntity(
    val id: String = "",
    @SerialName("customer_id") val customerId: String = "",
    val note: String = "",
    @SerialName("note_type") val noteType: String = "general",
    @SerialName("is_pinned") val isPinned: Boolean = false,
    @SerialName("tenant_id") val tenantId: String = "",
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = "",
    @SerialName("created_by") val createdBy: String? = null
)
