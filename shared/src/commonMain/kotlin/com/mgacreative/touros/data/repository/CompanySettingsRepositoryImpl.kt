package com.mgacreative.touros.data.repository

import com.mgacreative.touros.data.database.entity.CompanyEntity
import com.mgacreative.touros.data.util.isValidUuid
import com.mgacreative.touros.domain.model.CompanySeason
import com.mgacreative.touros.domain.model.CompanySettings
import com.mgacreative.touros.domain.repository.CompanySettingsRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray

class CompanySettingsRepositoryImpl(
    private val supabaseClient: SupabaseClient
) : CompanySettingsRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun getCompanySettings(companyId: String): Result<CompanySettings> {
        return runCatching {
            val targetId = if (companyId.isValidUuid()) companyId else "00000000-0000-0000-0000-000000000001"
            val entity = runCatching {
                supabaseClient.postgrest.from("companies")
                    .select {
                        filter {
                            eq("id", targetId)
                        }
                    }
                    .decodeSingle<CompanyEntity>()
            }.getOrElse {
                CompanyEntity(
                    id = targetId,
                    name = "TourOS Agency",
                    logoUrl = null,
                    themeColor = "#1976D2",
                    taxRate = 20.0,
                    seasons = "[]",
                    supportedCurrencies = listOf("TRY", "EUR", "USD"),
                    supportedLanguages = listOf("tr", "en")
                )
            }

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
            val targetId = if (settings.id.isValidUuid()) settings.id else "00000000-0000-0000-0000-000000000001"

            val updatePayload = buildJsonObject {
                put("name", settings.name)
                settings.logoUrl?.let { put("logo_url", it) }
                put("theme_color", settings.themeColor)
                put("tax_rate", settings.taxRate)
                put("seasons", seasonsJson)
                putJsonArray("supported_currencies") {
                    settings.supportedCurrencies.forEach { add(it) }
                }
                putJsonArray("supported_languages") {
                    settings.supportedLanguages.forEach { add(it) }
                }
            }

            runCatching {
                supabaseClient.postgrest.from("companies")
                    .update(updatePayload) {
                        filter {
                            eq("id", targetId)
                        }
                    }
            }

            val brandingPayload = buildJsonObject {
                put("agency_id", targetId)
                put("hero_title", settings.name)
                settings.logoUrl?.let { put("custom_logo_url", it) }
                settings.headerImageUrl?.let { put("header_image_url", it) }
            }

            runCatching {
                supabaseClient.postgrest.from("agency_branding")
                    .upsert(brandingPayload)
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
            val targetId = if (companyId.isValidUuid()) companyId else "00000000-0000-0000-0000-000000000001"
            val path = "company_${targetId}_$fileName"
            val bucket = supabaseClient.storage.from("company-logos")
            bucket.upload(path, fileBytes) {
                upsert = true
            }
            val publicUrl = bucket.publicUrl(path)

            val logoPayload = buildJsonObject {
                put("logo_url", publicUrl)
            }

            supabaseClient.postgrest.from("companies")
                .update(logoPayload) {
                    filter {
                        eq("id", targetId)
                    }
                }

            publicUrl
        }
    }
}
