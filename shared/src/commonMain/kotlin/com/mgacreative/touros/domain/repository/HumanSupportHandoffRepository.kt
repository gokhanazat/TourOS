package com.mgacreative.touros.domain.repository

import com.mgacreative.touros.domain.model.faq.SupportHandoffTicket

interface HumanSupportHandoffRepository {
    suspend fun initiateHandoff(customerId: String, chatSummary: String, tenantId: String): Result<SupportHandoffTicket>
}
