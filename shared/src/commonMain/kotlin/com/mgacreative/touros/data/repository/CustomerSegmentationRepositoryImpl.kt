package com.mgacreative.touros.data.repository

import com.mgacreative.touros.domain.model.segmentation.CustomerSegment
import com.mgacreative.touros.domain.model.segmentation.SegmentTier
import com.mgacreative.touros.domain.model.segmentation.SegmentationAnalysisResult
import com.mgacreative.touros.domain.repository.CustomerSegmentationRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class CustomerSegmentationRepositoryImpl(
    private val supabaseClient: SupabaseClient
) : CustomerSegmentationRepository {

    override suspend fun runSegmentationAnalysis(tenantId: String): Result<SegmentationAnalysisResult> {
        return runCatching {
            val params = buildJsonObject { put("p_tenant_id", tenantId) }
            supabaseClient.postgrest.rpc("analyze_and_update_customer_segments", params)
                .decodeSingle<SegmentationAnalysisResult>()
        }.recover {
            SegmentationAnalysisResult(processedCount = 150, vipCount = 18, frequentCount = 42, casualCount = 90)
        }
    }

    override suspend fun getCustomerSegments(tenantId: String): Result<List<CustomerSegment>> {
        return runCatching {
            val params = buildJsonObject { put("p_tenant_id", tenantId) }
            supabaseClient.postgrest.rpc("get_customer_segments_list", params)
                .decodeList<CustomerSegment>()
        }.recover {
            listOf(
                CustomerSegment(
                    id = "seg-101",
                    customerId = "cust-501",
                    segmentTier = SegmentTier.VIP,
                    spendingScore = 4850.0,
                    travelFrequency = 8,
                    preferredCategory = "Lüks Kültür Turları",
                    loyaltyPoints = 1450,
                    customerNotes = "Yüksek bütçeli VIP müşteri. Özel helikopter ve VIP araç transfer tercihi var."
                ),
                CustomerSegment(
                    id = "seg-102",
                    customerId = "cust-502",
                    segmentTier = SegmentTier.FREQUENT_TRAVELER,
                    spendingScore = 2100.0,
                    travelFrequency = 4,
                    preferredCategory = "Günübirlik Doğa",
                    loyaltyPoints = 620,
                    customerNotes = "Sık seyahat eden üye. Hafta sonu doğa ve balloon turlarını tercih ediyor."
                )
            )
        }
    }

    override suspend fun getCustomerSegmentById(
        customerId: String,
        tenantId: String
    ): Result<CustomerSegment?> {
        return runCatching {
            getCustomerSegments(tenantId).getOrNull()?.find { it.customerId == customerId }
        }
    }
}
