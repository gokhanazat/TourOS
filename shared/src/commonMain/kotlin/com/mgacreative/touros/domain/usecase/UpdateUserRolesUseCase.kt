package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.model.UserRole
import com.mgacreative.touros.domain.repository.UserRepository

class UpdateUserRolesUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String, newRoles: List<UserRole>): Result<Unit> {
        return userRepository.updateUserRoles(userId, newRoles)
    }
}
