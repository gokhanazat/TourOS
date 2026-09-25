# KESİN PROJE KURALLARI VE ÇALIŞMA PROTOKOLÜ

## 1. TEMEL ÇALIŞMA KURALLARI
- **Kapsam Sınırı:** Sadece promptta açıkça belirtilen dosya ve kapsam içinde çalış. Projenin başka hiçbir yerini değiştirme, silme veya yeniden yapılandırma.
- **Silme/Kaldırma Yasağı:** Benden açık onay almadan hiçbir dosyayı, sınıfı, fonksiyonu veya kod bloğunu projeden kaldırma/silme.
- **Netlik:** Anlamadığın, eksik veya çelişkili gördüğün yerlerde varsayım yapma; doğrudan bana sor ve açıklama bekle.
- **Tasarım Bütünlüğü:** Görsel dil Web, Desktop, Android ve iOS platformlarında birebir aynı ve tutarlı olmalıdır.
- **Özel İstem Etiketleri:**
  - `SORUnotu`: Bu ibareyi içeren mesajlarda KESİNLİKLE kod yazma ve mevcut kodu değiştirme; sadece soruyu açıkla/yanıtla.
  - `SORUN`: Bu ibare ile başlayan mesajlarda tüm odağını belirtilen probleme/hataya ver ve çözüme odaklan.

## 2. ALTYAPI VE VERİTABANI KULLANIMI
- **Yandex Database:** Veritabanı Yandex üzerinde tutulmaktadır. Her işlem/prompt sonrasında yeni veya güncellenen bir SQL varsa bunu Yandex üzerinde çalıştırılacak formatta sun/yürüt.
- **Supabase Sınırı:** Supabase tarafında HİÇBİR fonksiyon (Edge Functions, Database Functions vb.) etkin DEĞİLDİR; backend mantığını buna göre kur.

## 3. KİLİTLİ ALANLAR VE KISITLAMALAR (SEARCH MODÜLÜ)
- **Search Dokunulmazlığı:** "Search" (Arama) ile ilgili hiçbir dosyayı, sınıfı, bileşeni veya fonksiyonu (UI, ViewModel, Repository, UseCase vb.) KESİNLİKLE DEĞİŞTİRME, YENİDEN YAZMA, TAŞIMA veya SİLME.
- **Arama Değişiklik Talepleri:** Search modülü tamamen kilitlidir. Bu alanla ilgili zorunlu bir değişiklik gerekirse KOD YAZMA; değişikliğin tam nedenini açıkla ve benden açık ONAY bekle.
