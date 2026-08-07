package com.mgacreative.touros.domain.usecase

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

/**
 * 2.5.3 Rehber Yolcu Yoklaması Alındı / Alınmadı Değiştirme Use Case.
 */
class TogglePassengerCheckInUseCase(
    private val supabaseClient: SupabaseClient
) {
    suspend operator fun invoke(passengerId: String, isChecked: Boolean): Result<Boolean> {
        return runCatching {
            supabaseClient.postgrest.from("passengers")
                .update(mapOf("is_check_in" to isChecked)) { filter { eq("id", passengerId) } }
            true
        }
    }
}
