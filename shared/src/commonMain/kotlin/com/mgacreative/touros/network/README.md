# Network Katmanı

Uzak veri kaynaklarıyla (Supabase) iletişimi yönetir.

## Yapı

```
network/
├── SupabaseConfig.kt           → expect/actual Supabase URL ve anonKey
├── SupabaseClientProvider.kt   → SupabaseClient factory (singleton)
└── api/                        → API servis sınıfları (Faz 1'de eklenecek)
```

## Kurallar
- `SupabaseConfig` platform-specific `actual` ile implemente edilir.
- Android: `BuildConfig` üzerinden, diğer platformlar: sabit string.
- Tüm API çağrıları `SupabaseClient` üzerinden yapılır.
- Ktor engine'i platform bazlı (OkHttp/Darwin/CIO/JS) Koin tarafından sağlanır.
