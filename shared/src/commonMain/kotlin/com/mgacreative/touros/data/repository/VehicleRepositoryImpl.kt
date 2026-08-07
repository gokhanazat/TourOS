package com.mgacreative.touros.data.repository

import com.mgacreative.touros.data.database.entity.VehicleEntity
import com.mgacreative.touros.domain.model.Vehicle
import com.mgacreative.touros.domain.repository.VehicleRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

import com.mgacreative.touros.data.util.isValidUuid

class VehicleRepositoryImpl(
    private val supabaseClient: SupabaseClient
) : VehicleRepository {

    override suspend fun getVehicles(tenantId: String, vehicleType: String?): Result<List<Vehicle>> {
        return runCatching {
            val entities = supabaseClient.postgrest.from("vehicles")
                .select {
                    filter {
                        if (tenantId.isValidUuid()) {
                            eq("tenant_id", tenantId)
                        }
                        if (!vehicleType.isNullOrBlank()) {
                            eq("vehicle_type", vehicleType)
                        }
                    }
                }
                .decodeList<VehicleEntity>()

            entities.map { entity ->
                Vehicle(
                    id = entity.id,
                    plateNumber = entity.plateNumber,
                    brand = entity.brand,
                    model = entity.model,
                    year = entity.year,
                    capacity = entity.capacity,
                    vehicleType = entity.vehicleType,
                    color = entity.color,
                    isOwned = entity.isOwned,
                    ownerInfo = entity.ownerInfo,
                    insuranceExpiry = entity.insuranceExpiry,
                    inspectionExpiry = entity.inspectionExpiry,
                    lastMaintenanceDate = entity.lastMaintenanceDate,
                    nextMaintenanceDate = entity.nextMaintenanceDate,
                    maintenanceNotes = entity.maintenanceNotes,
                    isActive = entity.isActive,
                    tenantId = entity.tenantId
                )
            }
        }
    }

    override suspend fun getVehicleById(id: String): Result<Vehicle> {
        return runCatching {
            val entity = supabaseClient.postgrest.from("vehicles")
                .select { filter { eq("id", id) } }
                .decodeSingle<VehicleEntity>()

            Vehicle(
                id = entity.id,
                plateNumber = entity.plateNumber,
                brand = entity.brand,
                model = entity.model,
                year = entity.year,
                capacity = entity.capacity,
                vehicleType = entity.vehicleType,
                color = entity.color,
                isOwned = entity.isOwned,
                ownerInfo = entity.ownerInfo,
                insuranceExpiry = entity.insuranceExpiry,
                inspectionExpiry = entity.inspectionExpiry,
                lastMaintenanceDate = entity.lastMaintenanceDate,
                nextMaintenanceDate = entity.nextMaintenanceDate,
                maintenanceNotes = entity.maintenanceNotes,
                isActive = entity.isActive,
                tenantId = entity.tenantId
            )
        }
    }

    override suspend fun createVehicle(vehicle: Vehicle): Result<Vehicle> {
        return runCatching {
            val entity = VehicleEntity(
                plateNumber = vehicle.plateNumber,
                brand = vehicle.brand,
                model = vehicle.model,
                year = vehicle.year,
                capacity = vehicle.capacity,
                vehicleType = vehicle.vehicleType,
                color = vehicle.color,
                isOwned = vehicle.isOwned,
                ownerInfo = vehicle.ownerInfo,
                insuranceExpiry = vehicle.insuranceExpiry,
                inspectionExpiry = vehicle.inspectionExpiry,
                lastMaintenanceDate = vehicle.lastMaintenanceDate,
                nextMaintenanceDate = vehicle.nextMaintenanceDate,
                maintenanceNotes = vehicle.maintenanceNotes,
                isActive = vehicle.isActive,
                tenantId = vehicle.tenantId
            )
            val created = supabaseClient.postgrest.from("vehicles")
                .insert(entity) { select() }
                .decodeSingle<VehicleEntity>()

            vehicle.copy(id = created.id)
        }
    }

    override suspend fun updateVehicle(vehicle: Vehicle): Result<Vehicle> {
        return runCatching {
            val entity = VehicleEntity(
                id = vehicle.id,
                plateNumber = vehicle.plateNumber,
                brand = vehicle.brand,
                model = vehicle.model,
                year = vehicle.year,
                capacity = vehicle.capacity,
                vehicleType = vehicle.vehicleType,
                color = vehicle.color,
                isOwned = vehicle.isOwned,
                ownerInfo = vehicle.ownerInfo,
                insuranceExpiry = vehicle.insuranceExpiry,
                inspectionExpiry = vehicle.inspectionExpiry,
                lastMaintenanceDate = vehicle.lastMaintenanceDate,
                nextMaintenanceDate = vehicle.nextMaintenanceDate,
                maintenanceNotes = vehicle.maintenanceNotes,
                isActive = vehicle.isActive,
                tenantId = vehicle.tenantId
            )
            supabaseClient.postgrest.from("vehicles")
                .update(entity) { filter { eq("id", vehicle.id) } }
            vehicle
        }
    }

    override suspend fun deleteVehicle(id: String): Result<Boolean> {
        return runCatching {
            supabaseClient.postgrest.from("vehicles")
                .delete { filter { eq("id", id) } }
            true
        }
    }
}
