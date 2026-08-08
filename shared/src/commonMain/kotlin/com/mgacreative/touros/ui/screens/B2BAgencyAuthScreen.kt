package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mgacreative.touros.domain.model.B2BAgencyProfile
import com.mgacreative.touros.ui.components.*
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.ui.viewmodel.B2BAgencyAuthViewModel

/**
 * B2B Acente Girişi — TourOS 0.3
 *
 * Ana giriş ekranıyla aynı dar kart düzeni (max width 420dp, ortalanmış).
 * Üstte 'Acente Girişi' etiketiyle ayırt edici küçük bir rozet.
 * Oturum açıldıktan sonra B2B Acente Cari Hesap Bakiyesi Dashboard'u gösterilir.
 */
@Composable
fun B2BAgencyAuthScreen(
    viewModel: B2BAgencyAuthViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    var agencyCode by remember { mutableStateOf("ACN-GLB") }
    var email by remember { mutableStateOf("b2b@globaltravel.com") }
    var password by remember { mutableStateOf("123456") }

    Scaffold(
        containerColor = TourOSColors.Surface,
        topBar = {
            TourOSTopBar(
                title = "B2B Acente Portalı",
                subtitle = "Partner acente girişi ve cari hesap yönetimi",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←", style = TourOSTypography.TitleLarge.copy(color = TourOSColors.OnPrimary))
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 420.dp)
                    .fillMaxWidth()
                    .padding(TourOSSpacing.large),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
            ) {
                // Bildirim Mesajı
                if (state.notificationMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                            .background(TourOSColors.SuccessContainer)
                            .padding(TourOSSpacing.medium)
                    ) {
                        Text(
                            state.notificationMessage!!,
                            style = TourOSTypography.Label.copy(color = TourOSColors.Success)
                        )
                    }
                }

                if (!state.isAuthenticated) {
                    // ── ANA GİRİŞ EKRANIYLA AYNI DAR KART DÜZENİ ──────────────────
                    TourOSCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = TourOSColors.Surface,
                        contentPadding = TourOSSpacing.large
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium)
                        ) {
                            // ÜSTTE 'ACENTE GİRİŞİ' ETİKETİYLE AYIRT EDİCİ KÜÇÜK ROZET
                            TourOSStatusBadge(
                                text = "🏢 ACENTE GİRİŞİ",
                                backgroundColor = TourOSColors.PrimaryContainer,
                                textColor = TourOSColors.Primary
                            )

                            Text(
                                "TourOS B2B Portalı",
                                style = TourOSTypography.TitleLarge.copy(color = TourOSColors.Primary),
                                textAlign = TextAlign.Center
                            )

                            Text(
                                "Lütfen yetkili acente kodunuz ve şifreniz ile oturum açın.",
                                style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary),
                                textAlign = TextAlign.Center
                            )

                            HorizontalDivider(color = TourOSColors.Divider)

                            // Form Alanları
                            TourOSTextField(
                                value = agencyCode,
                                onValueChange = { agencyCode = it },
                                label = "Acente Kodu",
                                placeholder = "ACN-GLB",
                                modifier = Modifier.fillMaxWidth()
                            )

                            TourOSTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = "Yetkili E-Posta",
                                placeholder = "b2b@globaltravel.com",
                                modifier = Modifier.fillMaxWidth()
                            )

                            TourOSTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = "Şifre",
                                placeholder = "••••••",
                                visualTransformation = PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth()
                            )

                            TourOSButton(
                                text = "🔑 B2B Portalına Giriş Yap",
                                onClick = { viewModel.loginB2BAgency(agencyCode, email, password) },
                                enabled = !state.isLoading && agencyCode.isNotBlank() && email.isNotBlank(),
                                variant = TourOSButtonVariant.PRIMARY,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                } else {
                    // Giriş Yapılmış Acente Cari Dashboard
                    state.agencyProfile?.let { profile ->
                        B2BAgencyCurrentAccountDashboard(
                            profile = profile,
                            onLogout = { viewModel.logout() }
                        )
                    }
                }
            }
        }
    }
}

// ─── B2B Acente Cari Dashboard Kartı ──────────────────────────────────────────

@Composable
private fun B2BAgencyCurrentAccountDashboard(
    profile: B2BAgencyProfile,
    onLogout: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(TourOSSpacing.medium),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Acente Kimlik Kartı
        TourOSCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = TourOSColors.PrimaryContainer,
            contentPadding = TourOSSpacing.large
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        profile.agencyName,
                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary)
                    )
                    Text(
                        "KOD: ${profile.agencyCode}  ·  ${profile.contactEmail}",
                        style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                    )
                }

                TourOSStatusBadge(
                    text = "🟢 B2B ONAYLI",
                    backgroundColor = TourOSColors.SuccessContainer,
                    textColor = TourOSColors.Success
                )
            }
        }

        // Cari Hesap Metrik Kartları
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
        ) {
            TourOSCard(
                modifier = Modifier.weight(1f),
                backgroundColor = TourOSColors.Surface,
                contentPadding = TourOSSpacing.medium
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Kredi Limiti", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                    Text("₺ ${formatMoney(profile.creditLimit)}", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary))
                }
            }

            TourOSCard(
                modifier = Modifier.weight(1f),
                backgroundColor = TourOSColors.Surface,
                contentPadding = TourOSSpacing.medium
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Güncel Bakiye", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                    Text("₺ ${formatMoney(profile.currentBalance)}", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Success))
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small)
        ) {
            TourOSCard(
                modifier = Modifier.weight(1f),
                backgroundColor = TourOSColors.Surface,
                contentPadding = TourOSSpacing.medium
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Bekleyen Komisyon", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                    Text("₺ ${formatMoney(profile.pendingCommission)}", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Secondary))
                }
            }

            TourOSCard(
                modifier = Modifier.weight(1f),
                backgroundColor = TourOSColors.Surface,
                contentPadding = TourOSSpacing.medium
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Aktif Rezervasyon", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                    Text("${profile.activeBookingsCount} Adet", style = TourOSTypography.TitleMedium.copy(color = TourOSColors.TextPrimary))
                }
            }
        }

        TourOSButton(
            text = "🚪 B2B Oturumunu Kapat",
            onClick = onLogout,
            variant = TourOSButtonVariant.SECONDARY,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun formatMoney(amount: Double): String {
    val rounded = (amount * 100).toLong() / 100.0
    return rounded.toString()
}
