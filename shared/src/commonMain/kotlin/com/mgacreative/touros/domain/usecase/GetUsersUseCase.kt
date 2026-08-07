package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.model.User
import com.mgacreative.touros.domain.model.UserRole
import com.mgacreative.touros.domain.repository.UserRepository

class GetUsersUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(
        tenantId: String,
        searchQuery: String = "",
        roleFilter: UserRole? = null,
        activeOnly: Boolean = false
    ): Result<List<User>> {
        return userRepository.getUsersForTenant(tenantId).map { users ->
            users.filter { user ->
                val matchesSearch = searchQuery.isBlank() ||
                        user.fullName.contains(searchQuery, ignoreCase = true) ||
                        user.email.contains(searchQuery, ignoreCase = true)

                val matchesRole = roleFilter == null || user.role == roleFilter
                val matchesActive = !activeOnly || user.isActive

                matchesSearch && matchesRole && matchesActive
            }
        }
    }
}
