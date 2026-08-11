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
import androidx.compose.ui.unit.sp
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
 * TourOS 0.3 Tasarım Sistemine tam uyumlu Kayıt Ekranı.
 */
@Composable
fun RegisterScreen(
    onRegisterSuccess: (UserRole) -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: AuthViewModel = koinViewModel()
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            val user = (uiState as AuthUiState.Success).user
            onRegisterSuccess(user.role)
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
                        text = "TourOS",
                        style = TourOSTypography.DisplaySmall.copy(color = TourOSColors.Primary)
                    )

                    Spacer(modifier = Modifier.height(TourOSSpacing.xSmall))

                    Text(
                        text = "Yeni hesap oluşturun",
                        style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary)
                    )
                }

                Spacer(modifier = Modifier.height(TourOSSpacing.xLarge))

                // Agency Referral Code Information Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                        .background(TourOSColors.PrimaryContainer.copy(alpha = 0.3f))
                        .padding(TourOSSpacing.medium)
                ) {
                    Column {
                        Text(
                            text = "Acente Kayıt Sistem Referans Kodu:",
                            style = TourOSTypography.Caption.copy(color = TourOSColors.Primary, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        )
                        Text(
                            text = "AGN-8492",
                            style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Primary, fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold)
                        )
                        Text(
                            text = "Kayıt sonrası Acente Girişi yaparken bu kodu kullanmanız gerekmektedir.",
                            style = TourOSTypography.Caption.copy(color = TourOSColors.TextSecondary, fontSize = 10.sp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(TourOSSpacing.medium))

                // Error Banner
                val errorMessage = localError ?: (uiState as? AuthUiState.Error)?.message
                if (errorMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(TourOSSpacing.cornerRadiusSmall))
                            .background(TourOSColors.ErrorContainer)
                            .padding(TourOSSpacing.medium)
                    ) {
                        Text(
                            text = errorMessage,
                            style = TourOSTypography.BodyMedium.copy(color = TourOSColors.Error)
                        )
                    }
                    Spacer(modifier = Modifier.height(TourOSSpacing.large))
                }

                // Input Fields
                TourOSTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = "Ad Soyad",
                    placeholder = "Ahmet Yılmaz",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(TourOSSpacing.medium))

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
                    label = "Şifre (Min. 6 karakter)",
                    placeholder = "••••••••",
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(TourOSSpacing.medium))

                TourOSTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = "Şifre Tekrarı",
                    placeholder = "••••••••",
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(TourOSSpacing.xLarge))

                // Primary Button
                TourOSButton(
                    text = "Kayıt Ol",
                    onClick = {
                        localError = null
                        if (password != confirmPassword) {
                            localError = "Şifreler birbiriyle eşleşmiyor"
                            return@TourOSButton
                        }
                        viewModel.register(email, password, fullName)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    variant = TourOSButtonVariant.PRIMARY,
                    enabled = fullName.isNotBlank() && email.isNotBlank() && password.isNotBlank() && confirmPassword.isNotBlank(),
                    isLoading = uiState is AuthUiState.Loading
                )

                Spacer(modifier = Modifier.height(TourOSSpacing.large))

                // Login Link
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Zaten hesabınız var mı? ",
                        style = TourOSTypography.BodyMedium.copy(color = TourOSColors.TextSecondary)
                    )
                    Text(
                        text = "Giriş Yap",
                        style = TourOSTypography.TitleMedium.copy(color = TourOSColors.Secondary),
                        modifier = Modifier.clickable { onNavigateToLogin() }
                    )
                }
            }
        }
    }
}
