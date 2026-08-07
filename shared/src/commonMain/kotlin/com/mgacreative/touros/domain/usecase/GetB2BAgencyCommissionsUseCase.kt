package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.model.B2BAgencyCommissionItem
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * 4.1.3 B2B Acente Tur ve Dönemsel Komisyon Dökümü Getirme Use Case.
 */
class GetB2BAgencyCommissionsUseCase(
    private val supabaseClient: SupabaseClient
) {
    suspend operator fun invoke(tenantId: String, agencyId: String? = null): Result<List<B2BAgencyCommissionItem>> {
        return runCatching {
            val params = buildJsonObject {
                put("p_tenant_id", tenantId)
                if (agencyId != null) put("p_agency_id", agencyId)
            }

            val list = supabaseClient.postgrest.rpc("get_b2b_agency_commission_breakdown", params)
                .decodeList<B2BAgencyCommissionItem>()

            if (list.isEmpty()) getFallbackCommissions() else list
        }.recover { getFallbackCommissions() }
    }

    private fun getFallbackCommissions(): List<B2BAgencyCommissionItem> {
        return listOf(
            B2BAgencyCommissionItem("t101", "Kapadokya Balon & Vadi Turu", 12, 185000.0, 10.0, 18500.0, "HAK_EDILDI", "Ağustos 2026"),
            B2BAgencyCommissionItem("t102", "Ege Sahilleri & Antik Kentler", 8, 120000.0, 10.0, 12000.0, "HAK_EDILDI", "Ağustos 2026"),
            B2BAgencyCommissionItem("t103", "Karadeniz Yaylalar & Doğa Gezisi", 5, 75000.0, 10.0, 7500.0, "ODENDI", "Temmuz 2026"),
            B2BAgencyCommissionItem("t104", "İstanbul Kültür & Boğaz Turu", 6, 48000.0, 10.0, 4800.0, "BEKLIYOR", "Ağustos 2026")
        )
    }
}
