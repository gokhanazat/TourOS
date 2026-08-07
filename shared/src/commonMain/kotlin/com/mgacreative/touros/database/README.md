# Database Katmanı

Yerel önbellek ve offline veri yönetimi.

## Yapı

```
database/
├── cache/      → Offline cache stratejileri
└── ...
```

## Kurallar
- Room kullanılmaz (iptal edildi).
- Supabase + StateFlow ile veri yönetimi yapılır.
- Gerekirse key-value store (DataStore/Settings) kullanılabilir.
- Offline-first stratejisi ileride Faz 2+'da eklenecek.
