package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mgacreative.touros.ui.components.TourOSButton
import com.mgacreative.touros.ui.components.TourOSButtonVariant
import com.mgacreative.touros.ui.components.TourOSCard
import com.mgacreative.touros.ui.components.TourOSTextField
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.koin.compose.koinInject

@Serializable
data class PendingAgencyDto(
    val company_id: String,
    val agency_name: String,
    val full_name: String? = null,
    val email: String = "",
    val phone: String = "",
    val current_code: String = "",
    val is_active: Boolean = false,
    val status: String = "PENDING",
    val created_at: String = ""
)

@Serializable
data class RegisteredAgencyDto(
    val company_id: String,
    val agency_name: String,
    val full_name: String? = null,
    val email: String = "",
    val phone: String = "",
    val agency_code: String = "",
    val is_active: Boolean = true,
    val status: String = "ACTIVE",
    val created_at: String = ""
)

@Serializable
data class AssignCodeParams(
    val p_company_id: String,
    val p_agency_code: String
)

@Serializable
data class RejectAgencyParams(
    val p_company_id: String,
    val p_reason: String = "Sistem yöneticisi tarafından onaylanmadı"
)

@Serializable
data class DeleteAgencyParams(
    val p_company_id: String
)

/**
 * Admin Paneli - Onay Bekleyen ve Kayıtlı Acenteler Yönetim Ekranı.
 */
@Composable
fun AgencyApprovalScreen() {
    val supabase: SupabaseClient = koinInject()
    val scope = rememberCoroutineScope()

    var selectedTabIndex by remember { mutableStateOf(0) } // 0: Onay Bekleyenler, 1: Kayıtlı Acenteler
    var pendingAgencies by remember { mutableStateOf<List<PendingAgencyDto>>(emptyList()) }
    var registeredAgencies by remember { mutableStateOf<List<RegisteredAgencyDto>>(emptyList()) }
    var nextSuggestedCode by remember { mutableStateOf("AXL-0001") }
    var registeredSearchQuery by remember { mutableStateOf("") }
    
    var isLoading by remember { mutableStateOf(true) }
    var notificationMsg by remember { mutableStateOf<String?>(null) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    // Dialog state for hard delete confirmation
    var agencyToDelete by remember { mutableStateOf<PendingAgencyDto?>(null) }

    val inputCodes = remember { mutableStateMapOf<String, String>() }

    fun refreshAllData() {
        scope.launch {
            isLoading = true
            errorMsg = null
            try {
                // 1. Sıradaki önerilen kodu çek
                val suggested = runCatching {
                    supabase.postgrest.rpc("get_next_suggested_agency_code").decodeSingle<String>()
                }.getOrDefault("AXL-0001")
                nextSuggestedCode = suggested

                // 2. Onay bekleyenleri çek
                val pending = supabase.postgrest.rpc("get_pending_approval_agencies").decodeList<PendingAgencyDto>()
                pendingAgencies = pending

                // Önerilen kodu bekleyenlere otomatik doldur (eğer input boşsa)
                pending.forEach { agency ->
                    if (inputCodes[agency.company_id].isNullOrBlank()) {
                        inputCodes[agency.company_id] = suggested
                    }
                }

                // 3. Kayıtlı acenteleri çek
                val registered = supabase.postgrest.rpc("get_registered_agencies").decodeList<RegisteredAgencyDto>()
                registeredAgencies = registered

            } catch (e: Exception) {
                errorMsg = "Veriler yüklenirken hata oluştu: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        refreshAllData()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TourOSColors.Background)
            .padding(TourOSSpacing.large)
    ) {
        // Header Banner
        TourOSCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = TourOSColors.Surface,
            borderColor = TourOSColors.Border
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Acente Onay & Lisans Paneli",
                        style = TourOSTypography.TitleLarge.copy(color = TourOSColors.Primary)
                    )
                    Spacer(modifier = Modifier.height(TourOSSpacing.xSmall))
                    Text(
                        text = "Sisteme kaydolan acenteleri onaylayın, benzersiz acente kodları atayın ve mevcut kayıtlı acenteleri yönetin.",
                        style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary)
                    )
                }

                TourOSButton(
                    text = "Yenile",
                    onClick = { refreshAllData() },
                    variant = TourOSButtonVariant.TERTIARY,
                    isLoading = isLoading
                )
            }
        }

        Spacer(modifier = Modifier.height(TourOSSpacing.medium))

        // Notification Banner
        if (notificationMsg != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                    .background(TourOSColors.Primary.copy(alpha = 0.1f))
                    .padding(TourOSSpacing.medium)
            ) {
                Text(
                    text = notificationMsg!!,
                    style = TourOSTypography.BodyMedium.copy(color = TourOSColors.Primary)
                )
            }
            Spacer(modifier = Modifier.height(TourOSSpacing.medium))
        }

        if (errorMsg != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                    .background(TourOSColors.ErrorContainer)
                    .padding(TourOSSpacing.medium)
            ) {
                Text(
                    text = errorMsg!!,
                    style = TourOSTypography.BodyMedium.copy(color = TourOSColors.Error)
                )
            }
            Spacer(modifier = Modifier.height(TourOSSpacing.medium))
        }

        // Sekmeler (TabRow)
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = TourOSColors.Surface,
            contentColor = TourOSColors.Primary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = TourOSColors.Primary
                )
            }
        ) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.HourglassEmpty, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(TourOSSpacing.small))
                        Text(
                            text = "Onay Bekleyenler (${pendingAgencies.size})",
                            style = TourOSTypography.TitleSmall
                        )
                    }
                }
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(TourOSSpacing.small))
                        Text(
                            text = "Kayıtlı Acenteler (${registeredAgencies.size})",
                            style = TourOSTypography.TitleSmall
                        )
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(TourOSSpacing.large))

        // TAB 0: ONAY BEKLEYENLER
        if (selectedTabIndex == 0) {
            if (pendingAgencies.isEmpty()) {
                TourOSCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = TourOSColors.Surface,
                    borderColor = TourOSColors.Border
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(TourOSSpacing.xxLarge),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✅ Onay bekleyen yeni acente başvurusu bulunmamaktadır.",
                            style = TourOSTypography.BodyLarge.copy(color = TourOSColors.TextSecondary)
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                ) {
                    items(pendingAgencies, key = { it.company_id }) { agency ->
                        val currentInput = inputCodes[agency.company_id] ?: nextSuggestedCode

                        TourOSCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = TourOSColors.Surface,
                            borderColor = TourOSColors.Border
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Sol Bilgiler
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = agency.agency_name,
                                        style = TourOSTypography.TitleLarge.copy(color = TourOSColors.Primary)
                                    )
                                    Spacer(modifier = Modifier.height(TourOSSpacing.xSmall))
                                    Text(
                                        text = "Yetkili: ${agency.full_name ?: "-"} | E-posta: ${agency.email.ifBlank { "-" }}",
                                        style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextPrimary)
                                    )
                                    Spacer(modifier = Modifier.height(TourOSSpacing.xxSmall))
                                    Text(
                                        text = "Başvuru Tarihi: ${agency.created_at.take(10)} | Durum: Onay Bekliyor",
                                        style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                                    )
                                }

                                Spacer(modifier = Modifier.width(TourOSSpacing.large))

                                // Sağ İşlem Alanı (Kod Girişi + Onayla + Reddet + Sil)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
                                ) {
                                    TourOSTextField(
                                        value = currentInput,
                                        onValueChange = { inputCodes[agency.company_id] = it },
                                        label = "Acente Kodu",
                                        placeholder = nextSuggestedCode,
                                        modifier = Modifier.width(150.dp)
                                    )

                                    TourOSButton(
                                        text = "Onayla & Ata",
                                        onClick = {
                                            if (currentInput.isNotBlank()) {
                                                scope.launch {
                                                    try {
                                                        supabase.postgrest.rpc(
                                                            "assign_agency_code_and_activate",
                                                            AssignCodeParams(
                                                                p_company_id = agency.company_id,
                                                                p_agency_code = currentInput.trim().uppercase()
                                                            )
                                                        )
                                                        notificationMsg = "✅ ${agency.agency_name} için '${currentInput.uppercase()}' kodu başarıyla atandı ve aktifleştirildi."
                                                        refreshAllData()
                                                    } catch (e: Exception) {
                                                        errorMsg = "Kod atanırken hata oluştu: ${e.message}"
                                                    }
                                                }
                                            }
                                        },
                                        variant = TourOSButtonVariant.PRIMARY,
                                        enabled = currentInput.isNotBlank()
                                    )

                                    TourOSButton(
                                        text = "Reddet",
                                        onClick = {
                                            scope.launch {
                                                try {
                                                    supabase.postgrest.rpc(
                                                        "reject_agency",
                                                        RejectAgencyParams(
                                                            p_company_id = agency.company_id,
                                                            p_reason = "Admin tarafından reddedildi"
                                                        )
                                                    )
                                                    notificationMsg = "🚫 ${agency.agency_name} başvurusu reddedildi ve arşive alındı."
                                                    refreshAllData()
                                                } catch (e: Exception) {
                                                    errorMsg = "Reddedilirken hata oluştu: ${e.message}"
                                                }
                                            }
                                        },
                                        variant = TourOSButtonVariant.TERTIARY
                                    )

                                    IconButton(
                                        onClick = { agencyToDelete = agency }
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Kalıcı Sil",
                                            tint = TourOSColors.Error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // TAB 1: KAYITLI VE ONAYLANMIŞ ACENTELER
        if (selectedTabIndex == 1) {
            // Arama Kutusu
            TourOSTextField(
                value = registeredSearchQuery,
                onValueChange = { registeredSearchQuery = it },
                label = "Kayıtlı Acentelerde Ara",
                placeholder = "Acente adı, yetkili, e-posta veya acente kodu ile arayın...",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(TourOSSpacing.medium))

            val filteredRegistered = registeredAgencies.filter {
                val query = registeredSearchQuery.trim().lowercase()
                query.isBlank() ||
                        it.agency_name.lowercase().contains(query) ||
                        (it.full_name?.lowercase()?.contains(query) == true) ||
                        it.email.lowercase().contains(query) ||
                        it.agency_code.lowercase().contains(query)
            }

            if (filteredRegistered.isEmpty()) {
                TourOSCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = TourOSColors.Surface,
                    borderColor = TourOSColors.Border
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(TourOSSpacing.xxLarge),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (registeredSearchQuery.isNotBlank()) "Arama kriterine uygun kayıtlı acente bulunamadı." else "Henüz onaylanmış kayıtlı acente bulunmamaktadır.",
                            style = TourOSTypography.BodyLarge.copy(color = TourOSColors.TextSecondary)
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                ) {
                    items(filteredRegistered, key = { it.company_id }) { agency ->
                        TourOSCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = TourOSColors.Surface,
                            borderColor = TourOSColors.Border
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
                                    ) {
                                        Text(
                                            text = agency.agency_name,
                                            style = TourOSTypography.TitleLarge.copy(color = TourOSColors.Primary)
                                        )

                                        // Acente Kodu Rozeti
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                                                .background(TourOSColors.Primary.copy(alpha = 0.1f))
                                                .padding(horizontal = TourOSSpacing.medium, vertical = TourOSSpacing.xxSmall)
                                        ) {
                                            Text(
                                                text = agency.agency_code,
                                                style = TourOSTypography.TitleSmall.copy(color = TourOSColors.Primary)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(TourOSSpacing.xSmall))

                                    Text(
                                        text = "Yetkili: ${agency.full_name ?: "-"} | E-posta: ${agency.email.ifBlank { "-" }}",
                                        style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextPrimary)
                                    )
                                    Spacer(modifier = Modifier.height(TourOSSpacing.xxSmall))
                                    Text(
                                        text = "Kayıt Tarihi: ${agency.created_at.take(10)} | Durum: Aktif Acente",
                                        style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Kalıcı Silme Onay Popup'ı
    if (agencyToDelete != null) {
        val target = agencyToDelete!!
        AlertDialog(
            onDismissRequest = { agencyToDelete = null },
            title = {
                Text(
                    text = "Acenteyi Kalıcı Olarak Sil?",
                    style = TourOSTypography.TitleLarge.copy(color = TourOSColors.Error)
                )
            },
            text = {
                Text(
                    text = "${target.agency_name} (${target.email}) kaydı sistemden ve veritabanından tamamen silinecektir. Bu işlem geri alınamaz.",
                    style = TourOSTypography.BodyMedium
                )
            },
            confirmButton = {
                TourOSButton(
                    text = "Evet, Kalıcı Olarak Sil",
                    onClick = {
                        scope.launch {
                            try {
                                supabase.postgrest.rpc(
                                    "delete_agency_permanently",
                                    DeleteAgencyParams(p_company_id = target.company_id)
                                )
                                notificationMsg = "🗑️ ${target.agency_name} kaydı sistemden tamamen silindi."
                                agencyToDelete = null
                                refreshAllData()
                            } catch (e: Exception) {
                                errorMsg = "Kalıcı silme sırasında hata: ${e.message}"
                            }
                        }
                    },
                    variant = TourOSButtonVariant.PRIMARY
                )
            },
            dismissButton = {
                TextButton(onClick = { agencyToDelete = null }) {
                    Text("İptal", style = TourOSTypography.TitleSmall.copy(color = TourOSColors.TextSecondary))
                }
            }
        )
    }
}
