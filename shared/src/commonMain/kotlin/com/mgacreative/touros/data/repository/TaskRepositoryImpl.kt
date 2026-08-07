package com.mgacreative.touros.data.repository

import com.mgacreative.touros.data.database.entity.TaskEntity
import com.mgacreative.touros.domain.model.AssignedTask
import com.mgacreative.touros.domain.model.TaskStatus
import com.mgacreative.touros.domain.model.TaskType
import com.mgacreative.touros.domain.repository.TaskRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class TaskRepositoryImpl(
    private val supabaseClient: SupabaseClient
) : TaskRepository {

    override suspend fun getAssignedTasksForUser(userId: String): Result<List<AssignedTask>> {
        return runCatching {
            val entities = supabaseClient.postgrest.from("tasks")
                .select {
                    filter {
                        eq("assigned_to", userId)
                    }
                }
                .decodeList<TaskEntity>()

            entities.map { entity ->
                AssignedTask(
                    id = entity.id,
                    title = entity.title,
                    description = entity.description,
                    taskType = TaskType.fromKey(entity.refType ?: "custom"),
                    status = TaskStatus.fromKey(entity.status),
                    dueDate = entity.dueDate,
                    assignedTo = entity.assignedTo
                )
            }
        }
    }

    override suspend fun updateTaskStatus(taskId: String, status: TaskStatus): Result<Unit> {
        return runCatching {
            val updatePayload = buildJsonObject {
                put("status", status.name.lowercase())
            }
            supabaseClient.postgrest.from("tasks").update(updatePayload) {
                filter {
                    eq("id", taskId)
                }
            }
        }
    }
}
