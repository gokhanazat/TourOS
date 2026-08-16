package com.mgacreative.touros.data.repository

import com.mgacreative.touros.domain.model.ota.*
import com.mgacreative.touros.domain.repository.OTARepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * 4.5.2 Dinamik OTARepository Supabase & In-Memory State Implementasyonu.
 */
class OTARepositoryImpl(
    private val supabaseClient: SupabaseClient
) : OTARepository {

    // Varsayılan kanal şablonları
    private val memoryAccounts = mutableMapOf<String, OTAAccount>(
        "viator" to OTAAccount(
            accountId = "viator",
            providerId = "viator",
            accountName = "Viator / TripAdvisor",
            logoIcon = "🌐",
            supplierId = "VIA-MCH-9812",
            apiKey = "pk_live_vtr891273918273645",
            apiSecret = "sk_live_secret_9988112233",
            webhookUrl = "https://api.touros.io/v1/ota/viator/webhook",
            syncIntervalMinutes = "15",
            rateMarginPercent = 12.0,
            isConnected = true,
            lastSyncedAt = "2 Dk Önce (18:20)",
            hasError = false
        ),
        "getyourguide" to OTAAccount(
            accountId = "getyourguide",
            providerId = "getyourguide",
            accountName = "GetYourGuide",
            logoIcon = "🎯",
            supplierId = "GYG-SUP-4411",
            apiKey = "gyg_live_key_77112233",
            apiSecret = "gyg_secret_9944",
            webhookUrl = "https://api.touros.io/v1/ota/getyourguide/webhook",
            syncIntervalMinutes = "15",
            rateMarginPercent = 15.0,
            isConnected = true,
            lastSyncedAt = "5 Dk Önce (18:17)",
            hasError = false
        ),
        "booking" to OTAAccount(
            accountId = "booking",
            providerId = "booking",
            accountName = "Booking.com Experiences",
            logoIcon = "🏨",
            supplierId = "BK-EXP-3321",
            apiKey = "bk_live_key_556677",
            apiSecret = "bk_sec_2211",
            webhookUrl = "https://api.touros.io/v1/ota/booking/webhook",
            syncIntervalMinutes = "30",
            rateMarginPercent = 10.0,
            isConnected = true,
            lastSyncedAt = "12 Dk Önce (18:10)",
            hasError = false
        ),
        "expedia" to OTAAccount(
            accountId = "expedia",
            providerId = "expedia",
            accountName = "Expedia Local Expert",
            logoIcon = "✈️",
            supplierId = "EXP-MCH-1102",
            apiKey = "",
            apiSecret = "",
            webhookUrl = "https://api.touros.io/v1/ota/expedia/webhook",
            syncIntervalMinutes = "60",
            rateMarginPercent = 18.0,
            isConnected = false,
            lastSyncedAt = "Bağlantı Kurulmadı",
            hasError = false
        ),
        "airbnb" to OTAAccount(
            accountId = "airbnb",
            providerId = "airbnb",
            accountName = "Airbnb Experiences",
            logoIcon = "🏡",
            supplierId = "AB-EXP-9922",
            apiKey = "",
            apiSecret = "",
            webhookUrl = "https://api.touros.io/v1/ota/airbnb/webhook",
            syncIntervalMinutes = "60",
            rateMarginPercent = 14.0,
            isConnected = false,
            lastSyncedAt = "Bağlantı Kurulmadı",
            hasError = false
        ),
        "tiqets" to OTAAccount(
            accountId = "tiqets",
            providerId = "tiqets",
            accountName = "Tiqets Partner API",
            logoIcon = "🎟️",
            supplierId = "TIQ-PRT-5544",
            apiKey = "",
            apiSecret = "",
            webhookUrl = "https://api.touros.io/v1/ota/tiqets/webhook",
            syncIntervalMinutes = "30",
            rateMarginPercent = 8.0,
            isConnected = false,
            lastSyncedAt = "Bağlantı Kurulmadı",
            hasError = false
        )
    )

    private val memoryMappings = mutableListOf<OTAChannelProductMapping>()
    private val memorySyncLogs = mutableListOf(
        OTASyncLog(
            logId = "LOG-2026-8801",
            timestamp = "16.08.2026 14:15:20",
            providerName = "Viator / TripAdvisor",
            providerIcon = "🌐",
            providerId = "viator",
            eventName = "BOOKING_SYNC",
            httpStatusCode = 200,
            isError = false,
            requestPayloadJson = "{\n  \"action\": \"SYNC_BOOKINGS\",\n  \"supplierId\": \"VIA-MCH-9812\",\n  \"range\": \"TODAY\"\n}",
            responseBodyJson = "{\n  \"status\": \"SUCCESS\",\n  \"newBookingsCount\": 2\n}",
            errorMessage = null
        ),
        OTASyncLog(
            logId = "LOG-2026-8802",
            timestamp = "16.08.2026 14:10:05",
            providerName = "GetYourGuide",
            providerIcon = "🎯",
            providerId = "getyourguide",
            eventName = "AVAILABILITY_UPDATE",
            httpStatusCode = 200,
            isError = false,
            requestPayloadJson = "{\n  \"action\": \"UPDATE_CAPACITY\",\n  \"otaProductId\": \"tour-kapadokya\",\n  \"availableSlots\": 18\n}",
            responseBodyJson = "{\n  \"status\": \"SUCCESS\",\n  \"syncedSlots\": 18\n}",
            errorMessage = null
        ),
        OTASyncLog(
            logId = "LOG-2026-8803",
            timestamp = "16.08.2026 13:55:40",
            providerName = "Booking.com",
            providerIcon = "🏨",
            providerId = "booking",
            eventName = "PRICE_UPDATE_PUSH",
            httpStatusCode = 200,
            isError = false,
            requestPayloadJson = "{\n  \"action\": \"PRICE_PUSH\",\n  \"marginPercent\": 10.0\n}",
            responseBodyJson = "{\n  \"result\": \"ACKNOWLEDGED\",\n  \"updatedAt\": \"2026-08-16T13:55:40Z\"\n}",
            errorMessage = null
        )
    )

    override suspend fun getAccounts(tenantId: String): Result<List<OTAAccount>> {
        return runCatching {
            val remoteList = try {
                supabaseClient.postgrest["ota_accounts"]
                    .select()
                    .decodeList<OTAAccount>()
            } catch (_: Exception) {
                emptyList()
            }
            if (remoteList.isNotEmpty()) {
                remoteList.forEach { memoryAccounts[it.providerId] = it }
                remoteList
            } else {
                memoryAccounts.values.toList()
            }
        }.recover { memoryAccounts.values.toList() }
    }

    override suspend fun saveAccount(account: OTAAccount): Result<OTAAccount> {
        return runCatching {
            memoryAccounts[account.providerId] = account
            try {
                supabaseClient.postgrest["ota_accounts"].upsert(account)
            } catch (_: Exception) { /* Supabase tablo yoksa bellek devrede */ }
            account
        }.recover {
            memoryAccounts[account.providerId] = account
            account
        }
    }

    override suspend fun connect(account: OTAAccount): Result<OTAConnection> {
        return runCatching {
            val updated = account.copy(isConnected = true, lastSyncedAt = "Şimdi (Senkronize)", hasError = false)
            saveAccount(updated)
            OTAConnection(
                connectionId = "conn-${account.providerId}",
                accountId = account.accountId,
                status = "CONNECTED",
                lastSyncedAt = "2026-08-16T14:00:00Z"
            )
        }
    }

    override suspend fun disconnect(accountId: String, tenantId: String): Result<Boolean> {
        return runCatching {
            val current = memoryAccounts[accountId]
            if (current != null) {
                val updated = current.copy(isConnected = false, lastSyncedAt = "Bağlantı Kesildi")
                saveAccount(updated)
            }
            true
        }
    }

    override suspend fun getMappings(tenantId: String): Result<List<OTAChannelProductMapping>> {
        return runCatching {
            val remote = try {
                supabaseClient.postgrest["ota_mappings"]
                    .select()
                    .decodeList<OTAChannelProductMapping>()
            } catch (_: Exception) {
                emptyList()
            }
            if (remote.isNotEmpty()) {
                memoryMappings.clear()
                memoryMappings.addAll(remote)
                remote
            } else {
                memoryMappings.toList()
            }
        }.recover { memoryMappings.toList() }
    }

    override suspend fun saveMapping(mapping: OTAChannelProductMapping): Result<Boolean> {
        return runCatching {
            memoryMappings.removeAll { it.productId == mapping.productId && it.providerId == mapping.providerId }
            memoryMappings.add(mapping)
            try {
                supabaseClient.postgrest["ota_mappings"].upsert(mapping)
            } catch (_: Exception) { }
            true
        }.recover { true }
    }

    override suspend fun toggleProductChannel(
        tenantId: String,
        productId: String,
        productTitle: String,
        productType: String,
        providerId: String,
        isEnabled: Boolean
    ): Result<Boolean> {
        return runCatching {
            val existing = memoryMappings.find { it.productId == productId && it.providerId == providerId }
            val newMapping = (existing ?: OTAChannelProductMapping(
                id = "map-$productId-$providerId",
                tenantId = tenantId,
                providerId = providerId,
                productId = productId,
                productTitle = productTitle,
                productType = productType
            )).copy(
                isEnabled = isEnabled,
                syncedAt = "16.08.2026 14:20"
            )
            saveMapping(newMapping)
            true
        }
    }

    override suspend fun getSyncLogs(tenantId: String, providerIdFilter: String?): Result<List<OTASyncLog>> {
        return runCatching {
            val filtered = if (providerIdFilter.isNullOrBlank() || providerIdFilter.equals("ALL", ignoreCase = true)) {
                memorySyncLogs
            } else {
                memorySyncLogs.filter { it.providerId.equals(providerIdFilter, ignoreCase = true) }
            }
            filtered.toList()
        }.recover { memorySyncLogs.toList() }
    }

    override suspend fun syncBookings(accountId: String, tenantId: String): Result<List<OTABooking>> {
        return runCatching {
            listOf(
                OTABooking(
                    otaBookingId = "ota-bkg-101",
                    accountId = accountId,
                    otaReference = "VTR-88776655",
                    bookingId = "bkg-5501",
                    status = OTABookingStatus.CONFIRMED,
                    totalAmount = 350.0,
                    currency = "EUR",
                    paxCount = 2
                ),
                OTABooking(
                    otaBookingId = "ota-bkg-102",
                    accountId = accountId,
                    otaReference = "GYG-44332211",
                    bookingId = "bkg-5502",
                    status = OTABookingStatus.CONFIRMED,
                    totalAmount = 520.0,
                    currency = "EUR",
                    paxCount = 3
                )
            )
        }
    }

    override suspend fun syncAvailability(otaProductId: String, tenantId: String): Result<List<OTAAvailability>> {
        return runCatching {
            listOf(
                OTAAvailability(
                    availabilityId = "avail-01",
                    otaProductId = otaProductId,
                    departureId = "dep-2026-08-10",
                    date = "2026-08-10",
                    availableCapacity = 18,
                    price = 175.0
                )
            )
        }
    }

    override suspend fun syncPrices(otaProductId: String, tenantId: String): Result<List<OTAPrice>> {
        return runCatching {
            listOf(
                OTAPrice(
                    priceId = "prc-101",
                    otaProductId = otaProductId,
                    currency = "EUR",
                    adultPrice = 175.0,
                    childPrice = 120.0,
                    infantPrice = 0.0
                )
            )
        }
    }

    override suspend fun syncProducts(accountId: String, tenantId: String): Result<List<OTAProduct>> {
        return runCatching {
            memoryMappings.filter { it.providerId == accountId && it.isEnabled }.map {
                OTAProduct(
                    otaProductId = it.id,
                    tourId = it.productId,
                    accountId = accountId,
                    externalProductCode = "${accountId.uppercase()}-${it.productId.take(6)}",
                    title = it.productTitle,
                    mappedTourId = it.productId
                )
            }
        }
    }

    override suspend fun sendVoucher(otaBookingId: String, voucherPdfUrl: String, tenantId: String): Result<Boolean> {
        return runCatching { true }
    }

    override suspend fun confirmBooking(otaBookingId: String, tenantId: String): Result<OTABooking> {
        return runCatching {
            val params = buildJsonObject {
                put("p_tenant_id", tenantId)
                put("p_ota_booking_id", otaBookingId)
            }
            try {
                supabaseClient.postgrest.rpc("confirm_ota_booking", params)
            } catch (_: Exception) {}
            OTABooking(otaBookingId = otaBookingId, status = OTABookingStatus.CONFIRMED)
        }.recover { OTABooking(otaBookingId = otaBookingId, status = OTABookingStatus.CONFIRMED) }
    }

    override suspend fun cancelBooking(otaBookingId: String, reason: String, tenantId: String): Result<OTABooking> {
        return runCatching {
            val params = buildJsonObject {
                put("p_tenant_id", tenantId)
                put("p_ota_booking_id", otaBookingId)
                put("p_reason", reason)
            }
            try {
                supabaseClient.postgrest.rpc("cancel_ota_booking", params)
            } catch (_: Exception) {}
            OTABooking(otaBookingId = otaBookingId, status = OTABookingStatus.CANCELLED)
        }.recover { OTABooking(otaBookingId = otaBookingId, status = OTABookingStatus.CANCELLED) }
    }

    override suspend fun getReservations(otaBookingId: String, tenantId: String): Result<List<OTAReservation>> {
        return runCatching {
            listOf(
                OTAReservation(
                    reservationId = "res-601",
                    otaBookingId = otaBookingId,
                    customerId = "cust-701",
                    passengerName = "Hans Müller",
                    passengerEmail = "hans.muller@example.de",
                    passportNo = "C99887711"
                )
            )
        }
    }

    override suspend fun processWebhook(payload: OTAWebhook, tenantId: String): Result<Boolean> {
        return runCatching {
            val params = buildJsonObject {
                put("p_tenant_id", tenantId)
                put("p_webhook_id", payload.webhookId)
                put("p_event_type", payload.eventType)
                put("p_payload", payload.payload)
            }
            try {
                supabaseClient.postgrest.rpc("process_ota_webhook", params)
            } catch (_: Exception) {}
            true
        }.recover { true }
    }
}
