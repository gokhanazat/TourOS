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
    val endDate: String,   // YYYY-MM-DD
    val commissionRate: Double = 12.5 // Sezon komisyon oranı (%)
)

@Serializable
data class PromoBannerItem(
    val id: String = "",
    val title: String = "",
    val imageUrl: String = "",
    val targetUrl: String? = null
)

@Serializable
data class ServiceCardItem(
    val id: String = "",
    val title: String = "",
    val subtitle: String = "",
    val imageUrl: String = "",
    val targetUrl: String = "",
    val hotelId: String? = null,
    val hotelName: String? = null
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
    val webMersisNo: String = "",
    val webTaxOffice: String = "",
    val webTaxNumber: String = "",
    val promoBannerTitle: String? = null,
    val promoBannerImageUrl: String? = null,
    val promoBannerTargetUrl: String? = null,
    val promoBanners: List<PromoBannerItem> = emptyList(),
    val serviceCards: List<ServiceCardItem> = emptyList(),
    val bankName: String? = null,
    val iban: String? = null,
    val accountHolder: String? = null,
    val paypalEmail: String? = null,
    val paypalMeUrl: String? = null,
    val defaultMasterAgencyId: String? = "00000000-0000-0000-0000-000000000001",
    val defaultMasterAgencyCode: String? = "AGN-MASTER",
    val desktopAppUrl: String? = null,
    val androidApkUrl: String? = null,
    val isAppDownloadActive: Boolean = true
) {
    fun getEffectiveServiceCards(): List<ServiceCardItem> {
        val validList = serviceCards.filter { it.title.isNotBlank() || it.imageUrl.isNotBlank() }
        if (validList.size >= 6) return validList.take(6)

        val defaults = listOf(
            ServiceCardItem("1", "Paket Turlar / Tour Packages", "Gezginler için özel seçilmiş her şey dahil paket tur seçenekleri ve rehberli geziler.", "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800", "PACKAGE_TOUR"),
            ServiceCardItem("2", "Otel Rezervasyonları / Hotel Reservations", "En uygun fiyat garantili seçkin 5 yıldızlı oteller, tatil köyleri ve ayrıcalıklı konaklama.", "https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800", "HOTEL"),
            ServiceCardItem("3", "Macera Turları / Adventure Tours", "Safari, trekking, kültür turları ve heyecan dolu özel tatil rotaları.", "https://images.unsplash.com/photo-1533105079780-92b9be482077?w=800", "ADVENTURE"),
            ServiceCardItem("4", "Seyahat Desteği / Travel Assistance", "Sorunsuz bir seyahat deneyimi için 7/24 canlı müşteri desteği ve acente danışmanlığı.", "https://images.unsplash.com/photo-1488646953014-85cb44e25828?w=800", "ASSISTANCE"),
            ServiceCardItem("5", "Uçuş Rezervasyonu / Flight Booking", "Hızlı, uygun fiyatlı yurt içi ve yurt dışı charter ve tarifeli uçuş biletleri.", "https://images.unsplash.com/photo-1436491865332-7a61a109cc05?w=800", "FLIGHT"),
            ServiceCardItem("6", "Mavi Yolculuk & Cruise / Cruise Trips", "Lüks cruise gemileri ve büyüleyici koyları keşfedeceğiniz mavi yolculuk paketleri.", "https://images.unsplash.com/photo-1540555700478-4be289fbecef?w=800", "CRUISE")
        )

        val result = validList.toMutableList()
        for (i in result.size until 6) {
            val item = defaults.getOrNull(i) ?: ServiceCardItem("${i + 1}", "Hizmet Kartı ${i + 1}", "", "", "PACKAGE_TOUR")
            result.add(item)
        }
        return result.take(6)
    }
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

    /**
     * Verilen rezervasyon / tur hareket tarihine (YYYY-MM-DD) karşılık gelen sezonsal komisyon oranını (%) döndürür.
     * Belirtilen tarihte aktif sezon yoksa varsayılan komisyon oranını (%12.5) döndürür.
     */
    fun getCommissionRateForDate(dateStr: String): Double {
        if (dateStr.isBlank()) return 12.5
        val matchedSeason = seasons.firstOrNull { season ->
            season.startDate.isNotBlank() && season.endDate.isNotBlank() &&
            dateStr >= season.startDate && dateStr <= season.endDate
        }
        return matchedSeason?.commissionRate ?: 12.5
    }
}
