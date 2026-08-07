package com.mgacreative.touros.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 3.3.1 Finansal Raporlar (KDV, Gelir, Nakit, Banka, Kârlılık) Özet Modeli.
 */
@Serializable
data class FinancialReportSummary(
    @SerialName("total_revenue") val totalRevenue: Double = 0.0,
    @SerialName("total_expenses") val totalExpenses: Double = 0.0,
    @SerialName("net_profit") val netProfit: Double = 0.0,
    @SerialName("vat_collected") val vatCollected: Double = 0.0,
    @SerialName("vat_paid") val vatPaid: Double = 0.0,
    @SerialName("vat_payable") val vatPayable: Double = 0.0,
    @SerialName("cash_balance") val cashBalance: Double = 0.0,
    @SerialName("bank_balance") val bankBalance: Double = 0.0,
    @SerialName("pos_balance") val posBalance: Double = 0.0,
    @SerialName("profit_margin_percentage") val profitMarginPercentage: Double = 0.0
)
