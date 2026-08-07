package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.domain.model.Permission
import com.mgacreative.touros.domain.model.PermissionAction
import com.mgacreative.touros.domain.model.PermissionResource
import com.mgacreative.touros.domain.model.UserRole
import com.mgacreative.touros.domain.security.PermissionGuard
import com.mgacreative.touros.domain.usecase.UpdateRolePermissionsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface PermissionMatrixUiState {
    data object Loading : PermissionMatrixUiState
    data class Success(
        val selectedRole: UserRole,
        val permissionMap: Map<Pair<PermissionResource, PermissionAction>, Boolean>
    ) : PermissionMatrixUiState
    data class Error(val message: String) : PermissionMatrixUiState
    data object Saving : PermissionMatrixUiState
}

class PermissionMatrixViewModel(
    private val updateRolePermissionsUseCase: UpdateRolePermissionsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<PermissionMatrixUiState>(PermissionMatrixUiState.Loading)
    val uiState: StateFlow<PermissionMatrixUiState> = _uiState.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    init {
        selectRole(UserRole.TOUR_OPERATOR)
    }

    fun selectRole(role: UserRole) {
        viewModelScope.launch {
            _uiState.value = PermissionMatrixUiState.Loading

            // Rol için varsayılan ve dinamik izin haritasını oluştur
            val map = mutableMapOf<Pair<PermissionResource, PermissionAction>, Boolean>()
            PermissionResource.entries.forEach { resource ->
                PermissionAction.entries.forEach { action ->
                    val isAllowed = PermissionGuard.hasPermission(role, resource, action)
                    map[Pair(resource, action)] = isAllowed
                }
            }

            _uiState.value = PermissionMatrixUiState.Success(role, map)
        }
    }

    fun togglePermission(resource: PermissionResource, action: PermissionAction, isAllowed: Boolean) {
        val currentState = _uiState.value
        if (currentState is PermissionMatrixUiState.Success) {
            val newMap = currentState.permissionMap.toMutableMap()
            newMap[Pair(resource, action)] = isAllowed
            _uiState.value = currentState.copy(permissionMap = newMap)
        }
    }

    fun savePermissions(roleId: String = "role_id") {
        val currentState = _uiState.value
        if (currentState is PermissionMatrixUiState.Success) {
            viewModelScope.launch {
                _uiState.value = PermissionMatrixUiState.Saving

                val permissions = currentState.permissionMap.map { (key, isAllowed) ->
                    Permission(
                        id = "perm_${currentState.selectedRole.name}_${key.first.key}_${key.second.key}",
                        roleId = roleId,
                        resource = key.first,
                        action = key.second,
                        isAllowed = isAllowed
                    )
                }

                updateRolePermissionsUseCase(roleId, permissions)
                    .onSuccess {
                        _uiState.value = currentState
                        _userMessage.value = "Yetki matrisi başarıyla güncellendi ve anlık uygulandı"
                    }
                    .onFailure { exception ->
                        _uiState.value = PermissionMatrixUiState.Error(
                            exception.message ?: "İzinler kaydedilirken hata oluştu"
                        )
                    }
            }
        }
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }
}
