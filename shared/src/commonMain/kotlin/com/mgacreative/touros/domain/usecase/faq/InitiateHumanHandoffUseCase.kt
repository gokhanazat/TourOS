package com.mgacreative.touros.domain.usecase.faq

import com.mgacreative.touros.domain.model.faq.SupportHandoffTicket
import com.mgacreative.touros.domain.repository.HumanSupportHandoffRepository

class InitiateHumanHandoffUseCase(
    private val repository: HumanSupportHandoffRepository
) {
    suspend operator fun invoke(customerId: String, chatSummary: String, tenantId: String): Result<SupportHandoffTicket> {
        return repository.initiateHandoff(customerId, chatSummary, tenantId)
    }
}
