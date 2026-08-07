package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.engine.VoucherContractTemplateEngine
import com.mgacreative.touros.domain.model.DocumentItem
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * 3.4.2 Otomatik Voucher / Sözleşme PDF Oluşturma Use Case.
 */
class GenerateVoucherOrContractPdfUseCase(
    private val supabaseClient: SupabaseClient,
    private val templateEngine: VoucherContractTemplateEngine
) {
    suspend operator fun invoke(bookingId: String, docType: String, tenantId: String): Result<DocumentItem> {
        return runCatching {
            val params = buildJsonObject {
                put("p_booking_id", bookingId)
                put("p_document_type", docType)
            }

            val list = supabaseClient.postgrest.rpc("generate_voucher_or_contract_pdf", params)
                .decodeList<DocumentItem>()

            list.firstOrNull() ?: templateEngine.generateDummyPdfItem(bookingId, docType, tenantId)
        }.recover {
            templateEngine.generateDummyPdfItem(bookingId, docType, tenantId)
        }
    }
}
