package com.mgacreative.touros.domain.model.faq

import kotlinx.serialization.Serializable

enum class HandoffStatus {
    QUEUED,
    IN_PROGRESS,
    RESOLVED
}

@Serializable
data class SupportHandoffTicket(
    val ticketId: String,
    val status: HandoffStatus = HandoffStatus.QUEUED,
    val assignedAgentName: String = "Müşteri Temsilcisi Zeynep",
    val estimatedWaitMinutes: Int = 2
)
