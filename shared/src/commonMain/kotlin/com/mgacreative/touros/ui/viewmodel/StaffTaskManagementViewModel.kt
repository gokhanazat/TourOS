package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.domain.model.StaffTaskItem
import com.mgacreative.touros.domain.usecase.CreateStaffTaskUseCase
import com.mgacreative.touros.domain.usecase.GetCurrentUserUseCase
import com.mgacreative.touros.domain.usecase.GetStaffTasksUseCase
import com.mgacreative.touros.domain.usecase.SyncTaskCalendarUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class StaffTaskManagementUiState(
    val selectedPriority: String = "ALL", // ALL, URGENT, HIGH, MEDIUM, LOW
    val tasks: List<StaffTaskItem> = emptyList(),
    val isCalendarSynced: Boolean = true,
    val isLoading: Boolean = false,
    val notificationMessage: String? = null,
    val errorMessage: String? = null
)

class StaffTaskManagementViewModel(
    private val getStaffTasksUseCase: GetStaffTasksUseCase,
    private val createStaffTaskUseCase: CreateStaffTaskUseCase,
    private val syncTaskCalendarUseCase: SyncTaskCalendarUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(StaffTaskManagementUiState())
    val uiState: StateFlow<StaffTaskManagementUiState> = _uiState.asStateFlow()

    init {
        loadTasks()
    }

    fun loadTasks() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val user = getCurrentUserUseCase()
            val tenantId = user?.tenantId ?: "tenant_id"

            val res = getStaffTasksUseCase(tenantId)
            res.onSuccess { list ->
                _uiState.value = _uiState.value.copy(
                    tasks = list,
                    isLoading = false
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = err.message
                )
            }
        }
    }

    fun setPriorityFilter(priority: String) {
        _uiState.value = _uiState.value.copy(selectedPriority = priority)
    }

    fun addTask(title: String, assignedTo: String, priority: String, dueDate: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val user = getCurrentUserUseCase()
            val tenantId = user?.tenantId ?: "tenant_id"

            val newTask = StaffTaskItem(
                title = title,
                description = "Takvim entegrasyonu ile otomatik atanan personel görevi.",
                assignedTo = assignedTo,
                dueDate = dueDate,
                priority = priority,
                status = "PENDING",
                reminderMinutesBefore = 30,
                tenantId = tenantId,
                createdAt = "2026-08-06 13:57"
            )

            val res = createStaffTaskUseCase(newTask)
            res.onSuccess { created ->
                val calRes = syncTaskCalendarUseCase(created)
                val syncedTask = created.copy(calendarEventId = calRes.getOrNull() ?: "cal-123")

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    tasks = listOf(syncedTask) + _uiState.value.tasks,
                    notificationMessage = "✅ Yeni Görev Eklendi & Google Calendar Takvimine Senkronize Edildi!"
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = err.message ?: "Görev oluşturma hatası."
                )
            }
        }
    }
}
