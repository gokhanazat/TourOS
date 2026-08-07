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

class BookingRepositoryImpl(
    private val supabaseClient: SupabaseClient
) : BookingRepository {

    override suspend fun getBookings(tenantId: String): Result<List<Booking>> {
        return runCatching {
            val entities = supabaseClient.postgrest.from("bookings")
                .select {
                    filter {
                        if (tenantId.isValidUuid()) {
                            eq("tenant_id", tenantId)
                        }
                    }
                }
                .decodeList<BookingEntity>()

            entities.map { mapEntityToDomain(it) }
        }
    }

    override suspend fun getBookingById(id: String): Result<Booking> {
        return runCatching {
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
            val code = booking.bookingCode.ifBlank { "B-${booking.departureId.take(4)}-${(100..999).random()}" }
            val entity = BookingEntity(
                bookingCode = code,
                departureId = booking.departureId,
                customerId = booking.customerId,
                agencyId = booking.agencyId,
                customerName = booking.customerName,
                customerEmail = booking.customerEmail,
                customerPhone = booking.customerPhone,
                totalPrice = booking.totalPrice,
                currency = booking.currency,
                paxCount = booking.paxCount,
                status = booking.status.dbValue,
                notes = booking.notes,
                optionExpiration = booking.optionExpiration,
                tenantId = booking.tenantId
            )

            val inserted = supabaseClient.postgrest.from("bookings")
                .insert(entity) { select() }
                .decodeSingle<BookingEntity>()

            val bookingId = inserted.id

            if (booking.items.isNotEmpty()) {
                val itemEntities = booking.items.map { item ->
                    BookingItemEntity(
                        bookingId = bookingId,
                        description = item.description,
                        quantity = item.quantity,
                        unitPrice = item.unitPrice,
                        totalPrice = item.totalPrice,
                        itemType = item.itemType,
                        notes = item.notes,
                        tenantId = booking.tenantId
                    )
                }
                supabaseClient.postgrest.from("booking_items").insert(itemEntities)
            }

            if (booking.passengers.isNotEmpty()) {
                val passengerEntities = booking.passengers.map { pass ->
                    PassengerEntity(
                        bookingId = bookingId,
                        fullName = pass.fullName,
                        tcNo = pass.tcNo,
                        passportNo = pass.passportNo,
                        birthDate = pass.birthDate,
                        gender = pass.gender,
                        phone = pass.phone,
                        email = pass.email,
                        isLead = pass.isLead,
                        notes = pass.notes,
                        tenantId = booking.tenantId
                    )
                }
                supabaseClient.postgrest.from("passengers").insert(passengerEntities)
            }

            mapEntityToDomain(inserted).copy(
                items = booking.items,
                passengers = booking.passengers
            )
        }
    }

    override suspend fun updateBookingStatus(bookingId: String, status: String): Result<Unit> {
        return runCatching {
            val currentBooking = supabaseClient.postgrest.from("bookings")
                .select { filter { eq("id", bookingId) } }
                .decodeSingle<BookingEntity>()

            supabaseClient.postgrest.from("bookings")
                .update(mapOf("status" to status)) {
                    filter { eq("id", bookingId) }
                }

            val logEntity = com.mgacreative.touros.data.database.entity.BookingStatusLogEntity(
                bookingId = bookingId,
                fromStatus = currentBooking.status,
                toStatus = status,
                tenantId = currentBooking.tenantId,
                notes = "Durum $status olarak güncellendi"
            )
            runCatching {
                supabaseClient.postgrest.from("booking_status_logs").insert(logEntity)
            }
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
            tenantId = entity.tenantId,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }
}
