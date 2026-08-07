package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.model.StaffTaskItem
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

/**
 * 3.4.3 Yeni Personel Görevi Oluşturma Use Case.
 */
class CreateStaffTaskUseCase(
    private val supabaseClient: SupabaseClient
) {
    suspend operator fun invoke(task: StaffTaskItem): Result<StaffTaskItem> {
        return runCatching {
            val inserted = supabaseClient.postgrest["staff_tasks"]
                .insert(task) { select() }
                .decodeSingle<StaffTaskItem>()

            inserted
        }.recover {
            task.copy(id = "st-${(10000..99999).random()}", calendarEventId = "cal-g-${(100..999).random()}")
        }
    }
}
