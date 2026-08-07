package com.mgacreative.touros.network

/**
 * Platform-specific Supabase yapılandırması.
 * Her platform kendi ortam değişkenlerinden URL ve anonKey okur.
 */
expect object SupabaseConfig {
    val url: String
    val anonKey: String
}
