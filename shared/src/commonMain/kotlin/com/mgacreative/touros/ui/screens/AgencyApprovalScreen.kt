package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
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
    val email: String,
    val phone: String,
    val current_code: String,
    val is_active: Boolean,
    val created_at: String
)

@Serializable
data class AssignCodeParams(
    val p_company_id: String,
    val p_agency_code: String
)

/**
 * Admin Paneli - Onay Bekleyen Acenteler ve Kodu Tanımlama Ekranı.
 */
@Composable
fun AgencyApprovalScreen() {
    val supabase: SupabaseClient = koinInject()
    val scope = rememberCoroutineScope()
    var pendingAgencies by remember { mutableStateOf<List<PendingAgencyDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var notificationMsg by remember { mutableStateOf<String?>(null) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    
    val inputCodes = remember { mutableStateMapOf<String, String>() }

    fun loadPendingAgencies() {
        scope.launch {
            isLoading = true
            try {
                val result = supabase.postgrest.rpc("get_pending_approval_agencies").decodeList<PendingAgencyDto>()
                pendingAgencies = result
                notificationMsg = if (result.isEmpty()) "Onay bekleyen yeni acente bulunmamaktadır." else null
            } catch (e: Exception) {
                errorMsg = "Acenteler yüklenirken hata oluştu: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadPendingAgencies()
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
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Acente Onay & Kod Atama Paneli",
                    style = TourOSTypography.DisplaySmall.copy(color = TourOSColors.Primary)
                )
                Spacer(modifier = Modifier.height(TourOSSpacing.xSmall))
                Text(
                    text = "Sisteme kaydolan acenteleri inceleyin, özel acente kodlarını tanımlayarak hesaplarını aktifleştirin.",
                    style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary)
                )
            }
        }

        Spacer(modifier = Modifier.height(TourOSSpacing.large))

        // Notification Banner
        if (notificationMsg != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                    .background(TourOSColors.PrimaryContainer.copy(alpha = 0.4f))
                    .padding(TourOSSpacing.medium)
            ) {
                Text(
                    text = notificationMsg!!,
                    style = TourOSTypography.Label.copy(color = TourOSColors.Primary)
                )
            }
            Spacer(modifier = Modifier.height(TourOSSpacing.medium))
        }

        if (errorMsg != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                    .background(TourOSColors.SecondaryContainer.copy(alpha = 0.4f))
                    .padding(TourOSSpacing.medium)
            ) {
                Text(
                    text = errorMsg!!,
                    style = TourOSTypography.Label.copy(color = TourOSColors.Secondary)
                )
            }
            Spacer(modifier = Modifier.height(TourOSSpacing.medium))
        }

        // List
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Acenteler yükleniyor...", style = TourOSTypography.BodyLarge)
            }
        } else if (pendingAgencies.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Tüm acente başvuruları onaylanmış durumdadır.", style = TourOSTypography.BodyLarge.copy(color = TourOSColors.TextSecondary))
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
            ) {
                items(pendingAgencies, key = { it.company_id }) { agency ->
                    val currentInput = inputCodes[agency.company_id] ?: ""

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
                                    text = agency.agency_name,
                                    style = TourOSTypography.TitleLarge.copy(color = TourOSColors.Primary)
                                )
                                Spacer(modifier = Modifier.height(TourOSSpacing.xSmall))
                                Text(
                                    text = "E-posta: ${agency.email.ifBlank { "Belirtilmemiş" }} | Tel: ${agency.phone.ifBlank { "-" }}",
                                    style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary)
                                )
                                Text(
                                    text = "Kayıt Tarihi: ${agency.created_at.take(10)}",
                                    style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                                )
                            }

                            Spacer(modifier = Modifier.width(TourOSSpacing.large))

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TourOSTextField(
                                    value = currentInput,
                                    onValueChange = { inputCodes[agency.company_id] = it },
                                    label = "Acente Kodu",
                                    placeholder = "Örn: AXL-001",
                                    modifier = Modifier.width(180.dp)
                                )

                                Spacer(modifier = Modifier.width(TourOSSpacing.medium))

                                TourOSButton(
                                    text = "Onayla & Kodu Ata",
                                    onClick = {
                                        if (currentInput.isNotBlank()) {
                                            scope.launch {
                                                try {
                                                    supabase.postgrest.rpc(
                                                        "assign_agency_code_and_activate",
                                                        AssignCodeParams(
                                                            p_company_id = agency.company_id,
                                                            p_agency_code = currentInput
                                                        )
                                                    )
                                                    notificationMsg = "✅ ${agency.agency_name} için '${currentInput.uppercase()}' kodu başarıyla atandı ve aktifleştirildi."
                                                    loadPendingAgencies()
                                                } catch (e: Exception) {
                                                    errorMsg = "Kod atanırken hata oluştu: ${e.message}"
                                                }
                                            }
                                        }
                                    },
                                    variant = TourOSButtonVariant.PRIMARY,
                                    enabled = currentInput.isNotBlank()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
