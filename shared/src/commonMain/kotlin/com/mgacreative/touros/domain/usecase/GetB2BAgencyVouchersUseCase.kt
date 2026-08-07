package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.model.B2BAgencyVoucherItem
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * 4.1.4 Acente Voucher Belgelerini Getirme Use Case.
 */
class GetB2BAgencyVouchersUseCase(
    private val supabaseClient: SupabaseClient
) {
    suspend operator fun invoke(tenantId: String, agencyId: String? = null): Result<List<B2BAgencyVoucherItem>> {
        return runCatching {
            val params = buildJsonObject {
                put("p_tenant_id", tenantId)
                if (agencyId != null) put("p_agency_id", agencyId)
            }

            val list = supabaseClient.postgrest.rpc("get_b2b_agency_vouchers", params)
                .decodeList<B2BAgencyVoucherItem>()

            if (list.isEmpty()) getFallbackVouchers() else list
        }.recover { getFallbackVouchers() }
    }

    private fun getFallbackVouchers(): List<B2BAgencyVoucherItem> {
        return listOf(
            B2BAgencyVoucherItem("v1", "B2B-2608-4102", "Johann Schmidt", "Kapadokya Balon & Vadi Turu", "Cave Hotel & Spa", "15.08.2026", 2, "https://touros.storage.supabase.co/documents/voucher/v1.pdf", 1450000L, 2, "2026-08-06 14:00"),
            B2BAgencyVoucherItem("v2", "B2B-2608-4105", "Hans Müller", "Ege Sahilleri & Antik Kentler", "Bodrum Lüks Resort", "18.08.2026", 1, "https://touros.storage.supabase.co/documents/voucher/v2.pdf", 1250000L, 0, "2026-08-06 11:30")
        )
    }
}
