package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.model.ExchangeRate
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

/**
 * 3.2.5 TCMB Kurlarını Güncelleme Servis Use Case.
 */
class UpdateExchangeRatesUseCase(
    private val supabaseClient: SupabaseClient
) {
    suspend operator fun invoke(tenantId: String, newRates: List<ExchangeRate>): Result<List<ExchangeRate>> {
        return runCatching {
            supabaseClient.postgrest["exchange_rates"].upsert(newRates)
            newRates
        }.recover { newRates }
    }
}
