package com.mgacreative.touros.data.repository

import com.mgacreative.touros.data.database.entity.CompanyEntity
import com.mgacreative.touros.domain.model.CompanySeason
import com.mgacreative.touros.domain.model.CompanySettings
import com.mgacreative.touros.domain.repository.CompanySettingsRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class CompanySettingsRepositoryImpl(
    private val supabaseClient: SupabaseClient
) : CompanySettingsRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun getCompanySettings(companyId: String): Result<CompanySettings> {
        return runCatching {
            val entity = supabaseClient.postgrest.from("companies")
                .select {
                    filter {
                        eq("id", companyId)
                    }
                }
                .decodeSingle<CompanyEntity>()

            val seasons = runCatching {
                json.decodeFromString<List<CompanySeason>>(entity.seasons)
            }.getOrDefault(emptyList())

            CompanySettings(
                id = entity.id,
                name = entity.name,
                logoUrl = entity.logoUrl,
                themeColor = entity.themeColor,
                taxRate = entity.taxRate,
                seasons = seasons,
                supportedCurrencies = entity.supportedCurrencies,
                supportedLanguages = entity.supportedLanguages
            )
        }
    }

    override suspend fun updateCompanySettings(settings: CompanySettings): Result<CompanySettings> {
        return runCatching {
            val seasonsJson = json.encodeToString(settings.seasons)

            supabaseClient.postgrest.from("companies")
                .update(
                    mapOf(
                        "name" to settings.name,
                        "logo_url" to settings.logoUrl,
                        "theme_color" to settings.themeColor,
                        "tax_rate" to settings.taxRate,
                        "seasons" to seasonsJson,
                        "supported_currencies" to settings.supportedCurrencies,
                        "supported_languages" to settings.supportedLanguages
                    )
                ) {
                    filter {
                        eq("id", settings.id)
                    }
                }

            settings
        }
    }

    override suspend fun uploadLogo(
        companyId: String,
        fileBytes: ByteArray,
        fileName: String
    ): Result<String> {
        return runCatching {
            val path = "company_${companyId}_$fileName"
            val bucket = supabaseClient.storage.from("company-logos")
            bucket.upload(path, fileBytes) {
                upsert = true
            }
            val publicUrl = bucket.publicUrl(path)

            // Ayrıca companies tablosundaki logo_url'yi güncelle
            supabaseClient.postgrest.from("companies")
                .update(mapOf("logo_url" to publicUrl)) {
                    filter {
                        eq("id", companyId)
                    }
                }

            publicUrl
        }
    }
}
