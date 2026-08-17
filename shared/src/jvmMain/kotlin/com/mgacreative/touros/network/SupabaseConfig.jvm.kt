package com.mgacreative.touros.network

/**
 * JVM/Desktop: Supabase yapılandırması.
 */
actual object SupabaseConfig {
    actual val url: String = System.getenv("SUPABASE_URL") ?: "https://api.axileto.com"
    actual val anonKey: String = System.getenv("SUPABASE_ANON_KEY") ?: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlIjoiYW5vbiIsImlzcyI6InN1cGFiYXNlIiwiaWF0IjoxNzg2OTYyODU2LCJleHAiOjIxMDIzMjI4NTZ9.HzmIV6ONPXLRXSVkT1NdcLpKxf6DP_DqImuX0o8-8Lc"
}
