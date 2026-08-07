# Models (DTO) Katmanı

Data Transfer Object'ler — ağ/veritabanı ile domain arasında veri taşıma.

## Yapı

```
models/
└── dto/    → Supabase tablo yapılarıyla eşleşen DTO sınıfları
```

## Kurallar
- Her DTO `@Serializable` ile işaretlenir.
- DTO'lar domain model'lere `.toDomain()` extension ile dönüştürülür.
- Supabase tablo adları ile `@SerialName` annotation'ı uyumlu olmalıdır.
