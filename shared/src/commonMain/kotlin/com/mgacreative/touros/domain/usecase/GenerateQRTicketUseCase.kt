package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.model.B2CQRTicket
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * 4.2.3 Rezervasyon İçin QR Bilet Oluşturma Use Case.
 */
class GenerateQRTicketUseCase(
    private val supabaseClient: SupabaseClient
) {
    suspend operator fun invoke(bookingId: String, tenantId: String): Result<B2CQRTicket> {
        return runCatching {
            val params = buildJsonObject {
                put("p_tenant_id", tenantId)
                put("p_booking_id", bookingId)
            }

            val list = supabaseClient.postgrest.rpc("generate_booking_qr_ticket", params)
                .decodeList<B2CQRTicket>()

            list.firstOrNull() ?: B2CQRTicket(ticketId = bookingId)
        }.recover { B2CQRTicket(ticketId = bookingId) }
    }
}
