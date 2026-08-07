package com.mgacreative.touros.domain.model

import kotlinx.serialization.Serializable

/**
 * Sezon tanımı modeli.
 */
@Serializable
data class CompanySeason(
    val id: String = "",
    val name: String,
    val startDate: String, // YYYY-MM-DD
    val endDate: String    // YYYY-MM-DD
)

/**
 * Firma / Şirket Ayarları Domain Modeli.
 */
@Serializable
data class CompanySettings(
    val id: String,
    val name: String,
    val logoUrl: String? = null,
    val themeColor: String = "#1976D2",
    val taxRate: Double = 20.0,
    val seasons: List<CompanySeason> = emptyList(),
    val supportedCurrencies: List<String> = listOf("TRY", "EUR", "USD"),
    val supportedLanguages: List<String> = listOf("tr", "en")
)
