package com.mgacreative.touros.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 4.4.1 Desteklenen Dil Modeli.
 */
@Serializable
data class AppLanguageItem(
    val code: String = "tr",
    val name: String = "Türkçe",
    @SerialName("is_rtl") val isRtl: Boolean = false,
    @SerialName("flag_emoji") val flagEmoji: String = "🇹🇷"
)

/**
 * 4.4.1 Çeviri Sözlük Modeli.
 */
@Serializable
data class AppTranslationItem(
    @SerialName("language_code") val languageCode: String = "tr",
    @SerialName("translation_key") val translationKey: String = "welcome_title",
    @SerialName("translation_value") val translationValue: String = "TourOS Seyahat Sistemine Hoş Geldiniz"
)
