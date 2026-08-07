package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.repository.UserRepository

class ToggleUserStatusUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String, newIsActive: Boolean): Result<Unit> {
        if (userId.isBlank()) {
            return Result.failure(IllegalArgumentException("Kullanıcı ID boş olamaz"))
        }
        return userRepository.toggleUserActiveStatus(userId, newIsActive)
    }
}
