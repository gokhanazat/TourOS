package com.mgacreative.touros.domain.usecase.ota

import com.mgacreative.touros.domain.model.ota.OTABooking
import com.mgacreative.touros.domain.repository.OTARepository

class GetOtaBookingsUseCase(
    private val otaRepository: OTARepository
) {
    suspend operator fun invoke(accountId: String = "acc-001", tenantId: String): Result<List<OTABooking>> {
        return otaRepository.syncBookings(accountId, tenantId)
    }
}
