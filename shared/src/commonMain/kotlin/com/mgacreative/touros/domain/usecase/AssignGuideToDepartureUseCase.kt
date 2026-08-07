package com.mgacreative.touros.domain.usecase

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

/**
 * 2.5.2 Tur Kalkışına Rehber Atama Use Case.
 */
class AssignGuideToDepartureUseCase(
    private val supabaseClient: SupabaseClient
) {
    suspend operator fun invoke(departureId: String, guideId: String): Result<Boolean> {
        return runCatching {
            supabaseClient.postgrest.from("departures")
                .update(mapOf("guide_id" to guideId)) { filter { eq("id", departureId) } }
            true
        }
    }
}
