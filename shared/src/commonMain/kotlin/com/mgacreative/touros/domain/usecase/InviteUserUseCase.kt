package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.model.UserRole
import com.mgacreative.touros.domain.repository.AuthRepository

class InviteUserUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        email: String,
        role: UserRole,
        fullName: String
    ): Result<Unit> {
        if (email.isBlank()) {
            return Result.failure(IllegalArgumentException("E-posta adresi boş olamaz"))
        }
        if (fullName.isBlank()) {
            return Result.failure(IllegalArgumentException("Ad Soyad boş olamaz"))
        }
        return authRepository.inviteUser(
            email = email.trim(),
            role = role,
            fullName = fullName.trim()
        )
    }
}
