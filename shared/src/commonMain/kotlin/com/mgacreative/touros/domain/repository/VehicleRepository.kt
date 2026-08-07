package com.mgacreative.touros.domain.repository

import com.mgacreative.touros.domain.model.Vehicle

/**
 * 2.4.1 Araç CRUD Repository Arayüzü.
 */
interface VehicleRepository {
    suspend fun getVehicles(tenantId: String, vehicleType: String? = null): Result<List<Vehicle>>
    suspend fun getVehicleById(id: String): Result<Vehicle>
    suspend fun createVehicle(vehicle: Vehicle): Result<Vehicle>
    suspend fun updateVehicle(vehicle: Vehicle): Result<Vehicle>
    suspend fun deleteVehicle(id: String): Result<Boolean>
}
