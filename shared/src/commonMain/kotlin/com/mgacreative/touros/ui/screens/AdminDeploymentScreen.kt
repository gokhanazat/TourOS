package com.mgacreative.touros.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mgacreative.touros.ui.components.TourOSButton
import com.mgacreative.touros.ui.components.TourOSButtonVariant
import com.mgacreative.touros.ui.components.TourOSTextField
import com.mgacreative.touros.ui.components.TourOSTopBar
import com.mgacreative.touros.ui.localization.AppLanguageManager
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.ui.viewmodel.AdminDeploymentViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * SAAS ADMİN PANELİ: Sürüm, Dağıtım & CI/CD Yönetimi Ekranı.
 * Tek tıkla Desktop EXE/MSI derleme, Web Deploy tetikleme ve indirme linkleri.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminDeploymentScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: AdminDeploymentViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val uriHandler = LocalUriHandler.current

    var targetVersionTag by remember { mutableStateOf("v1.0.1") }

    Scaffold(
        topBar = {
            TourOSTopBar(
                title = AppLanguageManager.translate("SaaS Admin — Sürüm & Dağıtım (CI/CD) Yönetimi"),
                subtitle = AppLanguageManager.translate("Desktop EXE/MSI Derleme, Web Deploy ve Sürüm Dağıtım Masası")
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

            // BİLDİRİM BANNER'I
            if (!uiState.statusMessage.isNullOrBlank()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = TourOSColors.Success.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, TourOSColors.Success.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = uiState.statusMessage ?: "",
                            style = TourOSTypography.BodyMedium.copy(color = TourOSColors.Success, fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.clearMessages() }) {
                            Text("✕", color = TourOSColors.Success)
                        }
                    }
                }
            }

            if (!uiState.errorMessage.isNullOrBlank()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = TourOSColors.Error.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, TourOSColors.Error.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = uiState.errorMessage ?: "",
                            style = TourOSTypography.BodyMedium.copy(color = TourOSColors.Error, fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.clearMessages() }) {
                            Text("✕", color = TourOSColors.Error)
                        }
                    }
                }
            }

            // 2 KOLONLU DAĞITIM KARTLARI
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                maxItemsInEachRow = 2
            ) {

                // ── 1. MASAÜSTÜ (WINDOWS .EXE / .MSI) YÖNETİMİ ──
                Surface(
                    modifier = Modifier.weight(1f, fill = false).widthIn(min = 340.dp, max = 600.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = TourOSColors.Surface,
                    border = BorderStroke(1.dp, TourOSColors.SurfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = TourOSColors.Primary.copy(alpha = 0.12f),
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("🖥️", fontSize = 22.sp)
                                }
                            }
                            Column {
                                Text(
                                    "Windows Masaüstü (.EXE / .MSI)",
                                    style = TourOSTypography.TitleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    "Compose Native Desktop Dağıtımı",
                                    style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                                )
                            }
                        }

                        HorizontalDivider(color = TourOSColors.SurfaceVariant.copy(alpha = 0.5f))

                        // Durum Bilgileri
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = TourOSColors.Background,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text("Hedef Platform:", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                                    Text("Windows 10 / 11 (x64)", style = TourOSTypography.Caption.copy(fontWeight = FontWeight.Bold))
                                }
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text("Son Sürüm Durumu:", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                                    Text(uiState.lastDesktopBuildTime, style = TourOSTypography.Caption.copy(color = TourOSColors.Success, fontWeight = FontWeight.Bold))
                                }
                            }
                        }

                        // Sürüm Etiketi Girişi
                        TourOSTextField(
                            value = targetVersionTag,
                            onValueChange = { targetVersionTag = it },
                            label = "Derlenecek Sürüm Etiketi (Tag)",
                            placeholder = "Örn: v1.0.1",
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Butonlar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            TourOSButton(
                                text = if (uiState.isBuildingDesktop) "⏳ Derleniyor..." else "🚀 Yeni EXE Derle",
                                onClick = {
                                    viewModel.triggerDesktopBuild(targetVersionTag)
                                    if (uiState.githubToken.isBlank()) {
                                        uriHandler.openUri("${uiState.githubActionsUrl}/workflows/build-desktop.yml")
                                    }
                                },
                                enabled = !uiState.isBuildingDesktop,
                                variant = TourOSButtonVariant.PRIMARY,
                                modifier = Modifier.weight(1f)
                            )

                            TourOSButton(
                                text = "⬇️ Son Sürümü İndir",
                                onClick = {
                                    uriHandler.openUri(uiState.desktopDownloadUrl)
                                },
                                variant = TourOSButtonVariant.SECONDARY,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // ── 2. WEB APP (WASM / PRODUCTION) YÖNETİMİ ──
                Surface(
                    modifier = Modifier.weight(1f, fill = false).widthIn(min = 340.dp, max = 600.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = TourOSColors.Surface,
                    border = BorderStroke(1.dp, TourOSColors.SurfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = TourOSColors.Success.copy(alpha = 0.12f),
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("🌐", fontSize = 22.sp)
                                }
                            }
                            Column {
                                Text(
                                    "Web Uygulaması (Wasm / SPA)",
                                    style = TourOSTypography.TitleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    "axileto.com & GitHub Pages Yayını",
                                    style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary)
                                )
                            }
                        }

                        HorizontalDivider(color = TourOSColors.SurfaceVariant.copy(alpha = 0.5f))

                        // Durum Bilgileri
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = TourOSColors.Background,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text("Dağıtım Sunucuları:", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                                    Text("Yandex Server & GH Pages", style = TourOSTypography.Caption.copy(fontWeight = FontWeight.Bold))
                                }
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text("Yayın Durumu:", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                                    Text("🟢 " + uiState.lastWebDeployTime, style = TourOSTypography.Caption.copy(color = TourOSColors.Success, fontWeight = FontWeight.Bold))
                                }
                            }
                        }

                        // Butonlar
                        TourOSButton(
                            text = if (uiState.isDeployingWeb) "⏳ Dağıtılıyor..." else "🌐 Web Sürümünü Canlıya Al (Deploy)",
                            onClick = {
                                viewModel.triggerWebDeploy()
                                if (uiState.githubToken.isBlank()) {
                                    uriHandler.openUri("${uiState.githubActionsUrl}/workflows/deploy-web.yml")
                                }
                            },
                            enabled = !uiState.isDeployingWeb,
                            variant = TourOSButtonVariant.PRIMARY,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            TourOSButton(
                                text = "🔗 axileto.com Aç",
                                onClick = { uriHandler.openUri(uiState.liveWebUrl) },
                                variant = TourOSButtonVariant.SECONDARY,
                                modifier = Modifier.weight(1f)
                            )
                            TourOSButton(
                                text = "🔗 GitHub Pages Aç",
                                onClick = { uriHandler.openUri(uiState.githubPagesUrl) },
                                variant = TourOSButtonVariant.SECONDARY,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // ── 3. GİTHUB ACTIONS & CI/CD KONTROL PANELİ ──
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = TourOSColors.Surface,
                border = BorderStroke(1.dp, TourOSColors.SurfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("⚙️", fontSize = 22.sp)
                            Column {
                                Text("GitHub Actions Entegrasyonu", style = TourOSTypography.TitleMedium.copy(fontWeight = FontWeight.Bold))
                                Text("Repo: ${uiState.githubRepo}", style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary))
                            }
                        }

                        TourOSButton(
                            text = "📊 Actions Konsolunu Aç",
                            onClick = { uriHandler.openUri(uiState.githubActionsUrl) },
                            variant = TourOSButtonVariant.SECONDARY
                        )
                    }

                    TourOSTextField(
                        value = uiState.githubToken,
                        onValueChange = { viewModel.updateGithubToken(it) },
                        label = "GitHub Kişisel Erişim Tokeni (Opsiyonel - Doğrudan API Tetikleme için)",
                        placeholder = "ghp_...",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
