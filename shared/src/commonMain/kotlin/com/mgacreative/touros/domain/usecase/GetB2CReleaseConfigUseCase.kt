package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.model.B2CAppReleaseConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * 4.2.7 B2C Mobil Görsel Tasarım ve Yayın Konfigürasyonunu Getirme Use Case.
 */
class GetB2CReleaseConfigUseCase(
    private val supabaseClient: SupabaseClient
) {
    suspend operator fun invoke(tenantId: String): Result<B2CAppReleaseConfig> {
        return runCatching {
            val params = buildJsonObject {
                put("p_tenant_id", tenantId)
            }

            val list = supabaseClient.postgrest.rpc("get_b2c_release_app_config", params)
                .decodeList<B2CAppReleaseConfig>()

            list.firstOrNull() ?: B2CAppReleaseConfig()
        }.recover { B2CAppReleaseConfig() }
    }
}
