package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.domain.model.AssignedTask
import com.mgacreative.touros.domain.model.TaskStatus
import com.mgacreative.touros.domain.usecase.GetAssignedTasksUseCase
import com.mgacreative.touros.domain.usecase.GetCurrentUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AssignedTasksUiState {
    data object Loading : AssignedTasksUiState
    data class Success(val tasks: List<AssignedTask>) : AssignedTasksUiState
    data class Error(val message: String) : AssignedTasksUiState
}

class AssignedTasksViewModel(
    private val getAssignedTasksUseCase: GetAssignedTasksUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<AssignedTasksUiState>(AssignedTasksUiState.Loading)
    val uiState: StateFlow<AssignedTasksUiState> = _uiState.asStateFlow()

    init {
        loadTasks()
    }

    fun loadTasks() {
        viewModelScope.launch {
            _uiState.value = AssignedTasksUiState.Loading
            val currentUser = getCurrentUserUseCase()
            if (currentUser == null) {
                _uiState.value = AssignedTasksUiState.Error("Oturum bulunamadı")
                return@launch
            }

            getAssignedTasksUseCase.getTasks(currentUser.id)
                .onSuccess { tasks ->
                    _uiState.value = AssignedTasksUiState.Success(tasks)
                }
                .onFailure { exception ->
                    _uiState.value = AssignedTasksUiState.Error(
                        exception.message ?: "Görevler yüklenirken hata oluştu"
                    )
                }
        }
    }

    fun updateStatus(taskId: String, newStatus: TaskStatus) {
        viewModelScope.launch {
            getAssignedTasksUseCase.updateStatus(taskId, newStatus)
                .onSuccess {
                    loadTasks()
                }
        }
    }
}
