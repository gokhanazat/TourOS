# PROJECT SPECIFIC RULES & KESİN KURALLAR

## 1. TEMEL KURALLAR (CORE RULES)
1. **Prompt Kapsamı:** Sadece verilen prompt isteklerine odaklan. İstek dışındaki hiçbir kod/dosyayı değiştirme ve silme.
2. **Onay Mekanizması:** Sormadan ve kullanıcı onayı almadan hiçbir şeyi/kodu/bileşeni/dosyayı kaldırma.
3. **Netleştirme:** Anlaşılmayan veya belirsiz olan yerlerde tahmin yürütme, doğrudan kullanıcıya sor.
4. **SQL Çıktısı:** Her işlem/prompt sonrası veritabanını ilgilendiren bir değişiklik/yapı varsa SQL kodunu eksiksiz ver.
5. **Görsel Tutarlılık:** Web, Desktop, Android ve iOS platformlarında aynı tasarım dilini, tema ve bileşen yapısını birebir koru.
6. **Soru / Fikir Modu:** "soru" veya "düşünüyorum" notu ile paylaşılan mesajlarda kesinlikle kod yazma ve dosya değiştirme; sadece analiz et ve yanıtla.
7. **Sorun Odak Modu:** "Sorun" ibaresi ile başlayan istemlerde tam odaklanarak derinlemesine kök neden analizi ve çözüm üret.
8. **Veritabanı Mimarisi:** Database Yandex Cloud üzerinde tutuluyor ve oradan Supabase'e çekiliyor; veri akışını buna göre koru.

---

## 2. KESİN KISITLAMA: SEARCH (ARAMA) MODÜLÜ KİLİDİ
1. **Search Dokunulmazlığı:** "Search" (Arama) ile ilgili hiçbir dosyayı, sınıfı, bileşeni veya fonksiyonu (UI, ViewModel, Repository, UseCase vb.) **KESİNLİKLE DEĞİŞTİRME, YENİDEN YAZMA, TAŞIMA VEYA SİLME**.
2. **Kilitli Kod Koruması:** Search modülü/kodları kilitlidir. Bu alanla ilgili en ufak bir değişiklik gerekse dahi kod yazma, sadece açıkça nedenini belirt ve kullanıcıdan **ONAY** bekle.
3. **Kapsam Sınırı:** Çalışmanı yalnızca promptta açıkça belirtilen dosya ve görev kapsamı ile sınırla.
