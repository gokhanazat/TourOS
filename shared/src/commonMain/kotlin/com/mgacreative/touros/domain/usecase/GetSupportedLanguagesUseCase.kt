package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.model.AppLanguageItem
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

/**
 * 4.4.1 Desteklenen Dilleri Getirme Use Case.
 */
class GetSupportedLanguagesUseCase(
    private val supabaseClient: SupabaseClient
) {
    suspend operator fun invoke(): Result<List<AppLanguageItem>> {
        return runCatching {
            val list = supabaseClient.postgrest.rpc("get_supported_languages")
                .decodeList<AppLanguageItem>()

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
            AppLanguageItem("fr", "Français", false, "🇫🇷")
        )
    }
}
