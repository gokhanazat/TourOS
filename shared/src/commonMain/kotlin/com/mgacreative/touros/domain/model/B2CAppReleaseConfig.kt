package com.mgacreative.touros.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 4.2.7 B2C Mobil Uygulama Görsel Tasarım ve Play Store Yayın Konfigürasyonu.
 */
@Serializable
data class B2CAppReleaseConfig(
    @SerialName("version_name") val versionName: String = "1.0.0",
    @SerialName("version_code") val versionCode: Int = 100,
    @SerialName("min_supported_version") val minSupportedVersion: String = "1.0.0",
    @SerialName("release_track") val releaseTrack: String = "PRODUCTION",
    @SerialName("splash_theme") val splashTheme: String = "DARK_GRADIENT",
    @SerialName("brand_primary_color") val brandPrimaryColor: String = "#0F172A",
    @SerialName("brand_accent_color") val brandAccentColor: String = "#2563EB",
    @SerialName("updated_at") val updatedAt: String = "2026-08-06 17:39"
)
