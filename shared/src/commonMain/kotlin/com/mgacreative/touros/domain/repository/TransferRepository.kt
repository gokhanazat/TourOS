package com.mgacreative.touros.domain.repository

import com.mgacreative.touros.domain.model.Driver
import com.mgacreative.touros.domain.model.Guide
import com.mgacreative.touros.domain.model.TransferTask

/**
 * 2.4.2 Transfer Görevi, Şoför ve Rehber Repository Arayüzü.
 */
interface TransferRepository {
    suspend fun getTransfers(tenantId: String, status: String? = null): Result<List<TransferTask>>
    suspend fun getDrivers(tenantId: String): Result<List<Driver>>
    suspend fun getGuides(tenantId: String): Result<List<Guide>>
    suspend fun assignDriverAndGuide(
        transferId: String,
        driverId: String?,
        guideId: String?,
        vehicleId: String?
    ): Result<Boolean>
    suspend fun createTransfer(transfer: TransferTask): Result<TransferTask>
    suspend fun updateTransferStatus(transferId: String, status: String): Result<Boolean>
}
