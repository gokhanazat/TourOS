package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.data.database.entity.TransferPickupEntity
import com.mgacreative.touros.domain.model.PickupPoint
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

import com.mgacreative.touros.data.util.isValidUuid

/**
 * 2.4.3 Şoför Pickup Listesi Getirme Use Case.
 */
class GetDriverPickupListUseCase(
    private val supabaseClient: SupabaseClient
) {
    suspend operator fun invoke(tenantId: String, transferId: String? = null): Result<List<PickupPoint>> {
        return runCatching {
            val entities = supabaseClient.postgrest.from("transfer_pickups")
                .select {
                    filter {
                        if (tenantId.isValidUuid()) {
                            eq("tenant_id", tenantId)
                        }
                        if (!transferId.isNullOrBlank()) {
                            eq("transfer_id", transferId)
                        }
                    }
                }
                .decodeList<TransferPickupEntity>()

            entities.map { entity ->
                PickupPoint(
                    id = entity.id,
                    transferId = entity.transferId,
                    passengerName = entity.passengerName,
                    passengerPhone = entity.passengerPhone,
                    hotelName = entity.hotelName,
                    locationName = entity.locationName,
                    latitude = entity.latitude,
                    longitude = entity.longitude,
                    scheduledTime = entity.scheduledTime,
                    status = entity.status,
                    paxCount = entity.paxCount,
                    roomNumber = entity.roomNumber,
                    notes = entity.notes,
                    tenantId = entity.tenantId
                )
            }
        }
    }
}
