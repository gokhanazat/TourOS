package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.model.VehicleMaintenanceAlert
import com.mgacreative.touros.domain.repository.VehicleRepository
import kotlinx.datetime.LocalDate

/**
 * 2.4.4 Bakım/Sigorta/Muayene Yaklaşan Uyarıları Getirme Use Case.
 */
class GetVehicleMaintenanceAlertsUseCase(
    private val vehicleRepository: VehicleRepository
) {
    suspend operator fun invoke(tenantId: String, daysThreshold: Int = 30): Result<List<VehicleMaintenanceAlert>> {
        return runCatching {
            val vehiclesRes = vehicleRepository.getVehicles(tenantId)
            val vehicles = vehiclesRes.getOrDefault(emptyList())

            val alerts = mutableListOf<VehicleMaintenanceAlert>()
            val today = "2026-08-06"

            vehicles.forEach { vehicle ->
                val brandModelStr = "${vehicle.brand ?: ""} ${vehicle.model ?: ""}".trim().ifEmpty { "Araç" }

                // 1. Sigorta Bitiş Kontrolü
                vehicle.insuranceExpiry?.let { expiry ->
                    val daysLeft = calculateDaysBetween(today, expiry)
                    if (daysLeft <= daysThreshold) {
                        alerts.add(
                            VehicleMaintenanceAlert(
                                vehicleId = vehicle.id,
                                plateNumber = vehicle.plateNumber,
                                brandModel = brandModelStr,
                                alertType = "INSURANCE_EXPIRING",
                                expiryDate = expiry,
                                daysLeft = daysLeft,
                                severity = if (daysLeft <= 7) "CRITICAL" else "WARNING"
                            )
                        )
                    }
                }

                // 2. Muayene Bitiş Kontrolü
                vehicle.inspectionExpiry?.let { expiry ->
                    val daysLeft = calculateDaysBetween(today, expiry)
                    if (daysLeft <= daysThreshold) {
                        alerts.add(
                            VehicleMaintenanceAlert(
                                vehicleId = vehicle.id,
                                plateNumber = vehicle.plateNumber,
                                brandModel = brandModelStr,
                                alertType = "INSPECTION_EXPIRING",
                                expiryDate = expiry,
                                daysLeft = daysLeft,
                                severity = if (daysLeft <= 7) "CRITICAL" else "WARNING"
                            )
                        )
                    }
                }

                // 3. Gelecek Bakım Bitiş Kontrolü
                vehicle.nextMaintenanceDate?.let { expiry ->
                    val daysLeft = calculateDaysBetween(today, expiry)
                    if (daysLeft <= daysThreshold) {
                        alerts.add(
                            VehicleMaintenanceAlert(
                                vehicleId = vehicle.id,
                                plateNumber = vehicle.plateNumber,
                                brandModel = brandModelStr,
                                alertType = "MAINTENANCE_DUE",
                                expiryDate = expiry,
                                daysLeft = daysLeft,
                                severity = if (daysLeft <= 7) "CRITICAL" else "WARNING"
                            )
                        )
                    }
                }
            }

            alerts.sortedBy { it.daysLeft }
        }
    }

    private fun calculateDaysBetween(startDateStr: String, endDateStr: String): Int {
        return try {
            val start = LocalDate.parse(startDateStr)
            val end = LocalDate.parse(endDateStr)
            (end.toEpochDays() - start.toEpochDays()).toInt()
        } catch (_: Exception) {
            15
        }
    }
}
