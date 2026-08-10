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
            val targetTenant = tenantId.takeIf { it.isValidUuid() } ?: "00000000-0000-0000-0000-000000000001"
            
            val remoteEntities = runCatching {
                supabaseClient.postgrest.from("bookings")
                    .select {
                        filter {
                            eq("tenant_id", targetTenant)
                        }
                    }
                    .decodeList<BookingEntity>()
            }.getOrDefault(emptyList())

            val combined = (remoteEntities + localCache)
                .filter { !deletedIdsBlacklist.contains(it.id) && !deletedIdsBlacklist.contains(it.bookingCode) }
                .distinctBy { if (it.id.isNotBlank()) it.id else it.bookingCode }

            if (combined.isEmpty()) {
                localCache.filter { !deletedIdsBlacklist.contains(it.id) && !deletedIdsBlacklist.contains(it.bookingCode) }
                    .map { mapEntityToDomain(it) }
            } else {
                combined.map { mapEntityToDomain(it) }
            }
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
                tenantId = validTenantId
            )

            // 1. Yerel Önbelleğe Ekle (Rezervasyon Yönetiminde Anında Görünmesi İçin)
            localCache.removeAll { it.id == validId || it.bookingCode == code }
            localCache.add(0, entity)

            // 2. Supabase Veritabanına Ekle (Fail-Safe)
            runCatching {
                supabaseClient.postgrest.from("bookings").insert(entity)
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
            paymentMethod = entity.paymentMethod,
            tenantId = entity.tenantId,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }
}
