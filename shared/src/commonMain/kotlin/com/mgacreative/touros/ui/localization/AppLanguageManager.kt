package com.mgacreative.touros.ui.localization

import com.mgacreative.touros.domain.model.AppLanguageItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Global Canlı Dil Yöneticisi — TourOS
 *
 * Tüm platformlarda (Web, Desktop, Android, iOS) seçili uygulama dilini reaktif tutar
 * ve menü, ekran başlığı, tablo etiketleri ve buton metinlerini 6 dilde dinamik olarak çözer.
 */
object AppLanguageManager {
    val supportedLanguages = listOf(
        AppLanguageItem("tr", "Türkçe", false, "🇹🇷"),
        AppLanguageItem("en", "English", false, "🇬🇧"),
        AppLanguageItem("de", "Deutsch", false, "🇩🇪"),
        AppLanguageItem("ru", "Русский", false, "🇷🇺"),
        AppLanguageItem("ar", "العربية", true, "🇸🇦"),
        AppLanguageItem("es", "Español", false, "🇪🇸")
    )

    private val _currentLanguage = MutableStateFlow(supportedLanguages.first())
    val currentLanguage: StateFlow<AppLanguageItem> = _currentLanguage.asStateFlow()

    fun setLanguage(lang: AppLanguageItem) {
        _currentLanguage.value = lang
    }

    fun setLanguage(code: String) {
        setLanguageByCode(code)
    }

    fun setLanguageByCode(code: String) {
        val match = supportedLanguages.firstOrNull { it.code.equals(code, ignoreCase = true) }
        if (match != null) {
            _currentLanguage.value = match
        }
    }

    fun translate(key: String, langCode: String = _currentLanguage.value.code): String {
        if (langCode == "tr") return key
        val map = when (langCode) {
            "en" -> TranslationsEN.map
            "de" -> TranslationsDE.map
            "ru" -> TranslationsRU.map
            "ar" -> TranslationsAR.map
            "es" -> TranslationsES.map
            else -> null
        }
        return map?.get(key) ?: key
    }
}
