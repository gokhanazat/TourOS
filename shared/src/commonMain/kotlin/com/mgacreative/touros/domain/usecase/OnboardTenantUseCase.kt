package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.model.TenantOnboardingResult
import com.mgacreative.touros.domain.repository.TenantRepository

/**
 * Yeni Tur Operatörü kaydolduğunda otomatik şirket ve Sistem Yöneticisi profili oluşturan Use Case.
 */
class OnboardTenantUseCase(
    private val tenantRepository: TenantRepository
) {
    suspend operator fun invoke(
        companyName: String,
        adminFullName: String
    ): Result<TenantOnboardingResult> {
        if (companyName.isBlank()) {
            return Result.failure(IllegalArgumentException("Şirket adı boş olamaz"))
        }
        if (adminFullName.isBlank()) {
            return Result.failure(IllegalArgumentException("Yönetici adı boş olamaz"))
        }
        return tenantRepository.onboardTenant(
            companyName = companyName.trim(),
            adminFullName = adminFullName.trim()
        )
    }
}
