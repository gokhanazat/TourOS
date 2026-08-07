package com.mgacreative.touros.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class TaskStatus(val displayName: String) {
    @SerialName("open") OPEN("Bekliyor"),
    @SerialName("in_progress") IN_PROGRESS("Devam Ediyor"),
    @SerialName("done") DONE("Tamamlandı"),
    @SerialName("cancelled") CANCELLED("İptal Edildi");

    companion object {
        fun fromKey(key: String): TaskStatus =
            entries.firstOrNull { it.name.equals(key, ignoreCase = true) || it.displayName.equals(key, ignoreCase = true) } ?: OPEN
    }
}

@Serializable
enum class TaskType(val displayName: String) {
    @SerialName("tour") TOUR("Tur Rehberliği"),
    @SerialName("transfer") TRANSFER("Yolcu Transferi"),
    @SerialName("custom") CUSTOM("Genel Görev");

    companion object {
        fun fromKey(key: String): TaskType =
            entries.firstOrNull { it.name.equals(key, ignoreCase = true) } ?: CUSTOM
    }
}

/**
 * Rehber ve Şoföre Atanmış Görev Domain Modeli.
 */
@Serializable
data class AssignedTask(
    val id: String,
    val title: String,
    val description: String? = null,
    val taskType: TaskType = TaskType.CUSTOM,
    val status: TaskStatus = TaskStatus.OPEN,
    val dueDate: String? = null,
    val location: String? = null,
    val assignedTo: String? = null
)
