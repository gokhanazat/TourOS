package com.mgacreative.touros.domain.repository

import com.mgacreative.touros.domain.model.CompanySettings

/**
 * Şirket Ayarları Repository Arayüzü.
 */
interface CompanySettingsRepository {
    suspend fun getCompanySettings(companyId: String): Result<CompanySettings>
    suspend fun updateCompanySettings(settings: CompanySettings): Result<CompanySettings>
    suspend fun uploadLogo(companyId: String, fileBytes: ByteArray, fileName: String): Result<String>
    suspend fun uploadHeaderBanner(companyId: String, fileBytes: ByteArray, fileName: String): Result<String>
    suspend fun uploadPromoBannerImage(companyId: String, fileBytes: ByteArray, fileName: String): Result<String>
}
