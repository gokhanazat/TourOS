package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.model.B2BAgencyPrivateReport
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * 4.1.5 Acenteye Özel RLS Korumalı Rapor Getirme Use Case.
 */
class GetB2BAgencyPrivateReportsUseCase(
    private val supabaseClient: SupabaseClient
) {
    suspend operator fun invoke(tenantId: String, agencyId: String? = null): Result<B2BAgencyPrivateReport> {
        return runCatching {
            val params = buildJsonObject {
                put("p_tenant_id", tenantId)
                if (agencyId != null) put("p_agency_id", agencyId)
            }

            val list = supabaseClient.postgrest.rpc("get_b2b_agency_private_reports", params)
                .decodeList<B2BAgencyPrivateReport>()

            list.firstOrNull() ?: B2BAgencyPrivateReport()
        }.recover { B2BAgencyPrivateReport() }
    }
}
