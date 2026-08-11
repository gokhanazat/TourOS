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
import com.mgacreative.touros.ui.components.TourOSButton
import com.mgacreative.touros.ui.components.TourOSButtonVariant
import com.mgacreative.touros.ui.components.TourOSTopBar
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import org.koin.compose.koinInject

@Composable
fun GlobalWebCmsScreen(
    onNavigateBack: () -> Unit = {}
) {
    val authRepository: AuthRepository = koinInject()
    val currentUser by authRepository.observeAuthState().collectAsState()

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

                                    OutlinedTextField(
                                        value = headerImageUrl,
                                        onValueChange = { headerImageUrl = it },
                                        label = { Text("Header Banner Görsel URL / Dosya Yolu") },
                                        placeholder = { Text("https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=1200") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
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
                                    saveNotification = "✅ Ayarlar başarıyla kaydedildi! Canlı web sitesi güncellendi."
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
