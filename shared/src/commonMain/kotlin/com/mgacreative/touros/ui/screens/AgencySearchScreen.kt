package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
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
data class AgencySearchResultDto(
    val company_id: String,
    val agency_name: String,
    val operator_code: String,
    val email: String,
    val phone: String,
    val country: String,
    val address: String,
    val tax_number: String,
    val tax_office: String,
    val mersis_no: String,
    val is_active: Boolean,
    val created_at: String
)

@Serializable
data class SearchAgenciesParams(
    val p_search_query: String = "",
    val p_country: String = ""
)

/**
 * Admin Paneli - Acente Sorgulama ve Detay Görüntüleme Ekranı (Master-Detail).
 */
@Composable
fun AgencySearchScreen() {
    val supabase: SupabaseClient = koinInject()
    val scope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    var countryFilter by remember { mutableStateOf("") }
    var agencyList by remember { mutableStateOf<List<AgencySearchResultDto>>(emptyList()) }
    var selectedAgency by remember { mutableStateOf<AgencySearchResultDto?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    fun fetchAgencies() {
        scope.launch {
            isLoading = true
            try {
                val results = supabase.postgrest.rpc(
                    "search_agencies",
                    SearchAgenciesParams(
                        p_search_query = searchQuery.trim(),
                        p_country = countryFilter.trim()
                    )
                ).decodeList<AgencySearchResultDto>()
                
                agencyList = results
                if (selectedAgency == null || results.none { it.company_id == selectedAgency?.company_id }) {
                    selectedAgency = results.firstOrNull()
                }
            } catch (e: Exception) {
                errorMsg = "Acente sorgulaması başarısız: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(searchQuery, countryFilter) {
        fetchAgencies()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TourOSColors.Background)
            .padding(TourOSSpacing.large)
    ) {
        // Üst Banner & Filtre Alanı
        TourOSCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = TourOSColors.Surface,
            borderColor = TourOSColors.Border
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Acente Sorgulama & Detay Rehberi",
                    style = TourOSTypography.DisplaySmall.copy(color = TourOSColors.Primary)
                )
                Spacer(modifier = Modifier.height(TourOSSpacing.xSmall))
                Text(
                    text = "Sistemde kayıtlı tüm acenteleri isim, acente kodu, e-posta ve ülkeye göre filtreleyin ve detaylarını inceleyin.",
                    style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary)
                )

                Spacer(modifier = Modifier.height(TourOSSpacing.medium))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                ) {
                    TourOSTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = "Acente Adı / Kodu / E-posta",
                        placeholder = "Arama yapın (Örn: AXL-001 veya Gezgin)",
                        modifier = Modifier.weight(2f)
                    )

                    TourOSTextField(
                        value = countryFilter,
                        onValueChange = { countryFilter = it },
                        label = "Ülke Filtresi",
                        placeholder = "Örn: Türkiye, Almanya",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(TourOSSpacing.large))

        // Hata Mesajı Banner
        if (errorMsg != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                    .background(TourOSColors.SecondaryContainer.copy(alpha = 0.4f))
                    .padding(TourOSSpacing.medium)
            ) {
                Text(text = errorMsg!!, style = TourOSTypography.Label.copy(color = TourOSColors.Secondary))
            }
            Spacer(modifier = Modifier.height(TourOSSpacing.medium))
        }

        // Ana Master-Detail Gövdesi
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.large)
        ) {
            // Sol Taraf: Sonuç Listesi (Master)
            Column(
                modifier = Modifier
                    .weight(1.2f)
                    .fillMaxHeight()
            ) {
                Text(
                    text = "Arama Sonuçları (${agencyList.size})",
                    style = TourOSTypography.TitleLarge.copy(color = TourOSColors.Primary)
                )
                Spacer(modifier = Modifier.height(TourOSSpacing.small))

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Acenteler aranıyor...", style = TourOSTypography.BodyMedium)
                    }
                } else if (agencyList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "Arama kriterlerine uygun acente bulunamadı.",
                            style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary)
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
                    ) {
                        items(agencyList, key = { it.company_id }) { agency ->
                            val isSelected = agency.company_id == selectedAgency?.company_id
                            val borderColor = if (isSelected) TourOSColors.Primary else TourOSColors.Border
                            val containerColor = if (isSelected) TourOSColors.PrimaryContainer.copy(alpha = 0.25f) else TourOSColors.Surface

                            TourOSCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedAgency = agency },
                                backgroundColor = containerColor,
                                borderColor = borderColor
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = agency.agency_name,
                                            style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary)
                                        )
                                        Spacer(modifier = Modifier.height(TourOSSpacing.xSmall))
                                        Text(
                                            text = "${agency.email.ifBlank { "E-posta yok" }} | Tel: ${agency.phone.ifBlank { "-" }}",
                                            style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(TourOSSpacing.small))

                                    // Acente Kodu Rozeti (Badge)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                                            .background(TourOSColors.Primary)
                                            .padding(horizontal = TourOSSpacing.medium, vertical = TourOSSpacing.xSmall)
                                    ) {
                                        Text(
                                            text = agency.operator_code,
                                            style = TourOSTypography.Label.copy(color = TourOSColors.OnPrimary)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Sağ Taraf: Acente Detay Kartı (Detail Panel)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Text(
                    text = "Acente Detay Kartı",
                    style = TourOSTypography.TitleLarge.copy(color = TourOSColors.Primary)
                )
                Spacer(modifier = Modifier.height(TourOSSpacing.small))

                val agency = selectedAgency
                if (agency == null) {
                    TourOSCard(
                        modifier = Modifier.fillMaxSize(),
                        backgroundColor = TourOSColors.Surface,
                        borderColor = TourOSColors.Border
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                "Detaylarını görmek için soldaki listeden bir acente seçin.",
                                style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary)
                            )
                        }
                    }
                } else {
                    TourOSCard(
                        modifier = Modifier.fillMaxSize(),
                        backgroundColor = TourOSColors.Surface,
                        borderColor = TourOSColors.Border
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(TourOSSpacing.small)
                        ) {
                            // Başlık & Durum
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = agency.agency_name,
                                    style = TourOSTypography.DisplaySmall.copy(color = TourOSColors.Primary)
                                )

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                                        .background(
                                            if (agency.is_active) TourOSColors.PrimaryContainer else TourOSColors.SecondaryContainer
                                        )
                                        .padding(horizontal = TourOSSpacing.medium, vertical = TourOSSpacing.xSmall)
                                ) {
                                    Text(
                                        text = if (agency.is_active) "AKTİF HESAP" else "ONAY BEKLİYOR / PASİF",
                                        style = TourOSTypography.Label.copy(
                                            color = if (agency.is_active) TourOSColors.Primary else TourOSColors.Secondary
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(TourOSSpacing.large))

                            // Acente Kodu (Login Kodu) Vurgusu
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(TourOSSpacing.cornerRadius))
                                    .background(TourOSColors.PrimaryContainer.copy(alpha = 0.3f))
                                    .border(1.dp, TourOSColors.Primary, RoundedCornerShape(TourOSSpacing.cornerRadius))
                                    .padding(TourOSSpacing.medium)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Giriş Acente Kodu (B2B SaaS)",
                                            style = TourOSTypography.Label.copy(color = TourOSColors.TextSecondary)
                                        )
                                        Text(
                                            text = agency.operator_code,
                                            style = TourOSTypography.TitleLarge.copy(color = TourOSColors.Primary)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(TourOSSpacing.large))

                            // İletişim Bilgileri
                            Text(
                                text = "İletişim & Konum Bilgileri",
                                style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary)
                            )
                            Spacer(modifier = Modifier.height(TourOSSpacing.small))
                            DetailRow(label = "E-posta Adresi", value = agency.email.ifBlank { "Yok" })
                            DetailRow(label = "Telefon Numarası", value = agency.phone.ifBlank { "Yok" })
                            DetailRow(label = "Adres / Ülke", value = agency.address.ifBlank { "Türkiye" })

                            Spacer(modifier = Modifier.height(TourOSSpacing.medium))

                            // Kurumsal Bilgiler
                            Text(
                                text = "Kurumsal / Vergi Bilgileri",
                                style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary)
                            )
                            Spacer(modifier = Modifier.height(TourOSSpacing.small))
                            DetailRow(label = "Vergi Dairesi", value = agency.tax_office.ifBlank { "-" })
                            DetailRow(label = "Vergi Numarası", value = agency.tax_number.ifBlank { "-" })
                            DetailRow(label = "Mersis No", value = agency.mersis_no.ifBlank { "-" })
                            DetailRow(label = "Kayıt Tarihi", value = agency.created_at.take(10))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = TourOSSpacing.xSmall),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary))
        Text(text = value, style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary))
    }
}
