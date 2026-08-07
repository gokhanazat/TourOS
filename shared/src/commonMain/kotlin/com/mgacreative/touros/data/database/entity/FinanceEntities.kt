package com.mgacreative.touros.data.database.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * accounts tablosu – Kasa/banka hesapları entity.
 */
@Serializable
data class AccountEntity(
    val id: String = "",
    val name: String = "",
    @SerialName("account_type") val accountType: String = "cash",
    val currency: String = "TRY",
    val balance: Double = 0.0,
    val iban: String? = null,
    @SerialName("bank_name") val bankName: String? = null,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("tenant_id") val tenantId: String = "",
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = "",
    @SerialName("created_by") val createdBy: String? = null
)

/**
 * invoices tablosu – Fatura entity.
 */
@Serializable
data class InvoiceEntity(
    val id: String = "",
    @SerialName("invoice_no") val invoiceNo: String = "",
    @SerialName("booking_id") val bookingId: String? = null,
    @SerialName("invoice_type") val invoiceType: String = "sale",
    @SerialName("customer_name") val customerName: String = "",
    @SerialName("customer_tax_no") val customerTaxNo: String? = null,
    val subtotal: Double = 0.0,
    @SerialName("tax_rate") val taxRate: Double = 0.0,
    @SerialName("tax_amount") val taxAmount: Double = 0.0,
    @SerialName("total_amount") val totalAmount: Double = 0.0,
    val currency: String = "TRY",
    val status: String = "draft",
    @SerialName("issued_at") val issuedAt: String? = null,
    @SerialName("due_date") val dueDate: String? = null,
    val notes: String? = null,
    @SerialName("tenant_id") val tenantId: String = "",
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = "",
    @SerialName("created_by") val createdBy: String? = null
)

/**
 * payments tablosu – Ödeme entity.
 */
@Serializable
data class PaymentEntity(
    val id: String = "",
    @SerialName("invoice_id") val invoiceId: String = "",
    @SerialName("account_id") val accountId: String? = null,
    val amount: Double = 0.0,
    val currency: String = "TRY",
    @SerialName("payment_method") val paymentMethod: String = "cash",
    @SerialName("payment_date") val paymentDate: String = "",
    @SerialName("reference_no") val referenceNo: String? = null,
    val notes: String? = null,
    @SerialName("tenant_id") val tenantId: String = "",
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = "",
    @SerialName("created_by") val createdBy: String? = null
)

/**
 * commissions tablosu – Komisyon entity.
 */
@Serializable
data class CommissionEntity(
    val id: String = "",
    @SerialName("booking_id") val bookingId: String = "",
    @SerialName("agent_name") val agentName: String = "",
    @SerialName("agent_type") val agentType: String = "agency",
    val rate: Double = 0.0,
    val amount: Double = 0.0,
    val currency: String = "TRY",
    @SerialName("is_paid") val isPaid: Boolean = false,
    @SerialName("paid_at") val paidAt: String? = null,
    val notes: String? = null,
    @SerialName("tenant_id") val tenantId: String = "",
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = "",
    @SerialName("created_by") val createdBy: String? = null
)

/**
 * expenses tablosu – Gider entity.
 */
@Serializable
data class ExpenseEntity(
    val id: String = "",
    @SerialName("account_id") val accountId: String? = null,
    @SerialName("departure_id") val departureId: String? = null,
    val category: String = "",
    val description: String = "",
    val amount: Double = 0.0,
    val currency: String = "TRY",
    @SerialName("expense_date") val expenseDate: String = "",
    @SerialName("receipt_url") val receiptUrl: String? = null,
    val notes: String? = null,
    @SerialName("tenant_id") val tenantId: String = "",
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = "",
    @SerialName("created_by") val createdBy: String? = null
)
