package com.mgacreative.touros.domain.model

import kotlinx.serialization.Serializable

/**
 * 3.1.1 Gider / Masraf Domain Modeli.
 */
@Serializable
data class Expense(
    val id: String = "",
    val accountId: String? = null,
    val departureId: String? = null,
    val category: String = "", // fuel, toll, food, accommodation, other
    val description: String = "",
    val amount: Double = 0.0,
    val currency: String = "TRY",
    val expenseDate: String = "",
    val receiptUrl: String? = null,
    val notes: String? = null,
    val tenantId: String = ""
)
