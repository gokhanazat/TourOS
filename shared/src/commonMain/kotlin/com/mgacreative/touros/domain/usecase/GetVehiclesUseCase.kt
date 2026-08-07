package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.model.Vehicle
import com.mgacreative.touros.domain.repository.VehicleRepository

/**
 * 2.4.1 Araç Listeleme Use Case.
 */
class GetVehiclesUseCase(
    private val vehicleRepository: VehicleRepository
) {
    suspend operator fun invoke(tenantId: String, vehicleType: String? = null): Result<List<Vehicle>> {
        return vehicleRepository.getVehicles(tenantId, vehicleType)
    }
}
