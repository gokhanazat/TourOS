package com.mgacreative.touros.data.cache

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.TimeMark
import kotlin.time.TimeSource

@Serializable
data class SystemCacheConfig(
    val id: String = "GLOBAL_CACHE_CONFIG",
    val tenant_id: String = "00000000-0000-0000-0000-000000000001",
    val is_caching_enabled: Boolean = true,
    val price_ttl_minutes: Int = 15,
    val catalog_ttl_hours: Int = 24,
    val auto_flush_on_price_change: Boolean = true,
    val enabled_providers: List<String> = listOf("Coral Travel", "Pegas Touristik", "Anex Tour", "Travelata", "SunExpress", "Paximum", "Amadeus"),
    val total_requests_served: Long = 0L,
    val total_cache_hits: Long = 0L,
    val last_flushed_at: String = ""
) {
    val hitRatePercent: Int
        get() = if (total_requests_served > 0) ((total_cache_hits.toDouble() / total_requests_served) * 100).toInt() else 0
}

data class CacheItem(
    val key: String,
    val provider: String,
    val category: String,
    val jsonPayload: String,
    val expireMark: TimeMark,
    var hitCount: Int = 1
)

class SystemCacheManager(
    private val supabaseClient: SupabaseClient
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val timeSource = TimeSource.Monotonic
    private val _config = MutableStateFlow(SystemCacheConfig())
    val config: StateFlow<SystemCacheConfig> = _config.asStateFlow()

    private val memoryCache = mutableMapOf<String, CacheItem>()

    init {
        loadRemoteConfig()
    }

    private fun loadRemoteConfig() {
        scope.launch {
            try {
                val remote = supabaseClient.postgrest["system_cache_settings"]
                    .select {
                        filter {
                            eq("id", "GLOBAL_CACHE_CONFIG")
                        }
                    }
                    .decodeSingleOrNull<SystemCacheConfig>()

                if (remote != null) {
                    _config.value = remote
                }
            } catch (_: Exception) {
                // Keep default configuration if offline
            }
        }
    }

    fun updateConfig(updated: SystemCacheConfig, onComplete: ((Boolean) -> Unit)? = null) {
        _config.value = updated
        scope.launch {
            try {
                supabaseClient.postgrest["system_cache_settings"]
                    .upsert(updated)
                onComplete?.invoke(true)
            } catch (_: Exception) {
                onComplete?.invoke(false)
            }
        }
    }

    fun flushAllCache(onComplete: ((Int) -> Unit)? = null) {
        val count = memoryCache.size
        memoryCache.clear()
        val current = _config.value
        val updated = current.copy(last_flushed_at = "Bugün ${current.price_ttl_minutes}m")
        _config.value = updated

        scope.launch {
            try {
                supabaseClient.postgrest["system_cache_settings"].upsert(updated)
                supabaseClient.postgrest["system_cache_entries"].delete {
                    filter {
                        neq("cache_key", "DUMMY")
                    }
                }
                onComplete?.invoke(count.coerceAtLeast(1))
            } catch (_: Exception) {
                onComplete?.invoke(count)
            }
        }
    }

    fun get(key: String): String? {
        val currentCfg = _config.value
        if (!currentCfg.is_caching_enabled) return null

        val item = memoryCache[key] ?: return null
        if (item.expireMark.hasPassedNow()) {
            memoryCache.remove(key)
            return null
        }

        item.hitCount++
        recordHit()
        return item.jsonPayload
    }

    fun put(key: String, category: String, provider: String, payload: String, ttlMinutes: Int? = null) {
        val currentCfg = _config.value
        if (!currentCfg.is_caching_enabled) return

        val duration = if (ttlMinutes != null) {
            ttlMinutes.minutes
        } else when (category) {
            "PRICE" -> currentCfg.price_ttl_minutes.minutes
            else -> currentCfg.catalog_ttl_hours.hours
        }

        memoryCache[key] = CacheItem(
            key = key,
            provider = provider,
            category = category,
            jsonPayload = payload,
            expireMark = timeSource.markNow() + duration
        )
    }

    private fun recordHit() {
        val cur = _config.value
        _config.value = cur.copy(
            total_requests_served = cur.total_requests_served + 1,
            total_cache_hits = cur.total_cache_hits + 1
        )
    }

    fun getMemoryItemCount(): Int = memoryCache.size
}
