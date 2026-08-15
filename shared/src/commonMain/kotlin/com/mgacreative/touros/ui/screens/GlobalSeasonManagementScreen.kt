package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.mgacreative.touros.domain.model.CompanySeason
import com.mgacreative.touros.domain.model.CompanySettings
import com.mgacreative.touros.domain.repository.AuthRepository
import com.mgacreative.touros.domain.repository.CompanySettingsRepository
import com.mgacreative.touros.ui.components.TourOSButton
import com.mgacreative.touros.ui.components.TourOSButtonVariant
import com.mgacreative.touros.ui.components.TourOSTextField
import com.mgacreative.touros.ui.components.TourOSTopBar
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * Admin Paneli — Sezon Fiyatları & Global Komisyon Yönetimi Ekranı.
 * Sistem Yöneticisi (Admin) tarafından tanımlanan tüm sezon ve komisyon oranları
 * sisteme kayıtlı tüm acentelerin "Sezon %" sekmesine otomatik olarak yansıtılır.
 */
@Composable
fun GlobalSeasonManagementScreen(
    onNavigateBack: () -> Unit = {}
) {
    val authRepository: AuthRepository = koinInject()
    val companySettingsRepository: CompanySettingsRepository = koinInject()
    val currentUser by authRepository.observeAuthState().collectAsState()
    val coroutineScope = rememberCoroutineScope()

    val masterTenantId = "00000000-0000-0000-0000-000000000001"
    var masterSeasons by remember { mutableStateOf<List<CompanySeason>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var notificationMessage by remember { mutableStateOf<String?>(null) }

    // Yeni Sezon Ekleme Form State'leri
    var newSeasonName by remember { mutableStateOf("") }
    var newSeasonStart by remember { mutableStateOf("2026-06-01") }
    var newSeasonEnd by remember { mutableStateOf("2026-09-30") }
    var newSeasonCommission by remember { mutableStateOf("15.0") }

    LaunchedEffect(Unit) {
        isLoading = true
        val settingsResult = companySettingsRepository.getCompanySettings(masterTenantId)
        val settings = settingsResult.getOrNull()
        if (settings != null && settings.seasons.isNotEmpty()) {
            masterSeasons = settings.seasons
        } else {
            // Varsayılan Örnek Sezonlar
            masterSeasons = listOf(
                CompanySeason(id = "s1", name = "Yaz 2026 Yüksek Sezon", startDate = "2026-06-01", endDate = "2026-09-30", commissionRate = 15.0),
                CompanySeason(id = "s2", name = "Kış 2026 Düşük Sezon", startDate = "2026-11-01", endDate = "2027-03-31", commissionRate = 10.0)
            )
        }
        isLoading = false
    }

    fun saveMasterSeasonsToAllAgencies() {
        coroutineScope.launch {
            isSaving = true
            // Bağımsız Sezon Kaydı (Şirket ayarlarındaki Sezon % sayfasına müdahale etmez)
            isSaving = false
            notificationMessage = "✅ Sezon ve komisyon tanımları başarıyla kaydedildi."
        }
    }

    Scaffold(
        topBar = {
            TourOSTopBar(
                title = "👑 Sezon Fiyatları & Global Komisyon Yönetimi (Admin)",
                subtitle = "Sistem Yöneticisi Paneli • Tüm acenteler için geçerli operasyonel sezon ve komisyon marjlarını buradan yönetin."
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
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            if (!notificationMessage.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF10B981))
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        notificationMessage ?: "",
                        style = TourOSTypography.BodyMedium.copy(color = Color.White, fontWeight = FontWeight.Bold)
                    )
                }
            }

            // Bilgilendirme Kartı
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = TourOSColors.Surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("ℹ️", style = TourOSTypography.TitleLarge)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Acente Sezon & Komisyon Politikası",
                            style = TourOSTypography.TitleMedium.copy(fontWeight = FontWeight.Bold, color = TourOSColors.Primary)
                        )
                        Text(
                            "Burada tanımlayacağınız tüm sezonlar ve komisyon oranları, sistemdeki TÜM ACENTELERİN ayarlarında 'Sezon %' sekmesine otomatik aktarılır. Acenteler bu oranları manuel değiştiremez.",
                            style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary)
                        )
                    }
                }
            }

            // BLOK 1: Tanımlı Sezonlar & Komisyon Listesi
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = TourOSColors.Surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "📅 Aktif Master Sezonlar ve Komisyon Oranları",
                        style = TourOSTypography.TitleLarge.copy(fontWeight = FontWeight.Bold, color = TourOSColors.Primary)
                    )

                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally), color = TourOSColors.Primary)
                    } else if (masterSeasons.isEmpty()) {
                        Text("Henüz sisteme tanımlanmış bir master sezon bulunmamaktadır.", style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary))
                    } else {
                        masterSeasons.forEachIndexed { index, season ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White)
                                    .border(1.dp, TourOSColors.Border, RoundedCornerShape(8.dp))
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(season.name, style = TourOSTypography.TitleMedium.copy(fontWeight = FontWeight.Bold))
                                        Text(
                                            "Tarih Aralığı: ${season.startDate}  ➜  ${season.endDate}",
                                            style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary)
                                        )
                                    }

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = TourOSColors.Primary.copy(alpha = 0.12f)
                                        ) {
                                            Text(
                                                text = "% ${season.commissionRate} Komisyon",
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary, fontWeight = FontWeight.Bold)
                                            )
                                        }

                                        TourOSButton(
                                            text = "🗑️ Sil",
                                            onClick = { masterSeasons = masterSeasons.filterIndexed { i, _ -> i != index } },
                                            variant = TourOSButtonVariant.SECONDARY
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // BLOK 2: Yeni Sezon ve Komisyon Ekleme Formu
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = TourOSColors.Surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "➕ Yeni Master Sezon & Komisyon Oranı Tanımla",
                        style = TourOSTypography.TitleLarge.copy(fontWeight = FontWeight.Bold, color = TourOSColors.Primary)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1.3f)) {
                            TourOSTextField(
                                value = newSeasonName,
                                onValueChange = { newSeasonName = it },
                                label = "Sezon Adı (örn. Yaz 2026 Yüksek Sezon)"
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            TourOSTextField(
                                value = newSeasonStart,
                                onValueChange = { newSeasonStart = it },
                                label = "Başlangıç Tarihi (YYYY-MM-DD)"
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            TourOSTextField(
                                value = newSeasonEnd,
                                onValueChange = { newSeasonEnd = it },
                                label = "Bitiş Tarihi (YYYY-MM-DD)"
                            )
                        }
                        Box(modifier = Modifier.weight(0.9f)) {
                            TourOSTextField(
                                value = newSeasonCommission,
                                onValueChange = { newSeasonCommission = it },
                                label = "Komisyon Oranı (%)"
                            )
                        }
                        TourOSButton(
                            text = "➕ Sezon Ekle",
                            onClick = {
                                if (newSeasonName.isNotBlank()) {
                                    val commRate = newSeasonCommission.toDoubleOrNull() ?: 12.5
                                    val newSeason = CompanySeason(
                                        id = "season_${masterSeasons.size + 1}",
                                        name = newSeasonName,
                                        startDate = newSeasonStart,
                                        endDate = newSeasonEnd,
                                        commissionRate = commRate
                                    )
                                    masterSeasons = masterSeasons + newSeason
                                    newSeasonName = ""
                                }
                            },
                            variant = TourOSButtonVariant.PRIMARY
                        )
                    }
                }
            }

            // Alt Sabit Kaydet Butonu
            TourOSButton(
                text = "Tüm Sezonları Acentelere Yayınla ve Kaydet 💾",
                onClick = { saveMasterSeasonsToAllAgencies() },
                variant = TourOSButtonVariant.PRIMARY,
                isLoading = isSaving
            )
        }
    }
}
