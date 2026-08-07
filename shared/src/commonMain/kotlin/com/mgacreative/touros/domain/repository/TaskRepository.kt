package com.mgacreative.touros.domain.repository

import com.mgacreative.touros.domain.model.AssignedTask
import com.mgacreative.touros.domain.model.TaskStatus

/**
 * Görev ve Transfer İşlemleri Repository Arayüzü.
 */
interface TaskRepository {
    suspend fun getAssignedTasksForUser(userId: String): Result<List<AssignedTask>>
    suspend fun updateTaskStatus(taskId: String, status: TaskStatus): Result<Unit>
}
