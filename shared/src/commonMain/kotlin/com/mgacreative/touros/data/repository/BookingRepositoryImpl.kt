package com.mgacreative.touros.data.repository

import com.mgacreative.touros.data.database.entity.BookingEntity
import com.mgacreative.touros.data.database.entity.BookingItemEntity
import com.mgacreative.touros.data.database.entity.PassengerEntity
import com.mgacreative.touros.domain.model.Booking
import com.mgacreative.touros.domain.model.BookingItem
import com.mgacreative.touros.domain.model.BookingStatus
import com.mgacreative.touros.domain.model.Passenger
import com.mgacreative.touros.domain.repository.BookingRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import com.mgacreative.touros.data.util.isValidUuid
import com.mgacreative.touros.data.util.generateUuid
import kotlinx.serialization.json.put

class BookingRepositoryImpl(
    private val supabaseClient: SupabaseClient
) : BookingRepository {

    companion object {
        private val localCache = mutableListOf<BookingEntity>()
        private val deletedIdsBlacklist = mutableSetOf<String>()
    }

    override suspend fun getBookings(tenantId: String): Result<List<Booking>> {
        return runCatching {
            val remoteEntities = runCatching {
                supabaseClient.postgrest.from("bookings")
                    .select {
                        filter {
                            if (tenantId.isValidUuid() && tenantId != "ALL") {
                                eq("tenant_id", tenantId)
                            }
                        }
                    }
                    .decodeList<BookingEntity>()
            }.getOrDefault(emptyList())

            val remoteDomain = remoteEntities.map { mapEntityToDomain(it) }
            val cachedDomain = localCache.map { mapEntityToDomain(it) }

            // Deduplicate: Yerel önbellekteki yeni kayıtları remote kayıtlarla harmanla
            val combined = (cachedDomain + remoteDomain)
                .filter { it.id !in deletedIdsBlacklist && it.bookingCode !in deletedIdsBlacklist }
                .distinctBy { if (it.id.isNotBlank()) it.id else it.bookingCode }
                .sortedByDescending { it.createdAt }

            combined
        }
    }

    override suspend fun getBookingById(id: String): Result<Booking> {
        return runCatching {
            val entityFromCache = localCache.find { it.id == id || it.bookingCode == id }
            if (entityFromCache != null) {
                return@runCatching mapEntityToDomain(entityFromCache)
            }

            val entity = supabaseClient.postgrest.from("bookings")
                .select { filter { eq("id", id) } }
                .decodeSingle<BookingEntity>()

            val items = runCatching {
                supabaseClient.postgrest.from("booking_items")
                    .select { filter { eq("booking_id", id) } }
                    .decodeList<BookingItemEntity>()
            }.getOrDefault(emptyList()).map {
                BookingItem(
                    id = it.id,
                    bookingId = it.bookingId,
                    description = it.description,
                    quantity = it.quantity,
                    unitPrice = it.unitPrice,
                    totalPrice = it.totalPrice,
                    itemType = it.itemType,
                    notes = it.notes
                )
            }

            val passengers = runCatching {
                supabaseClient.postgrest.from("passengers")
                    .select { filter { eq("booking_id", id) } }
                    .decodeList<PassengerEntity>()
            }.getOrDefault(emptyList()).map {
                Passenger(
                    id = it.id,
                    bookingId = it.bookingId,
                    fullName = it.fullName,
                    tcNo = it.tcNo,
                    passportNo = it.passportNo,
                    birthDate = it.birthDate,
                    gender = it.gender,
                    phone = it.phone,
                    email = it.email,
                    isLead = it.isLead,
                    notes = it.notes
                )
            }

            mapEntityToDomain(entity).copy(items = items, passengers = passengers)
        }
    }

    override suspend fun createBooking(booking: Booking): Result<Booking> {
        return runCatching {
            val validId = if (booking.id.isValidUuid()) booking.id else generateUuid()
            val validDepartureId = booking.departureId?.takeIf { it.isValidUuid() }
            val validTenantId = if (booking.tenantId.isValidUuid()) booking.tenantId else "00000000-0000-0000-0000-000000000001"
            val code = booking.bookingCode.ifBlank { if (booking.bookingType == "HOTEL") "HTL-${(100000..999999).random()}" else "REZ-2026-${(1000..9999).random()}" }
            val nowIso = com.mgacreative.touros.getTodayTriple().let { (d, m, y) ->
                "${y}-${m.toString().padStart(2, '0')}-${d.toString().padStart(2, '0')}T12:00:00Z"
            }
            val validCreatedAt = booking.createdAt.ifBlank { nowIso }

            val entity = BookingEntity(
                id = validId,
                bookingCode = code,
                departureId = validDepartureId,
                customerId = booking.customerId?.takeIf { it.isValidUuid() },
                agencyId = booking.agencyId?.takeIf { it.isValidUuid() },
                customerName = booking.customerName,
                customerEmail = booking.customerEmail,
                customerPhone = booking.customerPhone,
                totalPrice = booking.totalPrice,
                currency = booking.currency,
                paxCount = booking.paxCount,
                status = booking.status.dbValue,
                notes = booking.notes,
                optionExpiration = booking.optionExpiration,
                operatorName = booking.operatorName,
                productName = booking.productName,
                departureDate = booking.departureDate,
                hotelId = booking.hotelId?.takeIf { it.isValidUuid() },
                checkInDate = booking.checkInDate,
                checkOutDate = booking.checkOutDate,
                roomTypeName = booking.roomTypeName,
                nights = booking.nights,
                bookingType = booking.bookingType,
                paymentMethod = booking.paymentMethod,
                operatorPnrCode = booking.operatorPnrCode ?: code,
                operatorStatus = booking.operatorStatus ?: "ONAYLANDI",
                tenantId = validTenantId,
                createdAt = validCreatedAt
            )

            // 1. Yerel Önbelleğe Ekle (Rezervasyon Yönetiminde Anında Görünmesi İçin)
            localCache.removeAll { it.id == validId || it.bookingCode == code }
            localCache.add(0, entity)

            // 2. Supabase bookings Tablosuna Ekle (Fail-Safe)
            runCatching {
                supabaseClient.postgrest.from("bookings").insert(entity)
            }

            // 3. Supabase booking_items Tablosuna Ekle
            if (booking.items.isNotEmpty()) {
                runCatching {
                    val itemEntities = booking.items.map { itm ->
                        BookingItemEntity(
                            id = if (itm.id.isValidUuid()) itm.id else generateUuid(),
                            bookingId = validId,
                            description = itm.description,
                            quantity = itm.quantity,
                            unitPrice = itm.unitPrice,
                            totalPrice = itm.totalPrice,
                            itemType = itm.itemType,
                            notes = itm.notes
                        )
                    }
                    supabaseClient.postgrest.from("booking_items").insert(itemEntities)
                }
            }

            // 4. Supabase passengers Tablosuna Ekle
            if (booking.passengers.isNotEmpty()) {
                runCatching {
                    val paxEntities = booking.passengers.map { p ->
                        PassengerEntity(
                            id = if (p.id.isValidUuid()) p.id else generateUuid(),
                            bookingId = validId,
                            fullName = p.fullName,
                            tcNo = p.tcNo,
                            passportNo = p.passportNo,
                            birthDate = p.birthDate,
                            gender = p.gender,
                            phone = p.phone,
                            email = p.email,
                            isLead = p.isLead,
                            notes = p.notes
                        )
                    }
                    supabaseClient.postgrest.from("passengers").insert(paxEntities)
                }
            }

            mapEntityToDomain(entity).copy(
                items = booking.items,
                passengers = booking.passengers
            )
        }
    }

    override suspend fun updateBookingStatus(bookingId: String, status: String): Result<Unit> {
        return runCatching {
            // Local cache güncelleme
            val idx = localCache.indexOfFirst { it.id == bookingId || it.bookingCode == bookingId }
            if (idx != -1) {
                val old = localCache[idx]
                localCache[idx] = old.copy(status = status)
            }

            // Supabase güncelleme
            runCatching {
                val currentBooking = supabaseClient.postgrest.from("bookings")
                    .select { filter { eq("id", bookingId) } }
                    .decodeSingleOrNull<BookingEntity>()

                supabaseClient.postgrest.from("bookings")
                    .update(mapOf("status" to status)) {
                        filter { eq("id", bookingId) }
                    }

                if (currentBooking != null) {
                    val logEntity = com.mgacreative.touros.data.database.entity.BookingStatusLogEntity(
                        bookingId = bookingId,
                        fromStatus = currentBooking.status,
                        toStatus = status,
                        tenantId = currentBooking.tenantId,
                        notes = "Durum $status olarak güncellendi"
                    )
                    supabaseClient.postgrest.from("booking_status_logs").insert(logEntity)
                }
            }
        }
    }

    override suspend fun deleteBooking(bookingId: String): Result<Unit> {
        val target = bookingId.trim()
        if (target.isBlank()) return Result.success(Unit)

        return runCatching {
            // 1. Karalisteye ekle ve önbellekten çıkar
            deletedIdsBlacklist.add(target)
            localCache.removeAll { entity ->
                val isMatch = entity.id == target || entity.bookingCode == target || (entity.id.isBlank() && entity.bookingCode == target)
                if (isMatch) {
                    if (entity.id.isNotBlank()) deletedIdsBlacklist.add(entity.id)
                    if (entity.bookingCode.isNotBlank()) deletedIdsBlacklist.add(entity.bookingCode)
                }
                isMatch
            }

            // 2. Supabase RPC Atomik Silme Fonksiyonunu Çağır
            runCatching {
                val params = kotlinx.serialization.json.buildJsonObject {
                    put("p_booking_id", kotlinx.serialization.json.JsonPrimitive(target))
                }
                supabaseClient.postgrest.rpc("delete_booking_by_id", params)
            }

            // 3. Supabase Doğrudan DELETE Fallback (id ve booking_code)
            runCatching {
                supabaseClient.postgrest.from("bookings").delete {
                    filter {
                        eq("id", target)
                    }
                }
            }
            runCatching {
                supabaseClient.postgrest.from("bookings").delete {
                    filter {
                        eq("booking_code", target)
                    }
                }
            }
            Unit
        }
    }

    override suspend fun getBookingStatusLogs(bookingId: String): Result<List<com.mgacreative.touros.domain.model.BookingStatusLog>> {
        return runCatching {
            val entities = supabaseClient.postgrest.from("booking_status_logs")
                .select { filter { eq("booking_id", bookingId) } }
                .decodeList<com.mgacreative.touros.data.database.entity.BookingStatusLogEntity>()

            entities.map { entity ->
                com.mgacreative.touros.domain.model.BookingStatusLog(
                    id = entity.id,
                    bookingId = entity.bookingId,
                    fromStatus = entity.fromStatus,
                    toStatus = entity.toStatus,
                    changedBy = entity.changedBy,
                    notes = entity.notes,
                    createdAt = entity.createdAt
                )
            }
        }
    }

    private fun mapEntityToDomain(entity: BookingEntity): Booking {
        val isHotel = entity.bookingType == "HOTEL" || !entity.hotelId.isNullOrBlank()
        val defaultProductName = if (isHotel) "Otel Konaklama" else "Kapadokya Turu"
        val defaultDate = if (isHotel) (entity.checkInDate ?: "2026-09-01") else "2026-09-01"

        return Booking(
            id = entity.id,
            bookingCode = entity.bookingCode,
            departureId = entity.departureId,
            customerId = entity.customerId,
            agencyId = entity.agencyId,
            customerName = entity.customerName,
            customerEmail = entity.customerEmail,
            customerPhone = entity.customerPhone,
            totalPrice = entity.totalPrice,
            currency = entity.currency,
            paxCount = entity.paxCount,
            status = BookingStatus.fromDbValue(entity.status),
            notes = entity.notes,
            optionExpiration = entity.optionExpiration,
            confirmedAt = entity.confirmedAt,
            cancelledAt = entity.cancelledAt,
            operatorName = entity.operatorName ?: "MGA Creative",
            productName = entity.productName ?: defaultProductName,
            departureDate = entity.departureDate ?: defaultDate,
            hotelId = entity.hotelId,
            checkInDate = entity.checkInDate,
            checkOutDate = entity.checkOutDate,
            roomTypeName = entity.roomTypeName,
            nights = entity.nights,
            bookingType = entity.bookingType ?: (if (isHotel) "HOTEL" else "TOUR"),
            operatorPnrCode = entity.operatorPnrCode,
            operatorStatus = entity.operatorStatus,
            paymentMethod = entity.paymentMethod,
            tenantId = entity.tenantId,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    override suspend fun updateOperatorPnr(bookingId: String, pnrCode: String, operatorStatus: String): Result<Unit> {
        return runCatching {
            val targetId = bookingId
            val idx = localCache.indexOfFirst { it.id == targetId || it.bookingCode == targetId }
            if (idx != -1) {
                val old = localCache[idx]
                localCache[idx] = old.copy(
                    operatorPnrCode = pnrCode,
                    operatorStatus = operatorStatus,
                    status = "ONAYLANDI"
                )
            }

            runCatching {
                val currentBooking = supabaseClient.postgrest.from("bookings")
                    .select { filter { eq("id", targetId) } }
                    .decodeSingleOrNull<BookingEntity>()

                supabaseClient.postgrest.from("bookings")
                    .update(mapOf(
                        "operator_pnr_code" to pnrCode,
                        "operator_status" to operatorStatus,
                        "status" to "ONAYLANDI"
                    )) {
                        filter { eq("id", targetId) }
                    }

                val netCost = currentBooking?.netCost ?: 0.0
                val operatorName = currentBooking?.operatorName ?: "Tur Operatörü"
                val currency = currentBooking?.currency ?: "TRY"
                val tenantId = currentBooking?.tenantId?.ifBlank { "00000000-0000-0000-0000-000000000001" } ?: "00000000-0000-0000-0000-000000000001"

                val transactionEntity = mapOf(
                    "booking_id" to targetId,
                    "operator_pnr_code" to pnrCode,
                    "transaction_type" to "CREDIT",
                    "amount" to netCost,
                    "currency" to currency,
                    "description" to "TO PNR: $pnrCode - $operatorName Onaylı Tur Maliyeti",
                    "tenant_id" to tenantId
                )
                runCatching {
                    supabaseClient.postgrest.from("current_account_transactions").insert(transactionEntity)
                }
            }
        }
    }
}
