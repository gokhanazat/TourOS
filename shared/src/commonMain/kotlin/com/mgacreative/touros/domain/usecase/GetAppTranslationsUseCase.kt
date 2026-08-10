package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.model.AppTranslationItem
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * 4.4.1 Seçili Dil Çeviri Sözlüğünü Getirme Use Case.
 */
class GetAppTranslationsUseCase(
    private val supabaseClient: SupabaseClient
) {
    suspend operator fun invoke(languageCode: String, tenantId: String): Result<Map<String, String>> {
        return runCatching {
            val params = buildJsonObject {
                put("p_language_code", languageCode)
                put("p_tenant_id", tenantId)
            }

            val list = supabaseClient.postgrest.rpc("get_app_translations", params)
                .decodeList<AppTranslationItem>()

            val map = list.associate { it.translationKey to it.translationValue }
            if (map.isEmpty()) getFallbackMap(languageCode) else map
        }.recover { getFallbackMap(languageCode) }
    }

    private fun getFallbackMap(code: String): Map<String, String> {
        return when (code) {
            "en" -> mapOf("welcome_title" to "Welcome to TourOS Travel System", "search_tours" to "Search Tours", "checkout" to "Proceed to Checkout")
            "de" -> mapOf("welcome_title" to "Willkommen beim TourOS Reisesystem", "search_tours" to "Touren Suchen", "checkout" to "Zur Kasse")
            "ru" -> mapOf("welcome_title" to "Добро пожаловать в TourOS", "search_tours" to "Поиск туров", "checkout" to "Оформить заказ")
            "ar" -> mapOf("welcome_title" to "مرحبا بكم في نظام توروس للسياحة", "search_tours" to "البحث عن الجولات", "checkout" to "الدفع والتأكيد")
            "es" -> mapOf("welcome_title" to "Bienvenido al Sistema TourOS", "search_tours" to "Buscar Tours", "checkout" to "Proceder al Pago")
            else -> mapOf("welcome_title" to "TourOS Seyahat Sistemine Hoş Geldiniz", "search_tours" to "Tur Ara", "checkout" to "Ödemeye Geç")
        }
    }
}
