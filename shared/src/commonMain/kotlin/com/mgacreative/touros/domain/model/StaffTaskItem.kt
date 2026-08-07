package com.mgacreative.touros.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 3.4.3 Personel Görev Yönetimi, Hatırlatma ve Takvim Modeli.
 */
@Serializable
data class StaffTaskItem(
    @SerialName("task_id") val id: String = "",
    val title: String = "",
    val description: String = "",
    @SerialName("assigned_to") val assignedTo: String = "",
    @SerialName("due_date") val dueDate: String = "",
    val priority: String = "MEDIUM", // LOW, MEDIUM, HIGH, URGENT
    val status: String = "PENDING", // PENDING, IN_PROGRESS, COMPLETED, CANCELLED
    @SerialName("reminder_minutes_before") val reminderMinutesBefore: Int = 30,
    @SerialName("calendar_event_id") val calendarEventId: String? = null,
    @SerialName("tenant_id") val tenantId: String = "",
    @SerialName("created_at") val createdAt: String = ""
)
