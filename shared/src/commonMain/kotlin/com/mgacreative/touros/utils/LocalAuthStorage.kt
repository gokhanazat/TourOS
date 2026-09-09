package com.mgacreative.touros.utils

import kotlinx.serialization.Serializable

@Serializable
data class SavedAuthCredentials(
    val email: String = "",
    val password: String = "",
    val agencyCode: String = "",
    val rememberMe: Boolean = false
)

expect object LocalAuthStorage {
    fun saveCredentials(credentials: SavedAuthCredentials)
    fun loadCredentials(): SavedAuthCredentials?
    fun clearCredentials()
}
