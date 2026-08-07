package com.mgacreative.touros.data.repository

import com.mgacreative.touros.data.database.entity.DriverEntity
import com.mgacreative.touros.data.database.entity.GuideEntity
import com.mgacreative.touros.data.database.entity.TransferEntity
import com.mgacreative.touros.domain.model.Driver
import com.mgacreative.touros.domain.model.Guide
import com.mgacreative.touros.domain.model.TransferTask
import com.mgacreative.touros.domain.repository.TransferRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

import com.mgacreative.touros.data.util.isValidUuid

class TransferRepositoryImpl(
    private val supabaseClient: SupabaseClient
) : TransferRepository {

    override suspend fun getTransfers(tenantId: String, status: String?): Result<List<TransferTask>> {
        return runCatching {
            val entities = supabaseClient.postgrest.from("transfers")
                .select {
                    filter {
                        if (tenantId.isValidUuid()) {
                            eq("tenant_id", tenantId)
                        }
                        if (!status.isNullOrBlank()) {
                            eq("status", status)
                        }
                    }
                }
                .decodeList<TransferEntity>()

            entities.map { entity ->
                TransferTask(
                    id = entity.id,
                    bookingId = entity.bookingId,
                    departureId = entity.departureId,
                    vehicleId = entity.vehicleId,
                    driverId = entity.driverId,
                    guideId = entity.guideId,
                    transferType = entity.transferType,
                    origin = entity.origin,
                    destination = entity.destination,
                    pickupTime = entity.pickupTime,
                    dropoffTime = entity.dropoffTime,
                    paxCount = entity.paxCount,
                    status = entity.status,
                    price = entity.price,
                    currency = entity.currency,
                    notes = entity.notes,
                    tenantId = entity.tenantId
                )
            }
        }
    }

    override suspend fun getDrivers(tenantId: String): Result<List<Driver>> {
        return runCatching {
            val entities = supabaseClient.postgrest.from("drivers")
                .select {
                    filter {
                        if (tenantId.isValidUuid()) {
                            eq("tenant_id", tenantId)
                        }
                    }
                }
                .decodeList<DriverEntity>()

            entities.map { entity ->
                Driver(
                    id = entity.id,
                    fullName = entity.fullName,
                    phone = entity.phone,
                    email = entity.email,
                    licenseClass = entity.licenseClass,
                    licenseExpiry = entity.licenseExpiry,
                    tcNo = entity.tcNo,
                    isActive = entity.isActive,
                    tenantId = entity.tenantId
                )
            }
        }
    }

    override suspend fun getGuides(tenantId: String): Result<List<Guide>> {
        return runCatching {
            val entities = supabaseClient.postgrest.from("guides")
                .select {
                    filter {
                        if (tenantId.isValidUuid()) {
                            eq("tenant_id", tenantId)
                        }
                    }
                }
                .decodeList<GuideEntity>()

            entities.map { entity ->
                Guide(
                    id = entity.id,
                    fullName = entity.fullName,
                    phone = entity.phone,
                    email = entity.email,
                    licenseNumber = entity.licenseNumber,
                    languages = entity.languages,
                    specialization = entity.specialization,
                    isActive = entity.isActive,
                    tenantId = entity.tenantId
                )
            }
        }
    }

    override suspend fun assignDriverAndGuide(
        transferId: String,
        driverId: String?,
        guideId: String?,
        vehicleId: String?
    ): Result<Boolean> {
        return runCatching {
            val updatePayload = mutableMapOf<String, Any?>()
            if (driverId != null) updatePayload["driver_id"] = driverId
            if (guideId != null) updatePayload["guide_id"] = guideId
            if (vehicleId != null) updatePayload["vehicle_id"] = vehicleId
            updatePayload["status"] = "assigned"

            supabaseClient.postgrest.from("transfers")
                .update(updatePayload) { filter { eq("id", transferId) } }
            true
        }
    }

    override suspend fun createTransfer(transfer: TransferTask): Result<TransferTask> {
        return runCatching {
            val entity = TransferEntity(
                bookingId = transfer.bookingId,
                departureId = transfer.departureId,
                vehicleId = transfer.vehicleId,
                driverId = transfer.driverId,
                guideId = transfer.guideId,
                transferType = transfer.transferType,
                origin = transfer.origin,
                destination = transfer.destination,
                pickupTime = transfer.pickupTime,
                dropoffTime = transfer.dropoffTime,
                paxCount = transfer.paxCount,
                status = transfer.status,
                price = transfer.price,
                currency = transfer.currency,
                notes = transfer.notes,
                tenantId = transfer.tenantId
            )
            val created = supabaseClient.postgrest.from("transfers")
                .insert(entity) { select() }
                .decodeSingle<TransferEntity>()

            transfer.copy(id = created.id)
        }
    }

    override suspend fun updateTransferStatus(transferId: String, status: String): Result<Boolean> {
        return runCatching {
            supabaseClient.postgrest.from("transfers")
                .update(mapOf("status" to status)) { filter { eq("id", transferId) } }
            true
        }
    }
}
