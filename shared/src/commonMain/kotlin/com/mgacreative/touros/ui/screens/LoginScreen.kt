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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.mgacreative.touros.domain.model.UserRole
import com.mgacreative.touros.ui.components.TourOSButton
import com.mgacreative.touros.ui.components.TourOSButtonVariant
import com.mgacreative.touros.ui.components.TourOSCard
import com.mgacreative.touros.ui.components.TourOSTextField
import com.mgacreative.touros.ui.theme.TourOSColors
import com.mgacreative.touros.ui.theme.TourOSSpacing
import com.mgacreative.touros.ui.theme.TourOSTypography
import com.mgacreative.touros.ui.viewmodel.AuthUiState
import com.mgacreative.touros.ui.viewmodel.AuthViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * TourOS 0.3 Tasarım Sistemine tam uyumlu Giriş Ekranı.
 * - Sadece açık tema (Surface arka plan)
 * - Max 420dp ortalanmış kurumsal kart
 * - TourOSColors (Primary: #1F4E5F, Secondary: #C97A2B)
 */
@Composable
fun LoginScreen(
    onLoginSuccess: (UserRole) -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit = {},
    viewModel: AuthViewModel = koinViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var agencyCode by remember { mutableStateOf("") }

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            val user = (uiState as AuthUiState.Success).user
            onLoginSuccess(user.role)
            viewModel.resetState()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TourOSColors.Surface),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(TourOSSpacing.large),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            TourOSCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 420.dp),
                backgroundColor = TourOSColors.Background,
                borderColor = TourOSColors.Border,
                contentPadding = TourOSSpacing.xxLarge
            ) {
                // Logo & Header
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                            .background(TourOSColors.Primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "T",
                            style = TourOSTypography.DisplaySmall.copy(color = TourOSColors.OnPrimary)
                        )
                    }

                    Spacer(modifier = Modifier.height(TourOSSpacing.medium))

                    Text(
                        text = "TourOS",
                        style = TourOSTypography.DisplaySmall.copy(color = TourOSColors.Primary)
                    )

                    Spacer(modifier = Modifier.height(TourOSSpacing.xSmall))

                    Text(
                        text = "Hesabınıza giriş yapın",
                        style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary)
                    )
                }

                Spacer(modifier = Modifier.height(TourOSSpacing.xLarge))

                // Error Banner (Anlaşılır & Şık Uyarı Kutusu)
                if (uiState is AuthUiState.Error) {
                    val rawMsg = (uiState as AuthUiState.Error).message
                    val displayMsg = if (rawMsg.contains("invalid_credentials") || rawMsg.contains("grant_type") || rawMsg.contains("Headers:")) {
                        "E-posta adresi veya şifre hatalı. Lütfen bilgilerinizi kontrol edip tekrar deneyin."
                    } else {
                        rawMsg
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                            .background(TourOSColors.SecondaryContainer.copy(alpha = 0.5f))
                            .padding(TourOSSpacing.medium)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(TourOSSpacing.small),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("⚠️", style = TourOSTypography.TitleMedium)
                            Text(
                                text = displayMsg,
                                style = TourOSTypography.Label.copy(color = TourOSColors.Secondary),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(TourOSSpacing.large))
                }


                // Form Fields
                TourOSTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "E-posta Adresi",
                    placeholder = "ornek@touros.com",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(TourOSSpacing.medium))

                TourOSTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Şifre",
                    placeholder = "••••••••",
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(TourOSSpacing.medium))

                TourOSTextField(
                    value = agencyCode,
                    onValueChange = { agencyCode = it },
                    label = "Acente Kodu (B2B SaaS)",
                    placeholder = "Örn: AGN-ANEX / ACT-001",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(TourOSSpacing.small))

                // Forgot Password Link
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        text = "Şifremi Unuttum?",
                        style = TourOSTypography.Label.copy(color = TourOSColors.Secondary),
                        modifier = Modifier.clickable { onNavigateToForgotPassword() }
                    )
                }

                Spacer(modifier = Modifier.height(TourOSSpacing.xLarge))

                // Primary Submit Button
                TourOSButton(
                    text = "Giriş Yap",
                    onClick = { viewModel.login(email, password) },
                    modifier = Modifier.fillMaxWidth(),
                    variant = TourOSButtonVariant.PRIMARY,
                    enabled = email.isNotBlank() && password.isNotBlank(),
                    isLoading = uiState is AuthUiState.Loading
                )

                Spacer(modifier = Modifier.height(TourOSSpacing.large))

                // Register Link (Secondary Metin Stili)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Hesabınız yok mu? ",
                        style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary)
                    )
                    Text(
                        text = "Kayıt Ol",
                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Secondary),
                        modifier = Modifier.clickable { onNavigateToRegister() }
                    )
                }
            }
        }
    }
}
