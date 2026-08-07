package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.model.AssignedTask
import com.mgacreative.touros.domain.model.TaskStatus
import com.mgacreative.touros.domain.repository.TaskRepository

class GetAssignedTasksUseCase(
    private val taskRepository: TaskRepository
) {
    suspend fun getTasks(userId: String): Result<List<AssignedTask>> {
        if (userId.isBlank()) {
            return Result.failure(IllegalArgumentException("Geçersiz Kullanıcı ID"))
        }
        return taskRepository.getAssignedTasksForUser(userId)
    }

    suspend fun updateStatus(taskId: String, status: TaskStatus): Result<Unit> {
        if (taskId.isBlank()) {
            return Result.failure(IllegalArgumentException("Geçersiz Görev ID"))
        }
        return taskRepository.updateTaskStatus(taskId, status)
    }
}
