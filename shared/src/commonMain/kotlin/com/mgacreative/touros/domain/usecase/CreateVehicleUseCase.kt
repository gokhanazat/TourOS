package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.model.Vehicle
import com.mgacreative.touros.domain.repository.VehicleRepository

/**
 * 2.4.1 Araç Ekleme / Güncelleme Use Case.
 */
class CreateVehicleUseCase(
    private val vehicleRepository: VehicleRepository
) {
    suspend operator fun invoke(vehicle: Vehicle): Result<Vehicle> {
        if (vehicle.plateNumber.isBlank()) {
            return Result.failure(IllegalArgumentException("Araç plakası zorunludur."))
        }
        if (vehicle.capacity <= 0) {
            return Result.failure(IllegalArgumentException("Koltuk kapasitesi 0'dan büyük olmalıdır."))
        }

        return if (vehicle.id.isBlank()) {
            vehicleRepository.createVehicle(vehicle)
        } else {
            vehicleRepository.updateVehicle(vehicle)
        }
    }
}
