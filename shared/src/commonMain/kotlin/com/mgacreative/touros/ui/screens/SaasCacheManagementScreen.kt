package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.sp
import com.mgacreative.touros.data.cache.SystemCacheManager
import com.mgacreative.touros.ui.components.TourOSButton
import com.mgacreative.touros.ui.components.TourOSButtonVariant
import com.mgacreative.touros.ui.components.TourOSTopBar
import com.mgacreative.touros.ui.localization.AppLanguageManager
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSTypography
import org.koin.compose.koinInject

/**
 * 👑 SAAS ADMİN PANELİ: Önbellek & API Performansı Yönetim Masası.
 * Tüm dış operatör API'lerinin önbellek süreleri (TTL), anlık tasarruf oranları ve cache temizliği buradan yönetilir.
 */
@Composable
fun SaasCacheManagementScreen(
    onNavigateBack: () -> Unit = {},
    systemCacheManager: SystemCacheManager = koinInject()
) {
    val cacheConfig by systemCacheManager.config.collectAsState()
    var saveNotification by remember { mutableStateOf<String?>(null) }

    var isCachingEnabled by remember(cacheConfig) { mutableStateOf(cacheConfig.is_caching_enabled) }
    var priceTtlMinutes by remember(cacheConfig) { mutableStateOf(cacheConfig.price_ttl_minutes) }
    var catalogTtlHours by remember(cacheConfig) { mutableStateOf(cacheConfig.catalog_ttl_hours) }
    var autoFlushOnPriceChange by remember(cacheConfig) { mutableStateOf(cacheConfig.auto_flush_on_price_change) }
    var enabledProviders by remember(cacheConfig) { mutableStateOf(cacheConfig.enabled_providers.toSet()) }

    Scaffold(
        topBar = {
            TourOSTopBar(
                title = AppLanguageManager.translate("👑 SaaS Admin - Önbellek & API Performansı (Caching)"),
                subtitle = AppLanguageManager.translate("Merkezi Dış API Sorgu Kotası, TTL Süreleri, Önbellek & Performans Yönetimi")
            )
        },
        containerColor = TourOSColors.Background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            if (!saveNotification.isNullOrBlank()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF10B981),
                    shadowElevation = 2.dp
                ) {
                    Text(
                        text = saveNotification ?: "",
                        modifier = Modifier.padding(14.dp),
                        style = TourOSTypography.BodyMedium.copy(color = Color.White, fontWeight = FontWeight.Bold)
                    )
                }
            }

            // 1. Üst Başlık & Kaydet / Flush Butonları
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = TourOSColors.Surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "⚡ Önbellek & API Performans Yönetim Masası",
                                style = TourOSTypography.TitleLarge.copy(fontWeight = FontWeight.Bold, color = TourOSColors.Primary)
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isCachingEnabled) Color(0xFF10B981) else Color(0xFFEF4444))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    if (isCachingEnabled) "ÖNBELLEK AKTİF" else "DEVRE DIŞI",
                                    style = TourOSTypography.Caption.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                )
                            }
                        }
                        Text(
                            "Dış tur, otel ve uçuş API'lerinden gelen verilerin saklanma sürelerini (TTL) belirleyin, kota tasarrufunu izleyin ve tek tıkla önbelleği yenileyin.",
                            style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = {
                                systemCacheManager.flushAllCache { count ->
                                    saveNotification = "🧹 $count adet önbellek kaydı başarıyla temizlendi! Tüm aramalar canlı API'ye yönlendirildi."
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444), contentColor = Color.White),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Önbelleği Boşalt (Flush) 🧹", fontWeight = FontWeight.Bold)
                        }

                        TourOSButton(
                            text = "Ayarları Kaydet 💾",
                            onClick = {
                                val updated = cacheConfig.copy(
                                    is_caching_enabled = isCachingEnabled,
                                    price_ttl_minutes = priceTtlMinutes,
                                    catalog_ttl_hours = catalogTtlHours,
                                    auto_flush_on_price_change = autoFlushOnPriceChange,
                                    enabled_providers = enabledProviders.toList()
                                )
                                systemCacheManager.updateConfig(updated) { success ->
                                    saveNotification = if (success) "✅ Önbellek ayarları ve TTL süreleri başarıyla kaydedildi!" else "⚠️ Ayarlar kaydedilirken hata oluştu."
                                }
                            },
                            variant = TourOSButtonVariant.PRIMARY
                        )
                    }
                }
            }

            // 2. Canlı İstatistikler & Kota Tasarrufu Pano Kartları
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Kart 1: API Tasarruf Oranı
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                    border = BorderStroke(1.dp, Color(0xFF86EFAC)),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("📊 API Kota Tasarruf Oranı", style = TourOSTypography.Caption.copy(color = Color(0xFF16A34A), fontWeight = FontWeight.Bold))
                        Text(
                            text = "%${cacheConfig.hitRatePercent} Hit Rate",
                            style = TourOSTypography.TitleLarge.copy(color = Color(0xFF15803D), fontWeight = FontWeight.ExtraBold, fontSize = 24.sp)
                        )
                        Text(
                            "Her 100 aramanın ${cacheConfig.hitRatePercent}'i önbellekten karşılanıyor, dış API kotası harcanmıyor.",
                            style = TourOSTypography.Caption.copy(color = Color(0xFF166534), fontSize = 11.sp)
                        )
                    }
                }

                // Kart 2: Önbellekten Kurtarılan Sorgu
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F9FF)),
                    border = BorderStroke(1.dp, Color(0xFFBAE6FD)),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("⚡ Önbellekten Sunulan İstek", style = TourOSTypography.Caption.copy(color = Color(0xFF0284C7), fontWeight = FontWeight.Bold))
                        Text(
                            text = "${cacheConfig.total_cache_hits} İstek",
                            style = TourOSTypography.TitleLarge.copy(color = Color(0xFF0369A1), fontWeight = FontWeight.ExtraBold, fontSize = 24.sp)
                        )
                        Text(
                            "Toplam ${cacheConfig.total_requests_served} arama arasından doğrudan hafızadan 10ms içinde açıldı.",
                            style = TourOSTypography.Caption.copy(color = Color(0xFF075985), fontSize = 11.sp)
                        )
                    }
                }

                // Kart 3: Bellekteki Anlık Kayıt Sayısı
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF5FF)),
                    border = BorderStroke(1.dp, Color(0xFFE9D5FF)),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("🧠 Bellekteki Aktif Cache", style = TourOSTypography.Caption.copy(color = Color(0xFF9333EA), fontWeight = FontWeight.Bold))
                        Text(
                            text = "${systemCacheManager.getMemoryItemCount()} Ürün / Arama",
                            style = TourOSTypography.TitleLarge.copy(color = Color(0xFF7E22CE), fontWeight = FontWeight.ExtraBold, fontSize = 24.sp)
                        )
                        Text(
                            "Son temizleme: ${if (cacheConfig.last_flushed_at.isNotBlank()) cacheConfig.last_flushed_at.take(19).replace("T", " ") else "Bugün"}",
                            style = TourOSTypography.Caption.copy(color = Color(0xFF6B21A8), fontSize = 11.sp)
                        )
                    }
                }
            }

            // 3. TTL (Time to Live) ve Çalışma Politikaları
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = TourOSColors.Surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("⏱️ Saklama Süreleri (TTL - Time to Live)", style = TourOSTypography.TitleMedium.copy(fontWeight = FontWeight.Bold, color = TourOSColors.Primary))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Fiyat TTL
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Canlı Fiyat ve Kontenjan Önbellek Süresi (Dakika)", style = TourOSTypography.BodyMedium.copy(fontWeight = FontWeight.Bold))
                            Text("Aynı arama tekrarlandığında fiyatın API'ye gitmeden gösterilme süresi. (Tavsiye: 10 - 20 dk)", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(5, 10, 15, 30, 60).forEach { mins ->
                                    val isSel = (priceTtlMinutes == mins)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSel) TourOSColors.Primary else Color(0xFFF1F5F9))
                                            .clickable { priceTtlMinutes = mins }
                                            .padding(horizontal = 14.dp, vertical = 8.dp)
                                    ) {
                                        Text("$mins Dk", color = if (isSel) Color.White else Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        // Katalog TTL
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Sabit Otel / Destinasyon / Tur Bilgileri Süresi (Saat)", style = TourOSTypography.BodyMedium.copy(fontWeight = FontWeight.Bold))
                            Text("Otel fotoğrafları, konum ve tesis özellikleri gibi nadir değişen veriler. (Tavsiye: 24 - 48 Saat)", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(6, 12, 24, 48, 72).forEach { hrs ->
                                    val isSel = (catalogTtlHours == hrs)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSel) TourOSColors.Primary else Color(0xFFF1F5F9))
                                            .clickable { catalogTtlHours = hrs }
                                            .padding(horizontal = 14.dp, vertical = 8.dp)
                                    ) {
                                        Text("$hrs Saat", color = if (isSel) Color.White else Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = Color(0xFFE2E8F0))

                    // Genel Switch'ler
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("⚡ Akıllı Önbellek Sistemi (Master Switch)", style = TourOSTypography.BodyMedium.copy(fontWeight = FontWeight.Bold))
                            Text("Devre dışı bırakılırsa tüm aramalar önbellek atlanarak her seferinde doğrudan dış API'ye gönderilir.", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                        }
                        Switch(
                            checked = isCachingEnabled,
                            onCheckedChange = { isCachingEnabled = it }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("🔄 Fiyat Değişiminde Otomatik Temizle", style = TourOSTypography.BodyMedium.copy(fontWeight = FontWeight.Bold))
                            Text("Operatörden webhook ile fiyat güncellemesi geldiğinde ilgili ürünün önbelleğini anında düşür.", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                        }
                        Switch(
                            checked = autoFlushOnPriceChange,
                            onCheckedChange = { autoFlushOnPriceChange = it }
                        )
                    }
                }
            }

            // 4. Sağlayıcı & Operatör Bazlı Önbellek İzin Listesi
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = TourOSColors.Surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("🔌 Aktif Sağlayıcı ve Dış API Önbellek Filtresi", style = TourOSTypography.TitleMedium.copy(fontWeight = FontWeight.Bold, color = TourOSColors.Primary))
                    Text("Aşağıdaki sağlayıcılar için önbellek sistemini tek tıkla açıp kapatabilirsiniz:", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))

                    val allProviders = listOf(
                        "Coral Travel",
                        "Pegas Touristik",
                        "Anex Tour",
                        "Travelata",
                        "SunExpress",
                        "Paximum",
                        "Amadeus"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        allProviders.forEach { provider ->
                            val isEnabled = enabledProviders.contains(provider)
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        val mutable = enabledProviders.toMutableSet()
                                        if (isEnabled) mutable.remove(provider) else mutable.add(provider)
                                        enabledProviders = mutable
                                    },
                                color = if (isEnabled) Color(0xFFECFDF5) else Color(0xFFF1F5F9),
                                border = BorderStroke(1.dp, if (isEnabled) Color(0xFF10B981) else Color(0xFFCBD5E1))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(if (isEnabled) "✓" else "✕", color = if (isEnabled) Color(0xFF059669) else Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                                    Text(provider, style = TourOSTypography.BodyMedium.copy(color = if (isEnabled) Color(0xFF065F46) else Color(0xFF64748B), fontWeight = FontWeight.SemiBold, fontSize = 12.sp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
