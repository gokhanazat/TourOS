package com.mgacreative.touros.data.repository

import com.mgacreative.touros.domain.model.User
import com.mgacreative.touros.domain.model.UserRole
import com.mgacreative.touros.domain.repository.AuthRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class AuthRepositoryImpl(
    private val supabaseClient: SupabaseClient
) : AuthRepository {

    private val auth = supabaseClient.auth
    private val _currentUserState = MutableStateFlow<User?>(null)
    private val scope = CoroutineScope(Dispatchers.Default)

    init {
        scope.launch {
            auth.sessionStatus.collect { status ->
                when (status) {
                    is SessionStatus.Authenticated -> {
                        _currentUserState.value = mapUserInfoToUser(status.session.user)
                    }
                    is SessionStatus.NotAuthenticated -> {
                        _currentUserState.value = null
                    }
                    else -> {}
                }
            }
        }
    }

    private fun mapUserInfoToUser(userInfo: io.github.jan.supabase.auth.user.UserInfo?, fallbackEmail: String = ""): User {
        val tenantId = userInfo?.userMetadata?.get("tenant_id")?.toString()?.replace("\"", "")
            ?: userInfo?.appMetadata?.get("tenant_id")?.toString()?.replace("\"", "")
        return User(
            id = userInfo?.id ?: "",
            email = userInfo?.email ?: fallbackEmail,
            fullName = userInfo?.userMetadata?.get("full_name")?.toString()?.replace("\"", "") ?: "",
            role = UserRole.CUSTOMER,
            tenantId = tenantId?.takeIf { it.isNotBlank() && it != "tenant_id" },
            isEmailVerified = userInfo?.emailConfirmedAt != null
        )
    }

    override suspend fun signUpWithEmail(
        email: String,
        password: String,
        fullName: String
    ): Result<User> {
        return runCatching {
            val userMetadata = buildJsonObject {
                put("full_name", fullName)
            }
            auth.signUpWith(Email) {
                this.email = email
                this.password = password
                this.data = userMetadata
            }
            val userInfo = auth.currentUserOrNull()
            mapUserInfoToUser(userInfo, fallbackEmail = email).copy(fullName = fullName)
        }
    }

    override suspend fun signInWithEmail(
        email: String,
        password: String
    ): Result<User> {
        return runCatching {
            auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            val userInfo = auth.currentUserOrNull()
                ?: throw IllegalStateException("Kullanıcı oturumu alınamadı")

            mapUserInfoToUser(userInfo, fallbackEmail = email)
        }
    }

    override suspend fun signOut(): Result<Unit> {
        return runCatching {
            auth.signOut()
            _currentUserState.value = null
        }
    }

    override suspend fun getCurrentUser(): User? {
        val userInfo = auth.currentUserOrNull() ?: return null
        return mapUserInfoToUser(userInfo)
    }

    override fun observeAuthState(): StateFlow<User?> {
        return _currentUserState.asStateFlow()
    }

    override suspend fun refreshSession(): Result<User> {
        return runCatching {
            auth.refreshCurrentSession()
            val userInfo = auth.currentUserOrNull()
                ?: throw IllegalStateException("Oturum yenilenemedi")
            mapUserInfoToUser(userInfo)
        }
    }

    override suspend fun isSessionValid(): Boolean {
        val session = auth.currentSessionOrNull() ?: return false
        return session.user != null
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return runCatching {
            auth.resetPasswordForEmail(email)
        }
    }

    override suspend fun resetPassword(newPassword: String): Result<Unit> {
        return runCatching {
            auth.updateUser {
                password = newPassword
            }
        }
    }

    override suspend fun resendVerificationEmail(email: String): Result<Unit> {
        return runCatching {
            auth.resendEmail(OtpType.Email.SIGNUP, email)
        }
    }

    override suspend fun inviteUser(
        email: String,
        role: UserRole,
        fullName: String
    ): Result<Unit> {
        return runCatching {
            val params = buildJsonObject {
                put("p_email", email)
                put("p_full_name", fullName)
                put("p_role", role.name)
            }
            supabaseClient.postgrest.rpc(
                function = "invite_user_to_tenant",
                parameters = params
            )
        }
    }
}
