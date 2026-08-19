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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

import com.mgacreative.touros.data.database.entity.CompanyEntity
import com.mgacreative.touros.data.database.entity.UserEntity

class AuthRepositoryImpl(
    private val supabaseClient: SupabaseClient
) : AuthRepository {

    private val auth = supabaseClient.auth
    private val _currentUserState = MutableStateFlow<User?>(null)
    private val scope = CoroutineScope(Dispatchers.Default)

    init {
        scope.launch {
            try {
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
            } catch (e: Throwable) {
                _currentUserState.value = null
            }
        }
    }

    private fun mapUserInfoToUser(userInfo: io.github.jan.supabase.auth.user.UserInfo?, fallbackEmail: String = ""): User {
        val userEmail = (userInfo?.email ?: fallbackEmail).lowercase().trim()
        val rawRole = userInfo?.userMetadata?.get("role")?.toString()?.replace("\"", "")
            ?: userInfo?.appMetadata?.get("role")?.toString()?.replace("\"", "")

        val isAdminUser = userEmail == "gkhnazat@gmail.com"

        val role = when {
            isAdminUser -> UserRole.SYSTEM_ADMIN
            rawRole.equals("AGENT", ignoreCase = true) || rawRole.equals("AGENCY", ignoreCase = true) -> UserRole.AGENT
            rawRole.equals("TOUR_OPERATOR", ignoreCase = true) || rawRole.equals("OPERATOR", ignoreCase = true) -> UserRole.TOUR_OPERATOR
            else -> UserRole.AGENT
        }

        val tenantId = if (isAdminUser) {
            "00000000-0000-0000-0000-000000000001"
        } else {
            userInfo?.userMetadata?.get("tenant_id")?.toString()?.replace("\"", "")
                ?: userInfo?.appMetadata?.get("tenant_id")?.toString()?.replace("\"", "")
        }

        return User(
            id = userInfo?.id ?: "",
            email = userInfo?.email ?: fallbackEmail,
            fullName = userInfo?.userMetadata?.get("full_name")?.toString()?.replace("\"", "") ?: if (isAdminUser) "Sistem Yöneticisi (Super Admin)" else "Acente Kullanıcısı",
            role = role,
            tenantId = tenantId?.takeIf { it.isNotBlank() && it != "tenant_id" },
            isEmailVerified = userInfo?.emailConfirmedAt != null
        )
    }

    private val postgrest = supabaseClient.postgrest

    override suspend fun signUpWithEmail(
        email: String,
        password: String,
        fullName: String
    ): Result<User> {
        return runCatching {
            val cleanEmail = email.trim().lowercase()
            val cleanName = fullName.trim()

            val userMetadata = buildJsonObject {
                put("full_name", cleanName)
                put("role", "AGENT")
            }

            try {
                auth.signUpWith(Email) {
                    this.email = cleanEmail
                    this.password = password
                    this.data = userMetadata
                }
            } catch (e: Throwable) {
                // Sunucuda mailer / SMTP hatası alınırsa doğrudan güvenli veritabanı RPC'si ile kaydı tamamla
                val params = buildJsonObject {
                    put("p_email", cleanEmail)
                    put("p_full_name", cleanName)
                    put("p_password", password)
                }
                postgrest.rpc("register_new_user", params)
            }

            // Yeni kaydolan acente onay beklediği için oturumu kapat
            auth.signOut()

            User(
                id = "",
                email = cleanEmail,
                fullName = cleanName,
                role = UserRole.AGENT,
                tenantId = null,
                isEmailVerified = false
            )
        }
    }

    override suspend fun signInWithEmail(
        email: String,
        password: String,
        agencyCode: String
    ): Result<User> {
        return runCatching {
            val cleanEmail = email.trim().lowercase()
            val cleanCode = agencyCode.trim().uppercase()

            auth.signInWith(Email) {
                this.email = cleanEmail
                this.password = password
            }
            val userInfo = auth.currentUserOrNull()
                ?: throw IllegalStateException("Kullanıcı oturumu alınamadı.")

            val isAdmin = cleanEmail == "gkhnazat@gmail.com"
            if (isAdmin) {
                return@runCatching mapUserInfoToUser(userInfo, fallbackEmail = cleanEmail)
            }

            // Normal Acente Giriş Kontrolleri:
            if (cleanCode.isBlank()) {
                auth.signOut()
                throw IllegalArgumentException("Acente girişi için 'Acente Kodu' zorunludur.")
            }

            // 1. Kullanıcı kaydı kontrolü
            val userRecord = postgrest.from("users")
                .select {
                    filter {
                        eq("auth_id", userInfo.id)
                    }
                }
                .decodeSingleOrNull<UserEntity>()

            val tenantId = userRecord?.tenantId
                ?: userInfo.userMetadata?.get("tenant_id")?.toString()?.replace("\"", "")

            if (tenantId.isNullOrBlank()) {
                auth.signOut()
                throw IllegalStateException("Acente şirket kaydınız bulunamadı. Lütfen sistem yöneticisiyle iletişime geçin.")
            }

            // 2. Şirket onay ve acente kodu kontrolü
            val companyRecord = postgrest.from("companies")
                .select {
                    filter {
                        eq("id", tenantId)
                    }
                }
                .decodeSingleOrNull<CompanyEntity>()

            if (companyRecord == null || !companyRecord.isActive || userRecord?.isActive == false) {
                auth.signOut()
                throw IllegalStateException("Acente başvurunuz henüz sistem yöneticisi tarafından onaylanmamıştır.")
            }

            val registeredCode = (companyRecord.defaultMasterAgencyCode ?: companyRecord.operatorCode ?: "").trim().uppercase()
            if (registeredCode.isBlank() || registeredCode != cleanCode) {
                auth.signOut()
                throw IllegalArgumentException("Acente Kodu hatalı! Lütfen yöneticiniz tarafından tanımlanan acente kodunu giriniz.")
            }

            mapUserInfoToUser(userInfo, fallbackEmail = cleanEmail)
        }
    }

    override suspend fun signOut(): Result<Unit> {
        return runCatching {
            auth.signOut()
            _currentUserState.value = null
        }
    }

    override suspend fun getCurrentUser(): User? {
        runCatching { auth.sessionStatus.first { it !is SessionStatus.Initializing } }
        val userInfo = auth.currentUserOrNull() ?: return null
        return mapUserInfoToUser(userInfo)
    }

    override fun observeAuthState(): StateFlow<User?> {
        return _currentUserState.asStateFlow()
    }

    override suspend fun refreshSession(): Result<User> {
        return runCatching {
            auth.sessionStatus.first { it !is SessionStatus.Initializing }
            auth.refreshCurrentSession()
            val userInfo = auth.currentUserOrNull()
                ?: throw IllegalStateException("Oturum yenilenemedi")
            mapUserInfoToUser(userInfo)
        }
    }

    override suspend fun isSessionValid(): Boolean {
        runCatching { auth.sessionStatus.first { it !is SessionStatus.Initializing } }
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
