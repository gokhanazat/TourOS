package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.model.CompanySettings
import com.mgacreative.touros.domain.repository.CompanySettingsRepository

class GetCompanySettingsUseCase(
    private val repository: CompanySettingsRepository
) {
    suspend operator fun invoke(companyId: String): Result<CompanySettings> {
        if (companyId.isBlank()) {
            return Result.failure(IllegalArgumentException("Geçersiz Şirket ID"))
        }
        return repository.getCompanySettings(companyId)
    }
}

class UpdateCompanySettingsUseCase(
    private val repository: CompanySettingsRepository
) {
    suspend operator fun invoke(settings: CompanySettings): Result<CompanySettings> {
        if (settings.name.isBlank()) {
            return Result.failure(IllegalArgumentException("Firma adı boş olamaz"))
        }
        if (settings.taxRate < 0) {
            return Result.failure(IllegalArgumentException("Vergi oranı negatif olamaz"))
        }
        return repository.updateCompanySettings(settings)
    }
}

class UploadCompanyLogoUseCase(
    private val repository: CompanySettingsRepository
) {
    suspend operator fun invoke(companyId: String, fileBytes: ByteArray, fileName: String): Result<String> {
        if (companyId.isBlank()) {
            return Result.failure(IllegalArgumentException("Geçersiz Şirket ID"))
        }
        if (fileBytes.isEmpty()) {
            return Result.failure(IllegalArgumentException("Görsel dosyası boş olamaz"))
        }
        return repository.uploadLogo(companyId, fileBytes, fileName)
    }
}
