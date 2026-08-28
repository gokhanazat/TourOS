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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.mgacreative.touros.ui.viewmodel.ForgotPasswordUiState
import com.mgacreative.touros.ui.viewmodel.ForgotPasswordViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * TourOS 0.3 Tasarım Sistemine tam uyumlu Şifremi Unuttum Ekranı.
 * - Sadece açık tema (Surface arka plan)
 * - Max 420dp ortalanmış kurumsal kart
 * - Single e-posta alanı & Primary buton
 */
@Composable
fun ForgotPasswordScreen(
    onNavigateToLogin: () -> Unit,
    viewModel: ForgotPasswordViewModel = koinViewModel()
) {
    var email by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TourOSColors.Surface),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 440.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(TourOSSpacing.large),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            TourOSCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = TourOSColors.Background,
                borderColor = TourOSColors.Border,
                contentPadding = TourOSSpacing.xxLarge
            ) {
                // Header
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
                        text = "Şifremi Unuttum",
                        style = TourOSTypography.TitleLarge.copy(color = TourOSColors.Primary)
                    )

                    Spacer(modifier = Modifier.height(TourOSSpacing.xSmall))

                    Text(
                        text = "Hesabınıza kayıtlı e-posta adresinizi girin. Şifre sıfırlama bağlantısı göndereceğiz.",
                        style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary)
                    )
                }

                Spacer(modifier = Modifier.height(TourOSSpacing.xLarge))

                // State Banner (Error / Success)
                when (val state = uiState) {
                    is ForgotPasswordUiState.Error -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                                .background(TourOSColors.ErrorContainer)
                                .padding(TourOSSpacing.medium)
                        ) {
                            Text(
                                text = state.message,
                                style = TourOSTypography.BodyMedium.copy(color = TourOSColors.Error)
                            )
                        }
                        Spacer(modifier = Modifier.height(TourOSSpacing.large))
                    }
                    is ForgotPasswordUiState.EmailSent -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                                .background(TourOSColors.SuccessContainer)
                                .padding(TourOSSpacing.medium)
                        ) {
                            Text(
                                text = "Şifre sıfırlama bağlantısı e-posta adresinize gönderildi. Lütfen gelen kutunuzu kontrol edin.",
                                style = TourOSTypography.BodyMedium.copy(color = TourOSColors.Success)
                            )
                        }
                        Spacer(modifier = Modifier.height(TourOSSpacing.large))
                    }
                    else -> {}
                }

                // Form Fields & Action Button
                if (uiState !is ForgotPasswordUiState.EmailSent) {
                    TourOSTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "E-posta Adresi",
                        placeholder = "ornek@touros.com",
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(TourOSSpacing.xLarge))

                    TourOSButton(
                        text = "Sıfırlama Bağlantısı Gönder",
                        onClick = { viewModel.sendResetEmail(email) },
                        modifier = Modifier.fillMaxWidth(),
                        variant = TourOSButtonVariant.PRIMARY,
                        enabled = email.isNotBlank(),
                        isLoading = uiState is ForgotPasswordUiState.Loading
                    )

                    Spacer(modifier = Modifier.height(TourOSSpacing.large))
                }

                // Back to Login Link
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Hatırladınız mı? ",
                        style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary)
                    )
                    Text(
                        text = "Giriş Ekranına Dön",
                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Secondary),
                        modifier = Modifier.clickable { onNavigateToLogin() }
                    )
                }
            }
        }
    }
}
