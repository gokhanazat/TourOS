package com.mgacreative.touros.data.repository

import com.mgacreative.touros.data.database.entity.AgencyBrandingEntity
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
    private var cachedSettings: CompanySettings? = null

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
            }.getOrNull()

            val branding = runCatching {
                supabaseClient.postgrest.from("agency_branding")
                    .select {
                        filter {
                            eq("agency_id", targetId)
                        }
                    }
                    .decodeSingleOrNull<AgencyBrandingEntity>()
            }.getOrNull()

            if (entity != null) {
                val seasons = runCatching {
                    json.decodeFromString<List<CompanySeason>>(entity.seasons)
                }.getOrDefault(emptyList())

                val loaded = CompanySettings(
                    id = entity.id,
                    name = entity.name.ifBlank { cachedSettings?.name ?: "" },
                    legalTitle = entity.legalTitle ?: cachedSettings?.legalTitle ?: "",
                    taxOffice = entity.taxOffice ?: cachedSettings?.taxOffice ?: "",
                    taxNumber = entity.taxNumber ?: cachedSettings?.taxNumber ?: "",
                    tradeRegistryNo = entity.tradeRegistryNo ?: cachedSettings?.tradeRegistryNo ?: "",
                    mersisNo = entity.mersisNo ?: cachedSettings?.mersisNo ?: "",
                    address = entity.address ?: cachedSettings?.address ?: "",
                    phone = entity.phone ?: cachedSettings?.phone ?: "",
                    email = entity.email ?: cachedSettings?.email ?: "",
                    companyType = entity.companyType,
                    operatorCode = entity.operatorCode,
                    logoUrl = entity.logoUrl ?: branding?.customLogoUrl ?: cachedSettings?.logoUrl,
                    themeColor = entity.themeColor.ifBlank { cachedSettings?.themeColor ?: "#1F4E5F" },
                    taxRate = entity.taxRate,
                    seasons = seasons.ifEmpty { cachedSettings?.seasons ?: emptyList() },
                    supportedCurrencies = entity.supportedCurrencies,
                    supportedLanguages = entity.supportedLanguages,
                    headerImageUrl = branding?.headerImageUrl ?: cachedSettings?.headerImageUrl,
                    heroSubtitle = branding?.heroSubtitle ?: cachedSettings?.heroSubtitle ?: "",
                    footerText = branding?.footerText ?: cachedSettings?.footerText ?: "",
                    webEmail = branding?.contactEmail ?: cachedSettings?.webEmail ?: "",
                    webPhone = branding?.contactPhone ?: cachedSettings?.webPhone ?: "",
                    webWhatsapp = branding?.whatsappNumber ?: cachedSettings?.webWhatsapp ?: "",
                    webAddress = branding?.contactAddress ?: cachedSettings?.webAddress ?: "",
                    bankName = entity.bankName ?: cachedSettings?.bankName,
                    iban = entity.iban ?: cachedSettings?.iban,
                    accountHolder = entity.accountHolder ?: cachedSettings?.accountHolder,
                    paypalEmail = entity.paypalEmail ?: cachedSettings?.paypalEmail,
                    paypalMeUrl = entity.paypalMeUrl ?: cachedSettings?.paypalMeUrl,
                    defaultMasterAgencyId = entity.defaultMasterAgencyId ?: cachedSettings?.defaultMasterAgencyId ?: "00000000-0000-0000-0000-000000000001",
                    defaultMasterAgencyCode = entity.defaultMasterAgencyCode ?: cachedSettings?.defaultMasterAgencyCode ?: "AGN-MASTER"
                )
                cachedSettings = loaded
                loaded
            } else {
                cachedSettings ?: CompanySettings(
                    id = targetId,
                    name = "",
                    taxRate = 20.0,
                    supportedCurrencies = listOf("TRY", "EUR", "USD"),
                    supportedLanguages = listOf("tr", "en"),
                    defaultMasterAgencyId = "00000000-0000-0000-0000-000000000001",
                    defaultMasterAgencyCode = "AGN-MASTER"
                )
            }
        }
    }

    override suspend fun updateCompanySettings(settings: CompanySettings): Result<CompanySettings> {
        return runCatching {
            cachedSettings = settings
            val seasonsJson = json.encodeToString(settings.seasons)
            val targetId = if (settings.id.isValidUuid()) settings.id else "00000000-0000-0000-0000-000000000001"

            val slugValue = settings.name.lowercase()
                .replace(" ", "-")
                .replace("ğ", "g")
                .replace("ü", "u")
                .replace("ş", "s")
                .replace("ı", "i")
                .replace("ö", "o")
                .replace("ç", "c")
                .ifBlank { "company-${targetId.take(8)}" }

            val upsertPayload = buildJsonObject {
                put("id", targetId)
                put("tenant_id", targetId)
                put("slug", slugValue)
                put("company_type", "acente")
                put("operator_code", "ACT")
                put("name", settings.name)
                put("legal_title", settings.legalTitle)
                put("tax_office", settings.taxOffice)
                put("tax_number", settings.taxNumber)
                put("trade_registry_no", settings.tradeRegistryNo)
                put("mersis_no", settings.mersisNo)
                put("address", settings.address)
                put("phone", settings.phone)
                put("email", settings.email)
                settings.bankName?.let { put("bank_name", it) }
                settings.iban?.let { put("iban", it) }
                settings.accountHolder?.let { put("account_holder", it) }
                settings.paypalEmail?.let { put("paypal_email", it) }
                settings.paypalMeUrl?.let { put("paypal_me_url", it) }
                settings.defaultMasterAgencyId?.let { put("default_master_agency_id", it) }
                settings.defaultMasterAgencyCode?.let { put("default_master_agency_code", it) }
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

            supabaseClient.postgrest.from("companies")
                .upsert(upsertPayload) {
                    onConflict = "id"
                }

            val existingBranding = runCatching {
                supabaseClient.postgrest.from("agency_branding")
                    .select { filter { eq("agency_id", targetId) } }
                    .decodeSingleOrNull<AgencyBrandingEntity>()
            }.getOrNull()

            val brandingPayload = buildJsonObject {
                existingBranding?.id?.takeIf { it.isNotBlank() }?.let { put("id", it) }
                put("agency_id", targetId)
                put("hero_title", settings.name)
                put("hero_subtitle", settings.heroSubtitle)
                put("footer_text", settings.footerText)
                put("contact_phone", settings.webPhone)
                put("contact_email", settings.webEmail)
                put("whatsapp_number", settings.webWhatsapp)
                put("contact_address", settings.webAddress)
                if (!settings.logoUrl.isNullOrBlank()) put("custom_logo_url", settings.logoUrl)
                if (!settings.headerImageUrl.isNullOrBlank()) put("header_image_url", settings.headerImageUrl)
            }

            runCatching {
                supabaseClient.postgrest.from("agency_branding")
                    .upsert(brandingPayload) {
                        onConflict = "agency_id"
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
            val targetId = if (companyId.isValidUuid()) companyId else "00000000-0000-0000-0000-000000000001"
            val cleanFileName = fileName
                .substringAfterLast("/")
                .substringAfterLast("\\")
                .lowercase()
                .replace(" ", "_")
                .replace("ğ", "g")
                .replace("ü", "u")
                .replace("ş", "s")
                .replace("ı", "i")
                .replace("ö", "o")
                .replace("ç", "c")
                .replace("i̇", "i")
                .filter { it.isLetterOrDigit() || it == '.' || it == '_' || it == '-' }

            val sanitizedPath = "company_${targetId}_${cleanFileName.ifBlank { "logo.png" }}"
            val bucket = supabaseClient.storage.from("company-logos")
            bucket.upload(sanitizedPath, fileBytes) {
                upsert = true
            }
            val publicUrl = bucket.publicUrl(sanitizedPath)

            val logoPayload = buildJsonObject {
                put("logo_url", publicUrl)
            }

            runCatching {
                supabaseClient.postgrest.from("companies")
                    .update(logoPayload) {
                        filter {
                            eq("id", targetId)
                        }
                    }
            }

            val existingBranding = runCatching {
                supabaseClient.postgrest.from("agency_branding")
                    .select { filter { eq("agency_id", targetId) } }
                    .decodeSingleOrNull<AgencyBrandingEntity>()
            }.getOrNull()

            val brandingPayload = buildJsonObject {
                existingBranding?.id?.takeIf { it.isNotBlank() }?.let { put("id", it) }
                put("agency_id", targetId)
                put("hero_title", existingBranding?.heroTitle ?: cachedSettings?.name ?: "")
                put("hero_subtitle", existingBranding?.heroSubtitle ?: cachedSettings?.heroSubtitle ?: "")
                put("footer_text", existingBranding?.footerText ?: cachedSettings?.footerText ?: "")
                put("custom_logo_url", publicUrl)
                existingBranding?.headerImageUrl?.let { put("header_image_url", it) }
                existingBranding?.contactPhone?.let { put("contact_phone", it) }
                existingBranding?.contactEmail?.let { put("contact_email", it) }
                existingBranding?.whatsappNumber?.let { put("whatsapp_number", it) }
                existingBranding?.contactAddress?.let { put("contact_address", it) }
            }

            runCatching {
                supabaseClient.postgrest.from("agency_branding")
                    .upsert(brandingPayload)
            }

            cachedSettings = cachedSettings?.copy(logoUrl = publicUrl)
            publicUrl
        }
    }

    override suspend fun uploadHeaderBanner(
        companyId: String,
        fileBytes: ByteArray,
        fileName: String
    ): Result<String> {
        return runCatching {
            val targetId = if (companyId.isValidUuid()) companyId else "00000000-0000-0000-0000-000000000001"
            val cleanFileName = fileName
                .substringAfterLast("/")
                .substringAfterLast("\\")
                .lowercase()
                .replace(" ", "_")
                .replace("ğ", "g")
                .replace("ü", "u")
                .replace("ş", "s")
                .replace("ı", "i")
                .replace("ö", "o")
                .replace("ç", "c")
                .replace("i̇", "i")
                .filter { it.isLetterOrDigit() || it == '.' || it == '_' || it == '-' }

            val sanitizedPath = "company_${targetId}_header_${cleanFileName.ifBlank { "header.png" }}"
            val bucket = supabaseClient.storage.from("company-logos")
            bucket.upload(sanitizedPath, fileBytes) {
                upsert = true
            }
            val publicUrl = bucket.publicUrl(sanitizedPath)

            val existingBranding = runCatching {
                supabaseClient.postgrest.from("agency_branding")
                    .select { filter { eq("agency_id", targetId) } }
                    .decodeSingleOrNull<AgencyBrandingEntity>()
            }.getOrNull()

            val headerPayload = buildJsonObject {
                existingBranding?.id?.takeIf { it.isNotBlank() }?.let { put("id", it) }
                put("agency_id", targetId)
                put("hero_title", existingBranding?.heroTitle ?: cachedSettings?.name ?: "")
                put("hero_subtitle", existingBranding?.heroSubtitle ?: cachedSettings?.heroSubtitle ?: "")
                put("footer_text", existingBranding?.footerText ?: cachedSettings?.footerText ?: "")
                put("header_image_url", publicUrl)
                existingBranding?.customLogoUrl?.let { put("custom_logo_url", it) }
                existingBranding?.contactPhone?.let { put("contact_phone", it) }
                existingBranding?.contactEmail?.let { put("contact_email", it) }
                existingBranding?.whatsappNumber?.let { put("whatsapp_number", it) }
                existingBranding?.contactAddress?.let { put("contact_address", it) }
            }

            runCatching {
                supabaseClient.postgrest.from("agency_branding")
                    .upsert(headerPayload)
            }

            cachedSettings = cachedSettings?.copy(headerImageUrl = publicUrl)
            publicUrl
        }
    }
}
