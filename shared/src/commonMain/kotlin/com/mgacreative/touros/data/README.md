# Data Katmanı

Repository implementasyonları ve veri kaynağı yönetimi.

## Yapı

```
data/
├── repository/     → Repository implementasyonları (domain interface'lerini implemente eder)
└── datasource/     → Remote/Local veri kaynağı sınıfları
```

## Kurallar
- Domain katmanındaki repository interface'lerini implemente eder.
- Supabase API çağrıları burada yapılır.
- DTO ↔ Domain model dönüşümleri burada gerçekleşir.
- Koin üzerinden DI ile inject edilir.
