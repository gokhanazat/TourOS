package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.model.CommissionRule
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

import com.mgacreative.touros.data.util.isValidUuid

/**
 * 3.1.6 Komisyon Kurallarını Getirme Use Case.
 */
class GetCommissionRulesUseCase(
    private val supabaseClient: SupabaseClient
) {
    suspend operator fun invoke(tenantId: String): Result<List<CommissionRule>> {
        return runCatching {
            supabaseClient.postgrest.from("commission_rules")
                .select {
                    filter {
                        if (tenantId.isValidUuid()) {
                            eq("tenant_id", tenantId)
                        }
                    }
                }
                .decodeList<CommissionRule>()
        }
    }
}
