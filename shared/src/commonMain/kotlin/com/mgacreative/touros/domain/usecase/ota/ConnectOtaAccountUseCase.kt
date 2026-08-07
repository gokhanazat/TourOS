package com.mgacreative.touros.domain.usecase.ota

import com.mgacreative.touros.domain.model.ota.OTAAccount
import com.mgacreative.touros.domain.model.ota.OTAConnection
import com.mgacreative.touros.domain.repository.OTARepository

class ConnectOtaAccountUseCase(
    private val otaRepository: OTARepository
) {
    suspend operator fun invoke(account: OTAAccount): Result<OTAConnection> {
        return otaRepository.connect(account)
    }
}
