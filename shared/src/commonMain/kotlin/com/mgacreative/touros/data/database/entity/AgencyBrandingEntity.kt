package com.mgacreative.touros.data.database.entity

import com.mgacreative.touros.domain.model.PromoBannerItem
import com.mgacreative.touros.domain.model.ServiceCardItem
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * agency_branding tablosu – Acentenin storefront özelleştirme tercihleri.
 */
@Serializable
data class AgencyBrandingEntity(
    val id: String = "",
    @SerialName("agency_id") val agencyId: String = "",
    @SerialName("hero_title") val heroTitle: String = "",
    @SerialName("hero_subtitle") val heroSubtitle: String = "",
    @SerialName("custom_logo_url") val customLogoUrl: String? = null,
    @SerialName("primary_color") val primaryColor: String = "#1F4E5F",
    @SerialName("footer_text") val footerText: String = "",
    @SerialName("header_image_url") val headerImageUrl: String? = null,
    @SerialName("contact_phone") val contactPhone: String? = null,
    @SerialName("contact_email") val contactEmail: String? = null,
    @SerialName("whatsapp_number") val whatsappNumber: String? = null,
    @SerialName("contact_address") val contactAddress: String? = null,
    @SerialName("web_mersis_no") val webMersisNo: String? = null,
    @SerialName("web_tax_office") val webTaxOffice: String? = null,
    @SerialName("web_tax_number") val webTaxNumber: String? = null,
    @SerialName("meta_description") val metaDescription: String? = null,
    @SerialName("promo_banners") val promoBanners: List<PromoBannerItem> = emptyList(),
    @SerialName("service_cards") val serviceCards: List<ServiceCardItem> = emptyList(),
    @SerialName("desktop_app_url") val desktopAppUrl: String? = null,
    @SerialName("android_apk_url") val androidApkUrl: String? = null,
    @SerialName("is_app_download_active") val isAppDownloadActive: Boolean? = true,
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = ""
)

/**
 * Travelata.ru tarzı Çoklu Operatör Karşılaştırmalı Acente Storefront Tur Kartı İtemı.
 */
@Serializable
data class AgencyStorefrontTourItem(
    @SerialName("tour_id") val tourId: String = "",
    val title: String = "",
    val code: String = "",
    val country: String = "",
    val city: String = "",
    val nights: Int = 3,
    @SerialName("base_price") val basePrice: Double = 0.0,
    @SerialName("final_price") val finalPrice: Double = 0.0,
    @SerialName("operator_name") val operatorName: String = "",
    @SerialName("compared_operator_count") val comparedOperatorCount: Int = 1,
    @SerialName("cover_image_url") val coverImageUrl: String? = null
)
