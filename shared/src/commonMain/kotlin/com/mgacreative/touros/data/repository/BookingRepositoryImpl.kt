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
import kotlinx.serialization.json.buildJsonObject
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
                            if (tenantId.isValidUuid() && tenantId != "ALL" && tenantId != "00000000-0000-0000-0000-000000000001") {
                                eq("tenant_id", tenantId)
                            }
                        }
                    }
                    .decodeList<BookingEntity>()
            }.onFailure { err ->
                println("⚠️ Supabase getBookings hatası: ${err.message}")
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
            val cleanId = id.trim()
            val entityFromCache = localCache.find { 
                it.id == cleanId || it.bookingCode == cleanId || it.operatorPnrCode == cleanId 
            }
            if (entityFromCache != null) {
                return@runCatching mapEntityToDomain(entityFromCache)
            }

            val entity = if (cleanId.isValidUuid()) {
                supabaseClient.postgrest.from("bookings")
                    .select { filter { eq("id", cleanId) } }
                    .decodeSingle<BookingEntity>()
            } else {
                val list = supabaseClient.postgrest.from("bookings")
                    .select {
                        filter {
                            or {
                                eq("booking_code", cleanId)
                                eq("operator_pnr_code", cleanId)
                            }
                        }
                    }
                    .decodeList<BookingEntity>()
                list.firstOrNull() ?: throw NoSuchElementException("Rezervasyon bulunamadı: $cleanId")
            }

            val bookingRealId = entity.id ?: cleanId

            val items = runCatching {
                supabaseClient.postgrest.from("booking_items")
                    .select { filter { eq("booking_id", bookingRealId) } }
                    .decodeList<BookingItemEntity>()
            }.getOrDefault(emptyList()).map {
                BookingItem(
                    id = it.id ?: generateUuid(),
                    bookingId = it.bookingId ?: bookingRealId,
                    description = it.description ?: "Hizmet",
                    quantity = it.quantity ?: 1,
                    unitPrice = it.unitPrice ?: 0.0,
                    totalPrice = it.totalPrice ?: 0.0,
                    itemType = it.itemType ?: "TOUR",
                    notes = it.notes
                )
            }

            val passengers = runCatching {
                supabaseClient.postgrest.from("passengers")
                    .select { filter { eq("booking_id", bookingRealId) } }
                    .decodeList<PassengerEntity>()
            }.getOrDefault(emptyList()).map {
                Passenger(
                    id = it.id ?: generateUuid(),
                    bookingId = it.bookingId ?: bookingRealId,
                    fullName = it.fullName ?: "Yolcu",
                    tcNo = it.tcNo,
                    passportNo = it.passportNo,
                    birthDate = it.birthDate,
                    gender = it.gender,
                    phone = it.phone,
                    email = it.email,
                    isLead = it.isLead ?: false,
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
                bookingNumber = code,
                departureId = validDepartureId,
                customerId = booking.customerId?.takeIf { it.isValidUuid() },
                agencyId = booking.agencyId?.takeIf { it.isValidUuid() },
                customerName = booking.customerName,
                customerEmail = booking.customerEmail,
                customerPhone = booking.customerPhone,
                totalPrice = booking.totalPrice,
                totalAmount = booking.totalPrice,
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
                operatorPnrCode = booking.operatorPnrCode?.takeIf { it.isNotBlank() },
                operatorStatus = booking.operatorStatus ?: "BEKLİYOR",
                tenantId = validTenantId,
                createdAt = validCreatedAt
            )

            // 1. Yerel Önbelleğe Ekle (Rezervasyon Yönetiminde Anında Görünmesi İçin)
            localCache.removeAll { it.id == validId || it.bookingCode == code }
            localCache.add(0, entity)

            // 2. Supabase bookings Tablosuna Temiz Payload İle Ekle (Kalıcı Saklama)
            runCatching {
                val bookingPayload = buildJsonObject {
                    put("id", validId)
                    put("booking_code", code)
                    put("booking_number", code)
                    if (validDepartureId != null) put("departure_id", validDepartureId)
                    if (booking.customerId?.isValidUuid() == true) put("customer_id", booking.customerId)
                    if (booking.agencyId?.isValidUuid() == true) put("agency_id", booking.agencyId)
                    put("customer_name", booking.customerName)
                    if (!booking.customerEmail.isNullOrBlank()) put("customer_email", booking.customerEmail)
                    if (!booking.customerPhone.isNullOrBlank()) put("customer_phone", booking.customerPhone)
                    put("total_price", booking.totalPrice)
                    put("total_amount", booking.totalPrice)
                    put("currency", booking.currency)
                    put("pax_count", booking.paxCount)
                    put("status", booking.status.dbValue)
                    if (!booking.notes.isNullOrBlank()) put("notes", booking.notes)
                    if (!booking.optionExpiration.isNullOrBlank()) put("option_expiration", booking.optionExpiration)
                    put("operator_name", booking.operatorName ?: "MGA Creative")
                    put("product_name", booking.productName ?: "Tur Rezervasyonu")
                    put("departure_date", booking.departureDate ?: "2026-09-01")
                    if (booking.hotelId?.isValidUuid() == true) put("hotel_id", booking.hotelId)
                    if (!booking.checkInDate.isNullOrBlank()) put("check_in_date", booking.checkInDate)
                    if (!booking.checkOutDate.isNullOrBlank()) put("check_out_date", booking.checkOutDate)
                    if (!booking.roomTypeName.isNullOrBlank()) put("room_type_name", booking.roomTypeName)
                    put("nights", booking.nights)
                    put("booking_type", booking.bookingType ?: "TOUR")
                    put("payment_method", booking.paymentMethod ?: "CREDIT_CARD")
                    if (!booking.operatorPnrCode.isNullOrBlank()) put("operator_pnr_code", booking.operatorPnrCode)
                    put("operator_status", booking.operatorStatus ?: "BEKLİYOR")
                    put("tenant_id", validTenantId)
                }
                supabaseClient.postgrest.from("bookings").insert(bookingPayload)
                println("✅ Supabase bookings kaydı başarıyla oluşturuldu: ID=$validId, Kod=$code")
            }.onFailure { err ->
                println("❌ Supabase bookings insert hatası: ${err.message}")
            }

            // 3. Supabase booking_items Tablosuna Ekle
            if (booking.items.isNotEmpty()) {
                runCatching {
                    booking.items.forEach { itm ->
                        val itemPayload = buildJsonObject {
                            put("id", if (itm.id.isValidUuid()) itm.id else generateUuid())
                            put("booking_id", validId)
                            put("description", itm.description)
                            put("quantity", itm.quantity)
                            put("unit_price", itm.unitPrice)
                            put("total_price", itm.totalPrice)
                            put("item_type", itm.itemType)
                            if (!itm.notes.isNullOrBlank()) put("notes", itm.notes)
                            put("tenant_id", validTenantId)
                        }
                        supabaseClient.postgrest.from("booking_items").insert(itemPayload)
                    }
                }.onFailure { err ->
                    println("❌ Supabase booking_items insert hatası: ${err.message}")
                }
            }

            // 4. Supabase passengers Tablosuna Ekle
            if (booking.passengers.isNotEmpty()) {
                runCatching {
                    booking.passengers.forEach { p ->
                        val paxPayload = buildJsonObject {
                            put("id", if (p.id.isValidUuid()) p.id else generateUuid())
                            put("booking_id", validId)
                            put("full_name", p.fullName)
                            if (!p.tcNo.isNullOrBlank()) put("tc_no", p.tcNo)
                            if (!p.passportNo.isNullOrBlank()) put("passport_no", p.passportNo)
                            if (!p.birthDate.isNullOrBlank()) put("birth_date", p.birthDate)
                            if (!p.gender.isNullOrBlank()) put("gender", p.gender)
                            if (!p.phone.isNullOrBlank()) put("phone", p.phone)
                            if (!p.email.isNullOrBlank()) put("email", p.email)
                            put("is_lead", p.isLead)
                            if (!p.notes.isNullOrBlank()) put("notes", p.notes)
                            put("tenant_id", validTenantId)
                        }
                        supabaseClient.postgrest.from("passengers").insert(paxPayload)
                    }
                }.onFailure { err ->
                    println("❌ Supabase passengers insert hatası: ${err.message}")
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
                        fromStatus = currentBooking.status ?: "Bekliyor",
                        toStatus = status,
                        tenantId = currentBooking.tenantId ?: "00000000-0000-0000-0000-000000000001",
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
                val isMatch = entity.id == target || entity.bookingCode == target || (entity.id != null && target == entity.id) || (entity.bookingCode != null && target == entity.bookingCode)
                if (isMatch) {
                    if (!entity.id.isNullOrBlank()) deletedIdsBlacklist.add(entity.id)
                    if (!entity.bookingCode.isNullOrBlank()) deletedIdsBlacklist.add(entity.bookingCode)
                }
                isMatch
            }

            // 2. Supabase silme - Önce bağlı alt kayıtları temizle (Foreign key hatasını önle)
            runCatching {
                if (target.isValidUuid()) {
                    supabaseClient.postgrest.from("booking_items").delete { filter { eq("booking_id", target) } }
                    supabaseClient.postgrest.from("passengers").delete { filter { eq("booking_id", target) } }
                    supabaseClient.postgrest.from("booking_status_logs").delete { filter { eq("booking_id", target) } }
                    supabaseClient.postgrest.from("bookings").delete { filter { eq("id", target) } }
                } else {
                    val matching = supabaseClient.postgrest.from("bookings").select { filter { eq("booking_code", target) } }.decodeList<BookingEntity>()
                    matching.forEach { b ->
                        if (!b.id.isNullOrBlank()) deletedIdsBlacklist.add(b.id)
                        if (!b.bookingCode.isNullOrBlank()) deletedIdsBlacklist.add(b.bookingCode)
                        val bId = b.id
                        if (!bId.isNullOrBlank()) {
                            supabaseClient.postgrest.from("booking_items").delete { filter { eq("booking_id", bId) } }
                            supabaseClient.postgrest.from("passengers").delete { filter { eq("booking_id", bId) } }
                            supabaseClient.postgrest.from("booking_status_logs").delete { filter { eq("booking_id", bId) } }
                            supabaseClient.postgrest.from("bookings").delete { filter { eq("id", bId) } }
                        }
                    }
                    supabaseClient.postgrest.from("bookings").delete { filter { eq("booking_code", target) } }
                }
            }
            Unit
        }
    }

    override suspend fun getBookingStatusLogs(bookingId: String): Result<List<com.mgacreative.touros.domain.model.BookingStatusLog>> {
        return runCatching {
            val entities = supabaseClient.postgrest.from("booking_status_logs")
                .select {
                    filter {
                        eq("booking_id", bookingId)
                    }
                }
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
        val code = entity.bookingCode.ifBlank {
            entity.bookingNumber?.takeIf { it.isNotBlank() } ?: "REZ-${entity.id.take(8)}"
        }
        val price = if (entity.totalPrice > 0.0) entity.totalPrice else (entity.totalAmount ?: 0.0)
        val isHotel = entity.bookingType == "HOTEL" || !entity.hotelId.isNullOrBlank()
        val defaultProductName = if (isHotel) "Otel Konaklama" else "Kapadokya Turu"
        val defaultDate = if (isHotel) (entity.checkInDate ?: "2026-09-01") else (entity.departureDate ?: "2026-09-01")

        return Booking(
            id = entity.id,
            bookingCode = code,
            departureId = entity.departureId,
            customerId = entity.customerId,
            agencyId = entity.agencyId,
            customerName = entity.customerName.ifBlank { "Misafir" },
            customerEmail = entity.customerEmail,
            customerPhone = entity.customerPhone,
            totalPrice = price,
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
            val targetId = bookingId.trim()
            val cleanPnr = pnrCode.trim().uppercase()

            // 1. Önbellekteki kaydı bul ve güncelle
            val cachedIdx = localCache.indexOfFirst { 
                it.id == targetId || it.bookingCode == targetId || it.operatorPnrCode == targetId 
            }
            var foundRealId = if (targetId.isValidUuid()) targetId else ""

            if (cachedIdx != -1) {
                val old = localCache[cachedIdx]
                if (old.id.isValidUuid()) foundRealId = old.id
                localCache[cachedIdx] = old.copy(
                    operatorPnrCode = cleanPnr,
                    operatorStatus = operatorStatus,
                    status = BookingStatus.ONAYLANDI.dbValue
                )
            }

            // 2. Supabase güncellemesi (UUID veya booking_code ile güvenli eşleşme)
            runCatching {
                if (foundRealId.isValidUuid()) {
                    supabaseClient.postgrest.from("bookings")
                        .update(mapOf(
                            "operator_pnr_code" to cleanPnr,
                            "operator_status" to operatorStatus,
                            "status" to BookingStatus.ONAYLANDI.dbValue
                        )) {
                            filter { eq("id", foundRealId) }
                        }
                } else {
                    supabaseClient.postgrest.from("bookings")
                        .update(mapOf(
                            "operator_pnr_code" to cleanPnr,
                            "operator_status" to operatorStatus,
                            "status" to BookingStatus.ONAYLANDI.dbValue
                        )) {
                            filter { eq("booking_code", targetId) }
                        }
                }

                val currentBooking = localCache.find { it.id == targetId || it.bookingCode == targetId || it.id == foundRealId }
                val netCost = currentBooking?.totalPrice ?: 0.0
                val operatorName = currentBooking?.operatorName ?: "Tur Operatörü"
                val currency = currentBooking?.currency ?: "TRY"
                val tenantId = currentBooking?.tenantId?.ifBlank { "00000000-0000-0000-0000-000000000001" } ?: "00000000-0000-0000-0000-000000000001"

                val transactionEntity = mapOf(
                    "booking_id" to (if (foundRealId.isValidUuid()) foundRealId else generateUuid()),
                    "operator_pnr_code" to cleanPnr,
                    "transaction_type" to "CREDIT",
                    "amount" to netCost,
                    "currency" to currency,
                    "description" to "TO PNR: $cleanPnr - $operatorName Onaylı Tur Maliyeti",
                    "tenant_id" to tenantId
                )
                runCatching {
                    supabaseClient.postgrest.from("current_account_transactions").insert(transactionEntity)
                }
            }
        }
    }
}
