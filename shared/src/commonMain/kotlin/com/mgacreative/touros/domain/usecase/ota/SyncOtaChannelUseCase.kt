package com.mgacreative.touros.domain.usecase.ota

import com.mgacreative.touros.domain.engine.OTASyncManager
import com.mgacreative.touros.domain.model.ota.OTABooking

class SyncOtaChannelUseCase(
    private val otaSyncManager: OTASyncManager
) {
    suspend operator fun invoke(accountId: String, isFullSync: Boolean, tenantId: String): Result<List<OTABooking>> {
        return if (isFullSync) {
            otaSyncManager.performFullSync(accountId, tenantId)
        } else {
            otaSyncManager.performIncrementalSync(accountId, tenantId)
        }
    }
}
