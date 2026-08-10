package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.model.CurrentAccountItem
import com.mgacreative.touros.domain.repository.BookingRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * 3.1.4 Müşteri/Acente/Tedarikçi Cari Hesap Ekstre Dökümü Use Case — Canlı Yedeklemeli Sürüm
 */
class GetCurrentAccountsUseCase(
    private val supabaseClient: SupabaseClient,
    private val bookingRepository: BookingRepository
) {
    suspend operator fun invoke(tenantId: String, entityTypeFilter: String? = null): Result<List<CurrentAccountItem>> {
        return runCatching {
            // 1. Supabase RPC Fonksiyonunu Çağır
            val rpcResult = runCatching {
                val params = buildJsonObject {
                    put("p_tenant_id", tenantId)
                    if (entityTypeFilter != null) {
                        put("p_entity_type", entityTypeFilter)
                    }
                }
                supabaseClient.postgrest.rpc("get_current_account_statement", params)
                    .decodeList<CurrentAccountItem>()
            }.getOrDefault(emptyList())

            if (rpcResult.isNotEmpty()) {
                rpcResult
            } else {
                // 2. RPC Henüz Yüklenmediyse veya Boşsa Canlı Rezervasyonlardan Dinamik Oluştur
                val bookings = bookingRepository.getBookings(tenantId).getOrDefault(emptyList())
                if (bookings.isEmpty()) {
                    emptyList()
                } else {
                    val grouped = bookings.groupBy { it.customerName.ifBlank { "Anonim Müşteri" } }
                    grouped.map { (custName, list) ->
                        val firstBooking = list.first()
                        val isAgency = !firstBooking.agencyId.isNullOrBlank()
                        val type = if (isAgency) "agency" else "customer"
                        val code = "CAR-${if (isAgency) "AGE" else "CUST"}-${(custName.hashCode() and 0xFFFFFF).toString(16).padStart(6, '0').uppercase()}"
                        val total = list.sumOf { it.totalPrice }
                        val lastDate = list.maxOfOrNull { it.checkInDate ?: it.departureDate ?: it.createdAt.take(10) } ?: "Bugün"

                        CurrentAccountItem(
                            entityId = code,
                            accountCode = code,
                            taxNo = "11111111111",
                            entityName = custName,
                            entityType = type,
                            phone = firstBooking.customerPhone,
                            email = firstBooking.customerEmail,
                            totalDebit = total,
                            totalCredit = 0.0,
                            balance = total,
                            currency = firstBooking.currency.ifBlank { "TRY" },
                            lastTransactionDate = lastDate
                        )
                    }
                }
            }
        }
    }
}
