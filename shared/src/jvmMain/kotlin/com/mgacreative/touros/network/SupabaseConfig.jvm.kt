package com.mgacreative.touros.network

/**
 * JVM/Desktop: Supabase yapılandırması.
 */
actual object SupabaseConfig {
    actual val url: String = System.getenv("SUPABASE_URL") ?: "https://yakhexsbjzszxyuyuwzz.supabase.co"
    actual val anonKey: String = System.getenv("SUPABASE_ANON_KEY") ?: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Inlha2hleHNianpzenh5dXl1d3p6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODUyNTY4MjIsImV4cCI6MjEwMDgzMjgyMn0.UaWe9PoIFE_JDp9IKJYaLyU0jJ03xZvn-9a9stSxm-A"
}
