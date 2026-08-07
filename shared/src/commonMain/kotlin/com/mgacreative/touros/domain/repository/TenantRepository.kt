package com.mgacreative.touros.domain.repository

import com.mgacreative.touros.domain.model.TenantOnboardingResult

/**
 * Tenant ve şirket yönetimi repository arayüzü.
 */
interface TenantRepository {
    suspend fun onboardTenant(
        companyName: String,
        adminFullName: String
    ): Result<TenantOnboardingResult>
}
