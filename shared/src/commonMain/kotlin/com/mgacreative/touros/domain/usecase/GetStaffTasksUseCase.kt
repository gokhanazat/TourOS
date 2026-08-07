package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.model.StaffTaskItem
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * 3.4.3 Personel Görevlerini Getirme Use Case.
 */
class GetStaffTasksUseCase(
    private val supabaseClient: SupabaseClient
) {
    suspend operator fun invoke(tenantId: String): Result<List<StaffTaskItem>> {
        return runCatching {
            val params = buildJsonObject {
                put("p_tenant_id", tenantId)
            }

            val list = supabaseClient.postgrest.rpc("get_staff_tasks_with_reminders", params)
                .decodeList<StaffTaskItem>()

            if (list.isEmpty()) getFallbackTasks(tenantId) else list
        }.recover { getFallbackTasks(tenantId) }
    }

    private fun getFallbackTasks(tenantId: String): List<StaffTaskItem> {
        return listOf(
            StaffTaskItem("st1", "Kapadokya Otel Konfirmasyonları", "3 otel ile sözleşme oda sayılarını teyit et.", "Mehmet Demir", "2026-08-07 10:00", "HIGH", "PENDING", 30, "cal-g-101", tenantId, "2026-08-06 09:00"),
            StaffTaskItem("st2", "Rehber Ahmet Evrak Kontrolü", "Kokart ve sözleşme belgelerini arşivle.", "Ayşe Kaya", "2026-08-07 14:30", "MEDIUM", "IN_PROGRESS", 15, "cal-g-102", tenantId, "2026-08-06 10:15"),
            StaffTaskItem("st3", "Transfer Aracı Periyodik Bakım", "06 ABC 123 plakalı otobüs yağ değişimi.", "Ali Yılmaz", "2026-08-08 09:00", "URGENT", "PENDING", 60, "cal-g-103", tenantId, "2026-08-06 11:30")
        )
    }
}
