package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.model.AppLanguageItem
import com.mgacreative.touros.domain.repository.CompanySettingsRepository

/**
 * 4.4.1 Desteklenen Dilleri Getirme Use Case.
 */
class GetSupportedLanguagesUseCase(
    private val companySettingsRepository: CompanySettingsRepository
) {
    private val allAvailableLanguages = mapOf(
        "tr" to AppLanguageItem("tr", "Türkçe", false, "🇹🇷"),
        "en" to AppLanguageItem("en", "English", false, "🇬🇧"),
        "de" to AppLanguageItem("de", "Deutsch", false, "🇩🇪"),
        "ru" to AppLanguageItem("ru", "Русский", false, "🇷🇺"),
        "ar" to AppLanguageItem("ar", "العربية", true, "🇸🇦"),
        "es" to AppLanguageItem("es", "Español", false, "🇪🇸")
    )

    suspend operator fun invoke(companyId: String = "00000000-0000-0000-0000-000000000001"): Result<List<AppLanguageItem>> {
        return runCatching {
            val settings = companySettingsRepository.getCompanySettings(companyId).getOrNull()
            val supportedCodes = settings?.supportedLanguages ?: listOf("tr", "en", "de", "ru", "ar", "es")

            val list = supportedCodes.mapNotNull { code ->
                allAvailableLanguages[code]
            }

            if (list.isEmpty()) getFallbackLanguages() else list
        }.recover { getFallbackLanguages() }
    }

    private fun getFallbackLanguages(): List<AppLanguageItem> {
        return listOf(
            AppLanguageItem("tr", "Türkçe", false, "🇹🇷"),
            AppLanguageItem("en", "English", false, "🇬🇧"),
            AppLanguageItem("de", "Deutsch", false, "🇩🇪"),
            AppLanguageItem("ru", "Русский", false, "🇷🇺"),
            AppLanguageItem("ar", "العربية", true, "🇸🇦"),
            AppLanguageItem("es", "Español", false, "🇪🇸")
        )
    }
}
