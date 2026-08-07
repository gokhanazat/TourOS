package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.model.CurrentAccountItem
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * 3.1.4 Müşteri/Acente/Tedarikçi Cari Hesap Ekstre Dökümü Use Case.
 */
class GetCurrentAccountsUseCase(
    private val supabaseClient: SupabaseClient
) {
    suspend operator fun invoke(tenantId: String, entityTypeFilter: String? = null): Result<List<CurrentAccountItem>> {
        return runCatching {
            val params = buildJsonObject {
                put("p_tenant_id", tenantId)
                if (entityTypeFilter != null) {
                    put("p_entity_type", entityTypeFilter)
                }
            }
            supabaseClient.postgrest.rpc("get_current_account_statement", params)
                .decodeList<CurrentAccountItem>()
        }
    }
}
