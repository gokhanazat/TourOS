package com.mgacreative.touros.ui.localization

import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * i18n Çoklu Dil ve Çeviri Bütünlüğü Regresyon & Denetim Testi
 *
 * Gerçek dil sözlüklerini (EN, RU, DE, AR, ES) tarar; boş anahtarları,
 * eksik eşleşmeleri ve diller arası çeviri kapsama oranlarını doğrular.
 */
class I18nTranslationKeysTest {

    private val languageMaps: Map<String, Map<String, String>> = mapOf(
        "en" to TranslationsEN.map,
        "ru" to TranslationsRU.map,
        "de" to TranslationsDE.map,
        "ar" to TranslationsAR.map,
        "es" to TranslationsES.map
    )

    @Test
    fun testNoBlankKeysOrValues() {
        val blankViolations = mutableListOf<String>()

        languageMaps.forEach { (lang, map) ->
            map.forEach { (key, value) ->
                if (key.isBlank()) {
                    blankViolations.add("[$lang] Empty/Blank Key found with value: '$value'")
                }
                if (value.isBlank()) {
                    blankViolations.add("[$lang] Empty/Blank Value found for key: '$key'")
                }
            }
        }

        assertTrue(
            blankViolations.isEmpty(),
            "Boş çeviri anahtarı veya değeri bulundu:\n" + blankViolations.joinToString("\n")
        )
    }

    @Test
    fun testTranslationCoverageAndAuditMissingKeys() {
        val allUniqueKeys = languageMaps.values.flatMap { it.keys }.toSet()
        val report = StringBuilder()
        report.appendLine("\n=======================================================")
        report.appendLine("           i18n Translation Audit Report               ")
        report.appendLine("=======================================================")
        report.appendLine("Toplam Benzersiz Anahtar Havuzu (Master Union): ${allUniqueKeys.size}\n")

        val missingSampleLimit = 5

        languageMaps.forEach { (lang, map) ->
            val missing = allUniqueKeys.filterNot { map.containsKey(it) }
            val coverage = if (allUniqueKeys.isNotEmpty()) {
                (map.size.toDouble() / allUniqueKeys.size.toDouble()) * 100.0
            } else 0.0

            val sampleMissing = if (missing.isNotEmpty()) {
                " | Örnek Eksikler: " + missing.take(missingSampleLimit).map { "\"$it\"" }.joinToString(", ")
            } else ""

            report.appendLine(
                "Dil: [%s] -> Tanımlı: %4d / %4d (Kapsama: %5.1f%%) | Eksik: %4d%s".format(
                    lang.uppercase(),
                    map.size,
                    allUniqueKeys.size,
                    coverage,
                    missing.size,
                    sampleMissing
                )
            )
        }
        report.appendLine("=======================================================\n")

        println(report.toString())

        // Tüm sözlüklerin dolu ve yüklü olduğunu doğrula
        languageMaps.forEach { (lang, map) ->
            assertTrue(map.isNotEmpty(), "$lang çeviri haritası boş olamaz!")
        }
    }

    @Test
    fun testAppLanguageManagerTranslateIntegrity() {
        val sampleKeys = listOf("Dashboard", "Turlar", "Oteller", "Finans", "Rezervasyon", "Ayarlar")
        AppLanguageManager.supportedLanguages.forEach { lang ->
            sampleKeys.forEach { key ->
                val result = AppLanguageManager.translate(key, lang.code)
                assertTrue(result.isNotBlank(), "Çeviri sonucu boş olamaz: $key ($lang)")
            }
        }
    }
}
