package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.model.PartialPaymentSummary
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * 3.2.2 Nakit/Kart/Havale ile Kısmi Ödeme (Depozito) Alımı Use Case.
 */
class ProcessPartialPaymentUseCase(
    private val supabaseClient: SupabaseClient
) {
    suspend operator fun invoke(
        bookingId: String,
        paymentMethod: String, // cash, credit_card, bank_transfer, online
        amount: Double,
        accountId: String? = null,
        referenceNo: String? = null,
        notes: String? = null,
        tenantId: String
    ): Result<PartialPaymentSummary> {
        if (amount <= 0) {
            return Result.failure(IllegalArgumentException("Ödeme tutarı 0'dan büyük olmalıdır."))
        }

        return runCatching {
            val params = buildJsonObject {
                put("p_booking_id", bookingId)
                put("p_payment_method", paymentMethod)
                put("p_amount", amount)
                if (accountId != null) put("p_account_id", accountId)
                if (referenceNo != null) put("p_reference_no", referenceNo)
                if (notes != null) put("p_notes", notes)
                put("p_tenant_id", tenantId)
            }

            val list = supabaseClient.postgrest.rpc("process_booking_partial_payment", params)
                .decodeList<PartialPaymentSummary>()

            list.firstOrNull() ?: PartialPaymentSummary(
                bookingId = bookingId,
                totalPaid = amount,
                paymentStatus = "PARTIALLY_PAID"
            )
        }
    }
}
