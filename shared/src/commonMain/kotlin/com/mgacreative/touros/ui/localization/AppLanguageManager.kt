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

    private val _currentLanguage = MutableStateFlow(supportedLanguages.firstOrNull { it.code == "ru" } ?: supportedLanguages.first())
    val currentLanguage: StateFlow<AppLanguageItem> = _currentLanguage.asStateFlow()

    private var adminLanguageCode: String = "tr"
    private var publicLanguageCode: String = "ru"

    fun syncContextLanguage(isAdminContext: Boolean) {
        val targetCode = if (isAdminContext) adminLanguageCode else publicLanguageCode
        setLanguageByCode(targetCode)
    }

    fun setContextLanguage(isAdminContext: Boolean, code: String) {
        if (isAdminContext) {
            adminLanguageCode = code
        } else {
            publicLanguageCode = code
        }
        setLanguageByCode(code)
    }

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
        } ?: return key

        val exact = map[key]
        if (exact != null) return exact

        if (key.endsWith(":")) {
            val keyWithoutColon = key.dropLast(1).trimEnd()
            val translatedWithoutColon = map[keyWithoutColon]
            if (translatedWithoutColon != null) return "$translatedWithoutColon:"
        }

        return key
    }

    fun formatNights(nightsText: String, langCode: String = _currentLanguage.value.code): String {
        val clean = nightsText.trim()
        val direct = translate(clean, langCode)
        if (direct != clean) return direct

        val num = clean.filter { it.isDigit() }.toIntOrNull()
        return when (langCode.lowercase()) {
            "ru" -> {
                if (clean.contains("-")) {
                    val parts = clean.split("-")
                    val lastNum = parts.getOrNull(1)?.filter { it.isDigit() }?.toIntOrNull() ?: 10
                    val word = when {
                        lastNum % 100 in 11..19 -> "ночей"
                        lastNum % 10 == 1 -> "ночь"
                        lastNum % 10 in 2..4 -> "ночи"
                        else -> "ночей"
                    }
                    "${parts[0].trim()} - ${parts[1].filter { it.isDigit() }.trim()} $word"
                } else if (num != null) {
                    val word = when {
                        num % 100 in 11..19 -> "ночей"
                        num % 10 == 1 -> "ночь"
                        num % 10 in 2..4 -> "ночи"
                        else -> "ночей"
                    }
                    "$num $word"
                } else clean
            }
            "en" -> {
                if (num == 1) "$num Night"
                else if (num != null) "$num Nights"
                else clean.replace("Gece", "Nights").replace("gece", "nights")
            }
            "de" -> {
                if (num == 1) "$num Nacht"
                else if (num != null) "$num Nächte"
                else clean.replace("Gece", "Nächte").replace("gece", "nächte")
            }
            else -> clean
        }
    }
}
