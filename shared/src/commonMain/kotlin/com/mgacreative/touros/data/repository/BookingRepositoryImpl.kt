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

        private fun formatToSqlDate(input: String?): String? {
            if (input.isNullOrBlank()) return null
            val trimmed = input.trim()
            if (trimmed.matches(Regex("""^\d{4}-\d{2}-\d{2}$"""))) return trimmed
            val partsDot = trimmed.split(".")
            if (partsDot.size == 3 && partsDot[2].length == 4) {
                return "${partsDot[2]}-${partsDot[1].padStart(2, '0')}-${partsDot[0].padStart(2, '0')}"
            }
            val partsSlash = trimmed.split("/")
            if (partsSlash.size == 3 && partsSlash[2].length == 4) {
                return "${partsSlash[2]}-${partsSlash[1].padStart(2, '0')}-${partsSlash[0].padStart(2, '0')}"
            }
            return null
        }
    }

    override suspend fun getBookings(tenantId: String): Result<List<Booking>> {
        return runCatching {
            val remoteEntities = runCatching {
                supabaseClient.postgrest.from("bookings")
                    .select {
                        filter {
                            if (tenantId.isValidUuid() && tenantId != "ALL" && tenantId != "00000000-0000-0000-0000-000000000001") {
                                or {
                                    eq("tenant_id", tenantId)
                                    eq("tenant_id", "00000000-0000-0000-0000-000000000001")
                                }
                            }
                        }
                    }
                    .decodeList<BookingEntity>()
            }.onFailure { err ->
                println("⚠️ Supabase getBookings hatası: ${err.message}")
                err.printStackTrace()
            }.getOrDefault(emptyList())

            if (remoteEntities.isNotEmpty()) {
                localCache.clear()
                localCache.addAll(remoteEntities)
            }

            val listToDisplay = if (remoteEntities.isNotEmpty()) {
                remoteEntities.map { mapEntityToDomain(it) }
            } else {
                localCache.map { mapEntityToDomain(it) }
            }

            val filtered = listToDisplay
                .filter { it.id !in deletedIdsBlacklist && it.bookingCode !in deletedIdsBlacklist }
                .distinctBy { if (it.id.isNotBlank()) it.id else it.bookingCode }
                .sortedByDescending { it.createdAt }

            filtered
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
                                eq("booking_number", cleanId)
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
                    description = it.safeDescription,
                    quantity = it.safeQuantity,
                    unitPrice = it.safeUnitPrice,
                    totalPrice = it.safeTotalPrice,
                    itemType = it.safeItemType,
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
                    fullName = it.safeFullName,
                    tcNo = it.safeTcNo,
                    passportNo = it.safePassportNo,
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
                customerName = booking.customerName.ifBlank { "Misafir" },
                customerEmail = booking.customerEmail,
                customerPhone = booking.customerPhone,
                totalPrice = booking.totalPrice,
                totalAmount = booking.totalPrice,
                currency = booking.currency.ifBlank { "TRY" },
                paxCount = booking.paxCount,
                status = booking.status.dbValue,
                notes = booking.notes,
                optionExpiration = booking.optionExpiration,
                operatorName = booking.operatorName ?: "MGA Creative",
                productName = booking.productName ?: "Tur Rezervasyonu",
                departureDate = booking.departureDate ?: "2026-09-01",
                hotelId = booking.hotelId?.takeIf { it.isValidUuid() },
                checkInDate = booking.checkInDate,
                checkOutDate = booking.checkOutDate,
                roomTypeName = booking.roomTypeName,
                nights = booking.nights,
                bookingType = booking.bookingType ?: "TOUR",
                paymentMethod = booking.paymentMethod ?: "CREDIT_CARD",
                operatorPnrCode = booking.operatorPnrCode?.takeIf { it.isNotBlank() },
                operatorStatus = booking.operatorStatus ?: "BEKLİYOR",
                tenantId = validTenantId,
                createdAt = validCreatedAt,
                updatedAt = validCreatedAt
            )

            // 2. Supabase bookings — Orijinal Tablo Şemasıyla %100 Uyumlu Güvenli Insert
            // Ekstra ürün ve operatör detayları notes alanı içerisinde zenginleştirilerek saklanır
            val richNotes = buildString {
                booking.productName?.takeIf { it.isNotBlank() }?.let { append("Ürün: $it • ") }
                booking.operatorName?.takeIf { it.isNotBlank() }?.let { append("Operatör: $it • ") }
                booking.departureDate?.takeIf { it.isNotBlank() }?.let { append("Tarih: $it • ") }
                booking.roomTypeName?.takeIf { it.isNotBlank() }?.let { append("Oda: $it • ") }
                if (booking.nights > 0) append("Gece: ${booking.nights} • ")
                booking.notes?.takeIf { it.isNotBlank() }?.let { append(it) }
            }.trim().removeSuffix("•").trim()

            val bookingJson = buildJsonObject {
                put("id", validId)
                put("booking_code", code)
                if (validDepartureId != null) put("departure_id", validDepartureId)
                booking.customerId?.takeIf { it.isValidUuid() }?.let { put("customer_id", it) }
                booking.agencyId?.takeIf { it.isValidUuid() }?.let { put("agency_id", it) }
                put("customer_name", booking.customerName.ifBlank { "Misafir" })
                booking.customerEmail?.takeIf { it.isNotBlank() }?.let { put("customer_email", it) }
                booking.customerPhone?.takeIf { it.isNotBlank() }?.let { put("customer_phone", it) }
                put("total_price", booking.totalPrice)
                put("currency", booking.currency.ifBlank { "TRY" })
                put("pax_count", booking.paxCount)
                put("status", booking.status.dbValue)
                if (richNotes.isNotBlank()) put("notes", richNotes)
                put("tenant_id", validTenantId)
            }

            try {
                supabaseClient.postgrest
                    .from("bookings")
                    .insert(bookingJson)
                println("✅ [BookingRepository] Supabase bookings kaydı başarıyla oluşturuldu: ID=$validId, Kod=$code")
                // Yalnızca Supabase'e başarılı yazıldıktan sonra yerel listeye ekle
                localCache.removeAll { it.id == validId || it.bookingCode == code || it.bookingNumber == code }
                localCache.add(0, entity)
            } catch (e: Exception) {
                println("❌ [BookingRepository] Supabase bookings insert HATASI: ${e.message}")
                e.printStackTrace()
                throw e
            }

            // 3. Supabase booking_items Tablosuna Ekle
            if (booking.items.isNotEmpty()) {
                try {
                    val itemJsons = booking.items.map { itm ->
                        buildJsonObject {
                            put("id", if (itm.id.isValidUuid()) itm.id else generateUuid())
                            put("booking_id", validId)
                            put("title", itm.description.ifBlank { "Hizmet" })
                            put("description", itm.description.ifBlank { "Hizmet" })
                            put("quantity", itm.quantity)
                            put("unit_price", itm.unitPrice)
                            put("total_price", itm.totalPrice)
                            put("item_type", itm.itemType)
                            if (!itm.notes.isNullOrBlank()) put("notes", itm.notes)
                            put("tenant_id", validTenantId)
                        }
                    }
                    supabaseClient.postgrest.from("booking_items").insert(itemJsons)
                    println("✅ [BookingRepository] Supabase booking_items (${booking.items.size} adet) başarıyla eklendi.")
                } catch (err: Exception) {
                    println("❌ [BookingRepository] Supabase booking_items insert hatası: ${err.message}")
                    err.printStackTrace()
                }
            }

            // 4. Supabase passengers Tablosuna Ekle
            if (booking.passengers.isNotEmpty()) {
                try {
                    val paxJsons = booking.passengers.map { p ->
                        val nameParts = p.fullName.trim().split(" ")
                        val fName = nameParts.firstOrNull() ?: "Misafir"
                        val lName = if (nameParts.size > 1) nameParts.drop(1).joinToString(" ") else "Yolcu"
                        buildJsonObject {
                            put("id", if (p.id.isValidUuid()) p.id else generateUuid())
                            put("booking_id", validId)
                            put("full_name", p.fullName)
                            put("first_name", fName)
                            put("last_name", lName)
                            p.tcNo?.takeIf { it.isNotBlank() }?.let { put("tc_no", it) }
                            p.passportNo?.takeIf { it.isNotBlank() }?.let { put("passport_no", it) }
                            val idNum = p.passportNo?.takeIf { it.isNotBlank() } ?: p.tcNo?.takeIf { it.isNotBlank() }
                            idNum?.let { put("id_number", it) }
                            formatToSqlDate(p.birthDate)?.let { put("birth_date", it) }
                            p.gender?.let { put("gender", it) }
                            put("passenger_type", if (p.isLead) "LEAD" else "ADULT")
                            p.phone?.takeIf { it.isNotBlank() }?.let { put("phone", it) }
                            p.email?.takeIf { it.isNotBlank() }?.let { put("email", it) }
                            put("is_lead", p.isLead)
                            p.notes?.takeIf { it.isNotBlank() }?.let { put("notes", it) }
                            put("tenant_id", validTenantId)
                        }
                    }
                    supabaseClient.postgrest.from("passengers").insert(paxJsons)
                    println("✅ [BookingRepository] Supabase passengers (${booking.passengers.size} adet) başarıyla eklendi.")
                } catch (err: Exception) {
                    println("❌ [BookingRepository] Supabase passengers insert hatası: ${err.message}")
                    err.printStackTrace()
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
            val idx = localCache.indexOfFirst { it.id == bookingId || it.bookingCode == bookingId || it.bookingNumber == bookingId }
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
                        fromStatus = currentBooking.safeStatus,
                        toStatus = status,
                        tenantId = currentBooking.safeTenantId,
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
                val isMatch = entity.id == target || entity.bookingCode == target || entity.bookingNumber == target
                if (isMatch) {
                    if (!entity.id.isNullOrBlank()) deletedIdsBlacklist.add(entity.id)
                    if (!entity.bookingCode.isNullOrBlank()) deletedIdsBlacklist.add(entity.bookingCode)
                    if (!entity.bookingNumber.isNullOrBlank()) deletedIdsBlacklist.add(entity.bookingNumber)
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
                    val matching = supabaseClient.postgrest.from("bookings").select { 
                        filter { 
                            or {
                                eq("booking_code", target)
                                eq("booking_number", target)
                            }
                        } 
                    }.decodeList<BookingEntity>()
                    matching.forEach { b ->
                        if (!b.id.isNullOrBlank()) deletedIdsBlacklist.add(b.id)
                        if (!b.bookingCode.isNullOrBlank()) deletedIdsBlacklist.add(b.bookingCode)
                        if (!b.bookingNumber.isNullOrBlank()) deletedIdsBlacklist.add(b.bookingNumber)
                        val bId = b.id
                        if (!bId.isNullOrBlank()) {
                            supabaseClient.postgrest.from("booking_items").delete { filter { eq("booking_id", bId) } }
                            supabaseClient.postgrest.from("passengers").delete { filter { eq("booking_id", bId) } }
                            supabaseClient.postgrest.from("booking_status_logs").delete { filter { eq("booking_id", bId) } }
                            supabaseClient.postgrest.from("bookings").delete { filter { eq("id", bId) } }
                        }
                    }
                    supabaseClient.postgrest.from("bookings").delete { filter { eq("booking_code", target) } }
                    supabaseClient.postgrest.from("bookings").delete { filter { eq("booking_number", target) } }
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
        val code = entity.safeCode
        val price = entity.safeTotalPrice
        val isHotel = entity.bookingType == "HOTEL" || !entity.hotelId.isNullOrBlank()

        val parsedProductName = when {
            !entity.productName.isNullOrBlank() -> entity.productName
            entity.notes?.contains("Ürün: ") == true -> entity.notes.substringAfter("Ürün: ").substringBefore(" •").substringBefore("\n").trim()
            else -> if (isHotel) "Otel Konaklama" else "Tur Rezervasyonu"
        }

        val parsedOperatorName = when {
            !entity.operatorName.isNullOrBlank() -> entity.operatorName
            entity.notes?.contains("Operatör: ") == true -> entity.notes.substringAfter("Operatör: ").substringBefore(" •").substringBefore("\n").trim()
            else -> "MGA Creative"
        }

        val parsedDepartureDate = when {
            !entity.departureDate.isNullOrBlank() -> entity.departureDate
            entity.notes?.contains("Tarih: ") == true -> entity.notes.substringAfter("Tarih: ").substringBefore(" •").substringBefore("\n").trim()
            entity.notes?.contains("Kalkış: ") == true -> entity.notes.substringAfter("Kalkış: ").substringBefore(" •").substringBefore("\n").trim()
            entity.notes?.contains("Giriş: ") == true -> entity.notes.substringAfter("Giriş: ").substringBefore(" •").substringBefore("\n").trim()
            else -> if (isHotel) (entity.checkInDate ?: "2026-09-01") else "2026-09-01"
        }

        val parsedRoomTypeName = when {
            !entity.roomTypeName.isNullOrBlank() -> entity.roomTypeName
            entity.notes?.contains("Oda: ") == true -> entity.notes.substringAfter("Oda: ").substringBefore(" •").substringBefore("\n").trim()
            else -> null
        }

        return Booking(
            id = entity.id.orEmpty(),
            bookingCode = code,
            departureId = entity.departureId,
            customerId = entity.customerId,
            agencyId = entity.agencyId,
            customerName = entity.safeCustomerName,
            customerEmail = entity.customerEmail,
            customerPhone = entity.customerPhone,
            totalPrice = price,
            currency = entity.currency ?: "TRY",
            paxCount = entity.paxCount ?: 1,
            status = BookingStatus.fromDbValue(entity.safeStatus),
            notes = entity.notes,
            optionExpiration = entity.optionExpiration,
            confirmedAt = entity.confirmedAt,
            cancelledAt = entity.cancelledAt,
            operatorName = parsedOperatorName,
            productName = parsedProductName,
            departureDate = parsedDepartureDate,
            hotelId = entity.hotelId,
            checkInDate = entity.checkInDate,
            checkOutDate = entity.checkOutDate,
            roomTypeName = parsedRoomTypeName,
            nights = entity.nights ?: 1,
            bookingType = entity.bookingType ?: (if (isHotel) "HOTEL" else "TOUR"),
            operatorPnrCode = entity.operatorPnrCode,
            operatorStatus = entity.operatorStatus ?: "BEKLİYOR",
            paymentMethod = entity.paymentMethod ?: "CREDIT_CARD",
            tenantId = entity.safeTenantId,
            createdAt = entity.createdAt.orEmpty(),
            updatedAt = entity.updatedAt.orEmpty()
        )
    }

    override suspend fun updateOperatorPnr(bookingId: String, pnrCode: String, operatorStatus: String): Result<Unit> {
        return runCatching {
            val targetId = bookingId.trim()
            val cleanPnr = pnrCode.trim().uppercase()

            // 1. Önbellekteki kaydı bul ve güncelle
            val cachedIdx = localCache.indexOfFirst { 
                it.id == targetId || it.bookingCode == targetId || it.bookingNumber == targetId || it.operatorPnrCode == targetId 
            }
            var foundRealId = if (targetId.isValidUuid()) targetId else ""

            if (cachedIdx != -1) {
                val old = localCache[cachedIdx]
                if (old.id.isValidUuid()) foundRealId = old.id.orEmpty()
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
                            filter { 
                                or {
                                    eq("booking_code", targetId)
                                    eq("booking_number", targetId)
                                }
                            }
                        }
                }

                val currentBooking = localCache.find { it.id == targetId || it.bookingCode == targetId || it.id == foundRealId }
                val netCost = currentBooking?.safeTotalPrice ?: 0.0
                val operatorName = currentBooking?.operatorName ?: "Tur Operatörü"
                val currency = currentBooking?.currency ?: "TRY"
                val tenantId = currentBooking?.safeTenantId ?: "00000000-0000-0000-0000-000000000001"

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
