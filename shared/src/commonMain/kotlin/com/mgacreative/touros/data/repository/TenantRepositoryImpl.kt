package com.mgacreative.touros.data.repository

import com.mgacreative.touros.domain.model.TenantOnboardingResult
import com.mgacreative.touros.domain.repository.TenantRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class TenantRepositoryImpl(
    private val supabaseClient: SupabaseClient
) : TenantRepository {

    override suspend fun onboardTenant(
        companyName: String,
        adminFullName: String
    ): Result<TenantOnboardingResult> {
        return runCatching {
            val params = buildJsonObject {
                put("p_company_name", companyName)
                put("p_admin_full_name", adminFullName)
            }
            val result = supabaseClient.postgrest.rpc(
                function = "onboard_tenant",
                parameters = params
            ).decodeAs<TenantOnboardingResult>()

            result
        }
    }
}
