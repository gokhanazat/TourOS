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

@Serializable
data class PromoBannerItem(
    val id: String = "",
    val title: String = "",
    val imageUrl: String = "",
    val targetUrl: String? = null
)

/**
 * Firma / Şirket Ayarları Domain Modeli.
 */
@Serializable
data class CompanySettings(
    val id: String,
    val name: String,
    val legalTitle: String = "",
    val taxOffice: String = "",
    val taxNumber: String = "",
    val tradeRegistryNo: String = "",
    val mersisNo: String = "",
    val address: String = "",
    val phone: String = "",
    val email: String = "",
    val companyType: String = "tur_operatoru", // tur_operatoru veya acente
    val operatorCode: String? = null, // örn. ANK, IST
    val logoUrl: String? = null,
    val themeColor: String = "#1976D2",
    val taxRate: Double = 20.0,
    val seasons: List<CompanySeason> = emptyList(),
    val supportedCurrencies: List<String> = listOf("TRY", "EUR", "USD"),
    val supportedLanguages: List<String> = listOf("tr", "en"),
    val headerImageUrl: String? = null,
    val heroSubtitle: String = "",
    val footerText: String = "",
    val webEmail: String = "",
    val webPhone: String = "",
    val webWhatsapp: String = "",
    val webAddress: String = "",
    val promoBannerTitle: String? = null,
    val promoBannerImageUrl: String? = null,
    val promoBannerTargetUrl: String? = null,
    val promoBanners: List<PromoBannerItem> = emptyList(),
    val bankName: String? = null,
    val iban: String? = null,
    val accountHolder: String? = null,
    val paypalEmail: String? = null,
    val paypalMeUrl: String? = null,
    val defaultMasterAgencyId: String? = "00000000-0000-0000-0000-000000000001",
    val defaultMasterAgencyCode: String? = "AGN-MASTER"
) {
    fun getEffectivePromoBanners(): List<PromoBannerItem> {
        val validList = promoBanners.filter { it.imageUrl.isNotBlank() || it.title.isNotBlank() }
        if (validList.isNotEmpty()) return validList

        if (!promoBannerImageUrl.isNullOrBlank() || !promoBannerTitle.isNullOrBlank()) {
            return listOf(
                PromoBannerItem(
                    id = "1",
                    title = promoBannerTitle ?: "",
                    imageUrl = promoBannerImageUrl ?: "",
                    targetUrl = promoBannerTargetUrl
                )
            )
        }
        return emptyList()
    }
}
