package com.mgacreative.touros.domain.engine

import com.mgacreative.touros.domain.model.faq.HandoffStatus
import com.mgacreative.touros.domain.model.faq.SupportHandoffTicket

class HumanSupportHandoffEngine {

    fun createHandoffTicket(chatSummary: String): SupportHandoffTicket {
        return SupportHandoffTicket(
            ticketId = "ticket-${chatSummary.hashCode().toString().take(6)}",
            status = HandoffStatus.QUEUED,
            assignedAgentName = "Müşteri Temsilcisi Zeynep",
            estimatedWaitMinutes = 2
        )
    }
}
