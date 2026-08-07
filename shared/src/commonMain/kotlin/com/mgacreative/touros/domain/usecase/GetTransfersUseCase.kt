package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.model.TransferTask
import com.mgacreative.touros.domain.repository.TransferRepository

/**
 * 2.4.2 Transfer Görevlerini Getirme Use Case.
 */
class GetTransfersUseCase(
    private val transferRepository: TransferRepository
) {
    suspend operator fun invoke(tenantId: String, status: String? = null): Result<List<TransferTask>> {
        return transferRepository.getTransfers(tenantId, status)
    }
}
