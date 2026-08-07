package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.domain.model.User
import com.mgacreative.touros.domain.model.UserRole
import com.mgacreative.touros.domain.usecase.GetCurrentUserUseCase
import com.mgacreative.touros.domain.usecase.GetUsersUseCase
import com.mgacreative.touros.domain.usecase.ToggleUserStatusUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface UserListUiState {
    data object Loading : UserListUiState
    data class Success(
        val users: List<User>,
        val searchQuery: String = "",
        val selectedRoleFilter: UserRole? = null,
        val activeOnlyFilter: Boolean = false
    ) : UserListUiState
    data class Error(val message: String) : UserListUiState
}

class UserListViewModel(
    private val getUsersUseCase: GetUsersUseCase,
    private val toggleUserStatusUseCase: ToggleUserStatusUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<UserListUiState>(UserListUiState.Loading)
    val uiState: StateFlow<UserListUiState> = _uiState.asStateFlow()

    private var tenantId: String = ""
    private var currentSearch: String = ""
    private var currentRole: UserRole? = null
    private var currentActiveOnly: Boolean = false

    init {
        loadUsers()
    }

    fun loadUsers() {
        viewModelScope.launch {
            _uiState.value = UserListUiState.Loading
            val currentUser = getCurrentUserUseCase()
            tenantId = currentUser?.tenantId ?: "tenant_id"
            fetchFilteredUsers()
        }
    }

    private suspend fun fetchFilteredUsers() {
        getUsersUseCase(
            tenantId = tenantId,
            searchQuery = currentSearch,
            roleFilter = currentRole,
            activeOnly = currentActiveOnly
        ).onSuccess { list ->
            _uiState.value = UserListUiState.Success(
                users = list,
                searchQuery = currentSearch,
                selectedRoleFilter = currentRole,
                activeOnlyFilter = currentActiveOnly
            )
        }.onFailure { exception ->
            _uiState.value = UserListUiState.Error(
                exception.message ?: "Kullanıcılar yüklenirken hata oluştu"
            )
        }
    }

    fun onSearchQueryChanged(query: String) {
        currentSearch = query
        viewModelScope.launch { fetchFilteredUsers() }
    }

    fun onRoleFilterSelected(role: UserRole?) {
        currentRole = role
        viewModelScope.launch { fetchFilteredUsers() }
    }

    fun onActiveFilterToggled(activeOnly: Boolean) {
        currentActiveOnly = activeOnly
        viewModelScope.launch { fetchFilteredUsers() }
    }

    fun toggleUserStatus(user: User) {
        viewModelScope.launch {
            toggleUserStatusUseCase(user.id, !user.isActive)
                .onSuccess {
                    fetchFilteredUsers()
                }
        }
    }
}
