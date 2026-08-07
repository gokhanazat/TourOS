# DI (Dependency Injection) Katmanı

Koin tabanlı bağımlılık enjeksiyonu yapılandırması.

## Yapı

```
di/
├── AppModule.kt      → Tüm shared Koin modülleri (network, repository, useCase, viewModel)
├── PlatformModule.kt → expect/actual platform-specific modüller
└── KoinInit.kt       → Koin başlatma fonksiyonu (tüm platform'lardan çağrılır)
```

## Kullanım
```kotlin
// Android (MainActivity veya Application)
initKoin { androidContext(this@TourOSApp) }

// iOS / Desktop / Web
initKoin()
```

## Kurallar
- Her yeni katman kendi Koin modülünü `AppModule.kt`'ye ekler.
- Platform-specific bağımlılıklar `PlatformModule`'de `actual` olarak tanımlanır.
- ViewModel'ler `koinViewModel()` ile Compose'da inject edilir.
