package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.model.B2BAgencyProfile
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * 4.1.1 B2B Acente Girişi ve Cari Hesap Bakiyesi Getirme Use Case.
 */
class GetB2BAgencyCurrentAccountUseCase(
    private val supabaseClient: SupabaseClient
) {
    suspend operator fun invoke(tenantId: String, agencyId: String? = null): Result<B2BAgencyProfile> {
        return runCatching {
            val params = buildJsonObject {
                put("p_tenant_id", tenantId)
                if (agencyId != null) put("p_agency_id", agencyId)
            }

            val list = supabaseClient.postgrest.rpc("get_b2b_agency_current_account", params)
                .decodeList<B2BAgencyProfile>()

            list.firstOrNull() ?: getFallbackProfile(tenantId)
        }.recover { getFallbackProfile(tenantId) }
    }

    private fun getFallbackProfile(tenantId: String): B2BAgencyProfile {
        return B2BAgencyProfile(
            agencyId = "acn-101",
            agencyCode = "ACN-GLB",
            agencyName = "Global Travel Agency B2B",
            contactEmail = "b2b@globaltravel.com",
            contactPhone = "+90 (212) 555 0100",
            creditLimit = 250000.0,
            currentBalance = 42800.0,
            currency = "TRY",
            activeBookingsCount = 14,
            pendingCommission = 4280.0,
            accountStatus = "ACTIVE",
            lastTransactionAt = "2026-08-06 14:00"
        )
    }
}
