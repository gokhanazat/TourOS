package com.mgacreative.touros.domain.engine

import com.mgacreative.touros.data.adapter.InternalOperatorAdapter
import com.mgacreative.touros.domain.model.ota.OTABooking
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * 4.6.10 İptal ve Event Bildirimi (Sistem İçi, Operatör Tarafı).
 * Rezervasyon iptal edildiğinde InternalOperatorAdapter.cancelBooking() tetiklenir
 * ve operatör tarafına SADECE sistem içi bildirim (notifications tablosu & Bildirim Merkezi) iletilir.
 * E-POSTA GÖNDERİMİ YOKTUR.
 */
class MarketplaceCancellationReceiver(
    private val supabaseClient: SupabaseClient,
    private val internalOperatorAdapter: InternalOperatorAdapter
) {

    /**
     * Pazaryeri rezervasyonunu iptal eder, adaptörü tetikler ve operatörün Bildirim Merkezine sistem içi bildirim düşer.
     */
    suspend fun processMarketplaceCancellation(
        bookingId: String,
        reason: String
    ): Result<OTABooking> {
        return runCatching {
            // 1. InternalOperatorAdapter cancelBooking tetikle
            val adapterResult = internalOperatorAdapter.cancelBooking(bookingId, reason).getOrThrow()

            // 2. Operatörün notifications tablosuna SADECE sistem içi bildirim düşen Supabase RPC çağrısı
            val rpcParams = buildJsonObject {
                put("p_booking_id", bookingId)
                put("p_reason", reason)
            }
            supabaseClient.postgrest.rpc("cancel_operator_marketplace_booking", rpcParams)

            adapterResult
        }
    }
}
