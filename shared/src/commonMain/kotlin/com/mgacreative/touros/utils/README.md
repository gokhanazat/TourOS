# Utils Katmanı

Ortak yardımcı fonksiyonlar ve extension'lar.

## Yapı

```
utils/
├── Extensions.kt       → Genel Kotlin extension fonksiyonları
├── DateUtils.kt         → Tarih/saat yardımcıları
├── ValidationUtils.kt   → Form validasyon fonksiyonları
└── Constants.kt         → Uygulama sabitleri
```

## Kurallar
- Platform-bağımsız (common) yardımcı fonksiyonlar burada tanımlanır.
- Platform-specific yardımcılar ilgili source set'te (androidMain, iosMain vb.) bulunur.
- Extension fonksiyonları `fun Type.extensionName()` formatında yazılır.
