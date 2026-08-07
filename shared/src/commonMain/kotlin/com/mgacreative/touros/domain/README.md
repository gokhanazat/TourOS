# Domain Katmanı

İş mantığı kurallarını ve domain entity'lerini barındırır. Hiçbir framework bağımlılığı yoktur (pure Kotlin).

## Yapı

```
domain/
├── model/          → Domain entity'ler (User, Tenant, UserRole vb.)
├── repository/     → Repository interface'leri (Faz 1'de eklenecek)
└── usecase/        → İş mantığı use case'leri (Faz 1'de eklenecek)
```

## Kurallar
- Sadece Kotlin stdlib ve kotlinx-serialization kullanılır.
- Diğer katmanlara (data, network, ui) bağımlılık **yoktur**.
- Entity'ler `@Serializable` ile işaretlenir (DTO dönüşümü için).
