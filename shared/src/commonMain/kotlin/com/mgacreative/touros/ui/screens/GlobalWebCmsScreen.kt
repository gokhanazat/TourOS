package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mgacreative.touros.domain.repository.AuthRepository
import com.mgacreative.touros.domain.repository.CompanySettingsRepository
import com.mgacreative.touros.domain.model.CompanySettings
import com.mgacreative.touros.domain.model.PromoBannerItem
import com.mgacreative.touros.ui.components.TourOSButton
import com.mgacreative.touros.ui.components.TourOSButtonVariant
import com.mgacreative.touros.ui.components.TourOSTopBar
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.utils.rememberFilePickerLauncher
import org.koin.compose.koinInject
import kotlinx.coroutines.launch

@Composable
fun GlobalWebCmsScreen(
    onNavigateBack: () -> Unit = {}
) {
    val authRepository: AuthRepository = koinInject()
    val companySettingsRepository: CompanySettingsRepository = koinInject()
    val currentUser by authRepository.observeAuthState().collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var selectedTab by remember { mutableStateOf(0) }
    var saveNotification by remember { mutableStateOf<String?>(null) }

    // Form State'leri
    var siteTitle by remember { mutableStateOf("TourOS Business - Lüks Seyahat & Otel Platformu") }
    var heroSlogan by remember { mutableStateOf("Dünyanın En Seçkin 5 Yıldızlı Otelleri ve Özel Tur Paketleri") }
    var headerImageUrl by remember { mutableStateOf("https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=1200") }
    var whatsappNumber by remember { mutableStateOf("+90 532 100 2030") }
    var supportEmail by remember { mutableStateOf("destek@touros.com") }
    var defaultCommissionMargin by remember { mutableStateOf("% 12.5 (B2B Standart Marj)") }
    var agencyReferralCode by remember { mutableStateOf("AGN-MASTER-8492") }
    var metaDescription by remember { mutableStateOf("TourOS B2B ve B2C seyahat platformu üzerinden tur operatörleri ve acentelerin en uygun otel ve uçak tekliflerini karşılaştırın.") }
    var promoBannersList by remember {
        mutableStateOf(
            listOf(
                PromoBannerItem("1", "Голубой тур", "https://images.unsplash.com/photo-1569263979104-865ab7cd8d13?w=1200", null),
                PromoBannerItem("2", "Bodrum Lüks Yat Turu", "https://images.unsplash.com/photo-1540555700478-4be289fbecef?w=1200", "https://touros.com")
            )
        )
    }

    LaunchedEffect(Unit) {
        val tid = currentUser?.tenantId ?: "00000000-0000-0000-0000-000000000001"
        val settings = companySettingsRepository.getCompanySettings(tid).getOrNull()
        if (settings != null) {
            val effBanners = settings.getEffectivePromoBanners()
            if (effBanners.isNotEmpty()) {
                promoBannersList = effBanners
            }
            if (settings.heroSubtitle.isNotBlank()) heroSlogan = settings.heroSubtitle
            if (!settings.headerImageUrl.isNullOrBlank()) headerImageUrl = settings.headerImageUrl
            if (settings.webWhatsapp.isNotBlank()) whatsappNumber = settings.webWhatsapp
            if (settings.webEmail.isNotBlank()) supportEmail = settings.webEmail
            if (!settings.defaultMasterAgencyCode.isNullOrBlank()) agencyReferralCode = settings.defaultMasterAgencyCode
        }
    }

    var activePickingSlideIndex by remember { mutableStateOf<Int?>(null) }

    fun formatFilePickerPath(path: String?): String {
        if (path.isNullOrBlank()) return ""
        val trimmed = path.trim()
        return when {
            trimmed.startsWith("http://") || trimmed.startsWith("https://") || trimmed.startsWith("file://") -> trimmed
            trimmed.length >= 2 && trimmed[1] == ':' -> "file:///" + trimmed.replace("\\", "/")
            trimmed.startsWith("/") -> "file://" + trimmed
            else -> trimmed
        }
    }

    val headerPickerLauncher = rememberFilePickerLauncher(mimeType = "image/*") { fileName, bytes ->
        if (bytes != null && bytes.isNotEmpty()) {
            coroutineScope.launch {
                val tid = currentUser?.tenantId ?: "00000000-0000-0000-0000-000000000001"
                companySettingsRepository.uploadHeaderBanner(tid, bytes, fileName.ifBlank { "header.png" })
                    .onSuccess { url ->
                        headerImageUrl = url
                        saveNotification = "✅ Header görseli Supabase bulut deposuna yüklendi!"
                    }
            }
        } else if (!fileName.isNullOrBlank()) {
            headerImageUrl = formatFilePickerPath(fileName)
        }
    }

    val slidePickerLauncher = rememberFilePickerLauncher(mimeType = "image/*") { fileName, bytes ->
        val idx = activePickingSlideIndex
        if (idx != null) {
            coroutineScope.launch {
                val tid = currentUser?.tenantId ?: "00000000-0000-0000-0000-000000000001"
                val uploadedUrlResult = if (bytes != null && bytes.isNotEmpty()) {
                    companySettingsRepository.uploadPromoBannerImage(tid, bytes, fileName.ifBlank { "promo.png" })
                } else {
                    Result.success(formatFilePickerPath(fileName))
                }
                val publicUrl = uploadedUrlResult.getOrNull()
                if (!publicUrl.isNullOrBlank()) {
                    val list = promoBannersList.toMutableList()
                    if (idx in list.indices) {
                        list[idx] = list[idx].copy(imageUrl = publicUrl)
                        promoBannersList = list
                        saveNotification = "✅ Promosyon görseli Supabase bulut deposuna yüklendi!"
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TourOSTopBar(
                title = "🌐 Ana Web Yönetimi & Canlı Önizleme (CMS)",
                subtitle = "Sistem Yöneticisi Paneli • Sistem Kök Kataloğu ve Web Özelleştirme Engine"
            )
        },
        containerColor = TourOSColors.Background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab Header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = TourOSColors.Surface,
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val tabs = listOf(
                        "🖥️ Canlı Web Önizleme",
                        "🖼️ Manşet & Slider",
                        "🎨 Logo & Açık Tema",
                        "🏷️ Fiyat & Marj",
                        "📞 İletişim & WhatsApp",
                        "🔑 Acente Kodu",
                        "🔍 SEO Bilgileri"
                    )

                    tabs.forEachIndexed { index, title ->
                        val isSelected = selectedTab == index
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) TourOSColors.Primary else Color(0xFFE2E8F0))
                                .clickable { selectedTab = index }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                style = TourOSTypography.BodyMedium.copy(
                                    color = if (isSelected) Color.White else TourOSColors.TextPrimary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
                    }
                }
            }

            if (!saveNotification.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF10B981))
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(saveNotification ?: "", style = TourOSTypography.BodyMedium.copy(color = Color.White, fontWeight = FontWeight.Bold))
                }
            }

            Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                when (selectedTab) {
                    0 -> {
                        // 🖥️ CANLI WEB ÖNİZLEME (LIVE PREVIEW)
                        Box(modifier = Modifier.fillMaxSize()) {
                            GlobalWebPublicScreen(referralCode = agencyReferralCode)
                        }
                    }
                    else -> {
                        // CMS AYAR FORMLARI
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            when (selectedTab) {
                                1 -> { // Manşet & Slider
                                    Text("🖼️ Manşet & Hero Slider Yönetimi", style = TourOSTypography.TitleLarge.copy(fontWeight = FontWeight.Bold))
                                    Text("Web sitesinin ana sayfasında ilk girişte görünecek manşet görselini ve sloganını ayarlayın.", style = TourOSTypography.BodyMedium)

                                    OutlinedTextField(
                                        value = heroSlogan,
                                        onValueChange = { heroSlogan = it },
                                        label = { Text("Hero Slogan Metni") },
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedTextField(
                                            value = headerImageUrl,
                                            onValueChange = { headerImageUrl = it },
                                            label = { Text("Header Banner Görsel URL / Dosya Yolu") },
                                            placeholder = { Text("https://... veya C:/Gorseller/banner.jpg") },
                                            modifier = Modifier.weight(1f)
                                        )
                                        Button(
                                            onClick = { headerPickerLauncher() },
                                            colors = ButtonDefaults.buttonColors(containerColor = TourOSColors.Primary, contentColor = Color.White)
                                        ) {
                                            Text("📁 Dosya Seç", color = Color.White, style = TourOSTypography.BodyMedium.copy(color = Color.White, fontWeight = FontWeight.Bold))
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("🛥️ Sol Promosyon Kartı Slider Listesi (Birden Fazla Banner)", style = TourOSTypography.TitleMedium.copy(fontWeight = FontWeight.Bold))
                                        Button(
                                            onClick = {
                                                val newSlide = PromoBannerItem(
                                                    id = (promoBannersList.size + 1).toString(),
                                                    title = "Yeni Promosyon Turu #${promoBannersList.size + 1}",
                                                    imageUrl = "https://images.unsplash.com/photo-1569263979104-865ab7cd8d13?w=1200",
                                                    targetUrl = null
                                                )
                                                promoBannersList = promoBannersList + newSlide
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = TourOSColors.Success, contentColor = Color.White)
                                        ) {
                                            Text("➕ Yeni Slayt Ekle", color = Color.White, style = TourOSTypography.BodyMedium.copy(color = Color.White, fontWeight = FontWeight.Bold))
                                        }
                                    }

                                    promoBannersList.forEachIndexed { index, slide ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 6.dp),
                                            colors = CardDefaults.cardColors(containerColor = TourOSColors.Surface),
                                            elevation = CardDefaults.cardElevation(2.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(16.dp),
                                                verticalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text("📌 Slayt #${index + 1}", style = TourOSTypography.TitleMedium.copy(fontWeight = FontWeight.Bold))
                                                    if (promoBannersList.size > 1) {
                                                        IconButton(
                                                            onClick = {
                                                                val list = promoBannersList.toMutableList()
                                                                list.removeAt(index)
                                                                promoBannersList = list
                                                            }
                                                        ) {
                                                            Text("🗑️ Sil", color = TourOSColors.Error, style = TourOSTypography.Caption)
                                                        }
                                                    }
                                                }

                                                OutlinedTextField(
                                                    value = slide.title,
                                                    onValueChange = { newTitle ->
                                                        val list = promoBannersList.toMutableList()
                                                        list[index] = list[index].copy(title = newTitle)
                                                        promoBannersList = list
                                                    },
                                                    label = { Text("Promosyon Başlığı (Örn: Mavi Yolculuk / Голубой тур)") },
                                                    modifier = Modifier.fillMaxWidth()
                                                )

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    OutlinedTextField(
                                                        value = slide.imageUrl,
                                                        onValueChange = { newUrl ->
                                                            val list = promoBannersList.toMutableList()
                                                            list[index] = list[index].copy(imageUrl = newUrl)
                                                            promoBannersList = list
                                                        },
                                                        label = { Text("Promosyon Görsel URL / Dosya Yolu") },
                                                        placeholder = { Text("https://... veya C:/Gorseller/tekne.jpg") },
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    Button(
                                                        onClick = {
                                                            activePickingSlideIndex = index
                                                            slidePickerLauncher()
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = TourOSColors.Primary, contentColor = Color.White)
                                                    ) {
                                                        Text("📁 Dosya Seç", color = Color.White, style = TourOSTypography.BodyMedium.copy(color = Color.White, fontWeight = FontWeight.Bold))
                                                    }
                                                }

                                                OutlinedTextField(
                                                    value = slide.targetUrl ?: "",
                                                    onValueChange = { newLink ->
                                                        val list = promoBannersList.toMutableList()
                                                        list[index] = list[index].copy(targetUrl = newLink.ifBlank { null })
                                                        promoBannersList = list
                                                    },
                                                    label = { Text("Tıklanınca Açılacak Hedef Link URL (Örn: https://...)") },
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                            }
                                        }
                                    }
                                }
                                2 -> { // Logo & Açık Tema
                                    Text("🎨 Logo, Banner & Açık Tema Ayarları", style = TourOSTypography.TitleLarge.copy(fontWeight = FontWeight.Bold))
                                    Text("Kurumsal açık temanın (Light Theme) renklerini, logo bağlantılarını ve banner resmi ayarlarını değiştirin.", style = TourOSTypography.BodyMedium)

                                    OutlinedTextField(
                                        value = siteTitle,
                                        onValueChange = { siteTitle = it },
                                        label = { Text("Web Sitesi Başlığı") },
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = headerImageUrl,
                                        onValueChange = { headerImageUrl = it },
                                        label = { Text("Header Arkaplan Banner Görsel URL") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                3 -> { // Fiyat & Marj
                                    Text("🏷️ Fiyatlama & Marj Kuralları", style = TourOSTypography.TitleLarge.copy(fontWeight = FontWeight.Bold))
                                    Text("Web sitesinde gösterilecek turların varsayılan acente ve müşteri kâr marjları.", style = TourOSTypography.BodyMedium)

                                    OutlinedTextField(
                                        value = defaultCommissionMargin,
                                        onValueChange = { defaultCommissionMargin = it },
                                        label = { Text("B2B Varsayılan Komisyon Marjı") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                4 -> { // İletişim & WhatsApp
                                    Text("📞 İletişim & WhatsApp Canlı Destek", style = TourOSTypography.TitleLarge.copy(fontWeight = FontWeight.Bold))
                                    Text("Web sitesindeki hızlı destek hatları ve iletişim bilgileri.", style = TourOSTypography.BodyMedium)

                                    OutlinedTextField(
                                        value = whatsappNumber,
                                        onValueChange = { whatsappNumber = it },
                                        label = { Text("WhatsApp Canlı Destek Numarası") },
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = supportEmail,
                                        onValueChange = { supportEmail = it },
                                        label = { Text("E-Posta Adresi") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                5 -> { // Acente Kodu
                                    Text("🔑 Acente Kodu & Referans Kuralları", style = TourOSTypography.TitleLarge.copy(fontWeight = FontWeight.Bold))
                                    Text("Acentelerin web sitesinden rezervasyon yaparken veya referral link kullanırken kullanacağı varsayılan master kod.", style = TourOSTypography.BodyMedium)

                                    OutlinedTextField(
                                        value = agencyReferralCode,
                                        onValueChange = { agencyReferralCode = it },
                                        label = { Text("Master Acente Referans Kodu") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                6 -> { // SEO Bilgileri
                                    Text("🔍 SEO & Google Meta Bilgileri", style = TourOSTypography.TitleLarge.copy(fontWeight = FontWeight.Bold))
                                    Text("Google arama motoru indekslemesi ve sosyal medya paylaşım kartları.", style = TourOSTypography.BodyMedium)

                                    OutlinedTextField(
                                        value = metaDescription,
                                        onValueChange = { metaDescription = it },
                                        label = { Text("Meta Description (Açıklama)") },
                                        modifier = Modifier.fillMaxWidth(),
                                        minLines = 3
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            TourOSButton(
                                text = "Değişiklikleri Canlı Web Sitesine Kaydet 💾",
                                onClick = {
                                    coroutineScope.launch {
                                        val tid = currentUser?.tenantId ?: "00000000-0000-0000-0000-000000000001"
                                        val currentSettings = companySettingsRepository.getCompanySettings(tid).getOrNull()
                                            ?: CompanySettings(id = tid, name = "TourOS Travels")
                                        
                                        val updatedSettings = currentSettings.copy(
                                            heroSubtitle = heroSlogan,
                                            headerImageUrl = headerImageUrl,
                                            webWhatsapp = whatsappNumber,
                                            webEmail = supportEmail,
                                            defaultMasterAgencyCode = agencyReferralCode,
                                            promoBanners = promoBannersList
                                        )
                                        companySettingsRepository.updateCompanySettings(updatedSettings)
                                        saveNotification = "✅ Slaytlar ve tüm ayarlar başarıyla kaydedildi! Canlı web sitesinde anında güncellendi."
                                    }
                                },
                                variant = TourOSButtonVariant.PRIMARY
                            )
                        }
                    }
                }
            }
        }
    }
}
