package com.mgacreative.touros.domain.model

import kotlinx.serialization.Serializable

/**
 * 3.1.1 Muhasebe Kasa / Banka / POS Hesabı Domain Modeli.
 */
@Serializable
data class Account(
    val id: String = "",
    val name: String = "",
    val accountType: String = "cash", // cash, bank, pos, online
    val currency: String = "TRY",
    val balance: Double = 0.0,
    val iban: String? = null,
    val bankName: String? = null,
    val isActive: Boolean = true,
    val tenantId: String = ""
)
