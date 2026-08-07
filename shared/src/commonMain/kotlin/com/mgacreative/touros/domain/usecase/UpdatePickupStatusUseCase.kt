package com.mgacreative.touros.domain.usecase

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

/**
 * 2.4.3 Pickup Noktası Yolcu Alındı / No-Show Durum Güncelleme Use Case.
 */
class UpdatePickupStatusUseCase(
    private val supabaseClient: SupabaseClient
) {
    suspend operator fun invoke(pickupId: String, status: String): Result<Boolean> {
        return runCatching {
            supabaseClient.postgrest.from("transfer_pickups")
                .update(mapOf("status" to status)) { filter { eq("id", pickupId) } }
            true
        }
    }
}
