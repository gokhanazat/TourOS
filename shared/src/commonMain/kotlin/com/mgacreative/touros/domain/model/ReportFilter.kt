package com.mgacreative.touros.domain.model

import kotlinx.serialization.Serializable

/**
 * 3.3.4 Rapor Filtreleme Modeli.
 */
@Serializable
data class ReportFilter(
    val startDate: String = "2026-07-01",
    val endDate: String = "2026-08-06",
    val companyName: String = "Tüm Firmalar",
    val currency: String = "TRY", // TRY, EUR, USD, GBP, AED, RUB
    val exportFormat: String = "pdf" // pdf, excel
)
