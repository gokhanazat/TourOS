# UI Katmanı

Compose Multiplatform arayüz bileşenleri, tema ve navigasyon.

## Yapı

```
ui/
├── theme/
│   ├── Color.kt        → TourOS renk paleti (Deep Teal / Amber / Coral)
│   ├── Type.kt         → Material 3 tipografi
│   ├── Theme.kt        → TourOSTheme composable (dark/light)
│   └── WindowSize.kt   → Adaptive breakpoint yardımcıları
├── navigation/
│   ├── Routes.kt           → @Serializable type-safe route'lar
│   ├── AppNavigation.kt    → NavHost grafiği
│   └── NavigationItems.kt  → Rol bazlı menü öğeleri
├── screens/
│   ├── SplashScreen.kt
│   ├── LoginScreen.kt
│   └── DashboardScreen.kt
└── components/             → Paylaşılan UI bileşenleri (Faz 1'de eklenecek)
```

## Adaptive UI Breakpoint'ler
| Sınıf | Genişlik | Navigasyon | Düzen |
|---|---|---|---|
| Compact | < 600dp | Bottom Nav | Tek panel |
| Medium | 600-839dp | Navigation Rail | Çift panel |
| Expanded | ≥ 840dp | Permanent Drawer | Çift panel |

## Kurallar
- Tüm composable'lar `TourOSTheme` içinde çalışır.
- Navigasyon type-safe route'lar ile yapılır (`@Serializable`).
- ViewModel'ler `koinViewModel()` ile inject edilir.
- Rol bazlı erişim `NavigationItems.kt`'deki `allowedRoles` ile kontrol edilir.
