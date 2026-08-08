package com.mgacreative.touros.domain.engine

import com.mgacreative.touros.data.adapter.InternalOperatorAdapter
import com.mgacreative.touros.domain.model.ota.OTAAccount
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * 4.6.9 Rezervasyon Routing Motoru (MarketplaceBookingRoutingEngine).
 * Tur kodundaki prefiksten (ör. ANK) ilgili operator_company_id'yi çözer.
 * Rezervasyon kaydedilince InternalOperatorAdapter.confirmBooking() çağrılarak
 * operatörün kendi bookings tablosuna cross-tenant kayıt ve teyit oluşturur.
 */
class MarketplaceBookingRoutingEngine(
    private val supabaseClient: SupabaseClient,
    private val internalOperatorAdapter: InternalOperatorAdapter
) {

    /**
     * Tur kodundan Operatör Prefiksini Ayıklar (ör. "ANK-00001" -> "ANK")
     */
    fun extractPrefixFromTourCode(tourCode: String): String {
        val trimmed = tourCode.trim().uppercase()
        val parts = trimmed.split("-")
        return if (parts.size >= 2) parts[0] else "TUR"
    }

    /**
     * Tur kodunun prefiksine göre Operatör Firma ID'sini Çözer.
     */
    suspend fun resolveOperatorCompanyId(tourCode: String): Result<String> {
        return runCatching {
            val prefix = extractPrefixFromTourCode(tourCode)
            val params = buildJsonObject { put("p_tour_code", tourCode) }
            val resolvedId = supabaseClient.postgrest.rpc("resolve_operator_company_id", params)
                .decodeSingleOrNull<String>()
            
            resolvedId ?: throw IllegalStateException("'$prefix' kodlu Tur Operatörü bulunamadı.")
        }
    }

    /**
     * Acenteden gelen rezervasyonu operatör firmaya route eder ve confirmBooking çağırır.
     */
    suspend fun routeAndConfirmBooking(
        agencyId: String,
        tourCode: String,
        tourId: String,
        customerName: String,
        paxCount: Int,
        totalAmount: Double
    ): Result<String> {
        return runCatching {
            val operatorCompanyId = resolveOperatorCompanyId(tourCode).getOrThrow()

            // Cross-tenant Supabase RPC ile operatörün bookings tablosuna doğrudan kayıt yap
            val rpcParams = buildJsonObject {
                put("p_agency_id", agencyId)
                put("p_operator_company_id", operatorCompanyId)
                put("p_tour_id", tourId)
                put("p_customer_name", customerName)
                put("p_pax_count", paxCount)
                put("p_total_amount", totalAmount)
            }

            val bookingId = supabaseClient.postgrest.rpc("confirm_operator_marketplace_booking", rpcParams)
                .decodeSingleOrNull<String>() ?: "bkg-routed-001"

            // InternalOperatorAdapter üzerinden yetkili konfirmasyon
            val account = OTAAccount(accountId = agencyId, apiKey = operatorCompanyId)
            internalOperatorAdapter.authenticate(account)
            internalOperatorAdapter.confirmBooking(bookingId)

            bookingId
        }
    }
}
