package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.repository.TransferRepository

/**
 * 2.4.2 Şoför ve Rehber Atama Use Case.
 */
class AssignDriverAndGuideUseCase(
    private val transferRepository: TransferRepository
) {
    suspend operator fun invoke(
        transferId: String,
        driverId: String?,
        guideId: String?,
        vehicleId: String?
    ): Result<Boolean> {
        if (transferId.isBlank()) {
            return Result.failure(IllegalArgumentException("Transfer görevi ID boş olamaz."))
        }
        return transferRepository.assignDriverAndGuide(transferId, driverId, guideId, vehicleId)
    }
}
