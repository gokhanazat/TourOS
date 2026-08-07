package com.mgacreative.touros.domain.repository

import com.mgacreative.touros.domain.model.User
import com.mgacreative.touros.domain.model.UserRole
import kotlinx.coroutines.flow.StateFlow

/**
 * Supabase Auth ve kullanıcı yönetimi repository arayüzü.
 */
interface AuthRepository {
    suspend fun signUpWithEmail(email: String, password: String, fullName: String): Result<User>
    suspend fun signInWithEmail(email: String, password: String): Result<User>
    suspend fun signOut(): Result<Unit>
    suspend fun getCurrentUser(): User?
    fun observeAuthState(): StateFlow<User?>
    suspend fun refreshSession(): Result<User>
    suspend fun isSessionValid(): Boolean
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    suspend fun resetPassword(newPassword: String): Result<Unit>
    suspend fun resendVerificationEmail(email: String): Result<Unit>
    suspend fun inviteUser(email: String, role: UserRole, fullName: String): Result<Unit>
}
