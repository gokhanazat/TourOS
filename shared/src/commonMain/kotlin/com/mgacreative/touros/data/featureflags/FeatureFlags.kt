package com.mgacreative.touros.data.featureflags

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Serializable
data class AppFeatureFlagDto(
    @SerialName("flag_key") val flagKey: String = "",
    @SerialName("is_enabled") val isEnabled: Boolean = false,
    val description: String? = null
)

/**
 * public.app_feature_flags tablosundaki aç/kapa ayarları.
 * Ayarlar okunamazsa güvenli varsayılan kullanılır: uçuş sekmesi KAPALI.
 */
object FeatureFlags {
    const val FLIGHT_SEARCH_TAB = "flight_search_tab"
    const val TOUR_ACTUALIZATION = "tour_actualization"

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var loaded = false

    private val _flags = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val flags: StateFlow<Map<String, Boolean>> = _flags.asStateFlow()

    private val _all = MutableStateFlow<List<AppFeatureFlagDto>>(emptyList())
    val all: StateFlow<List<AppFeatureFlagDto>> = _all.asStateFlow()

    fun isEnabled(key: String, map: Map<String, Boolean> = _flags.value): Boolean = map[key] ?: false

    /** Uygulama ömründe bir kez yükler (force = true ile yeniden). */
    fun ensureLoaded(client: SupabaseClient, force: Boolean = false) {
        if (loaded && !force) return
        loaded = true
        scope.launch { refresh(client) }
    }

    suspend fun refresh(client: SupabaseClient) {
        runCatching {
            client.postgrest["app_feature_flags"].select().decodeList<AppFeatureFlagDto>()
        }.onSuccess { list ->
            _all.value = list.sortedBy { it.flagKey }
            _flags.value = list.associate { it.flagKey to it.isEnabled }
        }.onFailure {
            loaded = false // bir sonraki ekranda tekrar denensin
        }
    }

    /** Sadece süper admin başarılı olur (RPC içinde kontrol edilir). */
    suspend fun setEnabled(client: SupabaseClient, key: String, enabled: Boolean): Result<Unit> = runCatching {
        val params = buildJsonObject {
            put("p_flag_key", key)
            put("p_enabled", enabled)
        }
        client.postgrest.rpc("set_app_feature_flag", params)
        refresh(client)
    }
}
