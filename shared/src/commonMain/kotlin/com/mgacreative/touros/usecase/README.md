# UseCase Katmanı

İş mantığı operasyonlarını kapsüller. Her use case tek bir işi yapar.

## Yapı

```
usecase/
├── auth/       → Login, Register, Logout vb.
├── tour/       → Tur CRUD operasyonları
├── booking/    → Rezervasyon operasyonları
└── ...
```

## Kurallar
- Her use case tek bir public `invoke()` operatörüne sahiptir.
- Repository interface'lerini inject eder (domain katmanından).
- UI katmanı use case'leri ViewModel üzerinden çağırır.
- Coroutine suspend fonksiyonları kullanılır.
