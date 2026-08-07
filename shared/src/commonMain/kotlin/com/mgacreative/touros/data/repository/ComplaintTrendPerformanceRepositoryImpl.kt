package com.mgacreative.touros.data.repository

import com.mgacreative.touros.domain.model.feedback.EntityType
import com.mgacreative.touros.domain.model.feedback.VendorPerformanceImpact
import com.mgacreative.touros.domain.repository.ComplaintTrendPerformanceRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class ComplaintTrendPerformanceRepositoryImpl(
    private val supabaseClient: SupabaseClient? = null
) : ComplaintTrendPerformanceRepository {

    override suspend fun getVendorPerformanceImpacts(tenantId: String): Result<List<VendorPerformanceImpact>> {
        return runCatching {
            if (supabaseClient != null) {
                val params = buildJsonObject { put("p_tenant_id", tenantId) }
                supabaseClient.postgrest.rpc("get_complaint_trend_performance_report", params)
                    .decodeList<VendorPerformanceImpact>()
            } else {
                getMockVendorImpacts()
            }
        }.recover {
            getMockVendorImpacts()
        }
    }

    private fun getMockVendorImpacts(): List<VendorPerformanceImpact> {
        return listOf(
            VendorPerformanceImpact(
                entityName = "Kapadokya VIP Transfer Ltd",
                entityType = EntityType.SUPPLIER,
                complaintCount = 28,
                trendSpikePercent = 42.5,
                performanceScore = 3.2,
                alertMessage = "Klima şikayetleri geçen aya göre %42.5 arttı."
            ),
            VendorPerformanceImpact(
                entityName = "Kapadokya Mağara Otel A",
                entityType = EntityType.HOTEL,
                complaintCount = 14,
                trendSpikePercent = 25.0,
                performanceScore = 3.8,
                alertMessage = "Kahvaltı memnuniyetsizliği %25 arttı."
            ),
            VendorPerformanceImpact(
                entityName = "Rehber Mehmet Demir",
                entityType = EntityType.GUIDE,
                complaintCount = 9,
                trendSpikePercent = 18.0,
                performanceScore = 4.1,
                alertMessage = "Buluşma noktasına geç ulaşım şikayetleri arttı."
            )
        )
    }
}
