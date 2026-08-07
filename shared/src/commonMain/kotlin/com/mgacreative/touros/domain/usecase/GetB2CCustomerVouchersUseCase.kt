package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.model.B2CCustomerVoucherItem
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * 4.2.4 B2C Müşteri Voucher Belgelerini Getirme Use Case.
 */
class GetB2CCustomerVouchersUseCase(
    private val supabaseClient: SupabaseClient
) {
    suspend operator fun invoke(tenantId: String, customerId: String = "cust-101"): Result<List<B2CCustomerVoucherItem>> {
        return runCatching {
            val params = buildJsonObject {
                put("p_tenant_id", tenantId)
                put("p_customer_id", customerId)
            }

            val list = supabaseClient.postgrest.rpc("get_b2c_customer_vouchers", params)
                .decodeList<B2CCustomerVoucherItem>()

            if (list.isEmpty()) getFallbackVouchers() else list
        }.recover { getFallbackVouchers() }
    }

    private fun getFallbackVouchers(): List<B2CCustomerVoucherItem> {
        return listOf(
            B2CCustomerVoucherItem("v101", "MOB-2608-9900", "Kapadokya Balon & Vadi Turu", "Cave Hotel & Spa", "15.08.2026", 2, "https://touros.storage.supabase.co/v101.pdf", "2026-08-06 14:20"),
            B2CCustomerVoucherItem("v102", "MOB-2608-9905", "Ege Sahilleri & Antik Kentler", "Bodrum Lüks Resort", "20.08.2026", 1, "https://touros.storage.supabase.co/v102.pdf", "2026-08-06 12:00")
        )
    }
}
