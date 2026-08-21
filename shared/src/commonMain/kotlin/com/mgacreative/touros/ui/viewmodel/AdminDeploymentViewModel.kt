package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.SupabaseClient
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

data class AdminDeploymentUiState(
    val isBuildingDesktop: Boolean = false,
    val isDeployingWeb: Boolean = false,
    val statusMessage: String? = null,
    val errorMessage: String? = null,
    val githubRepo: String = "gokhanazat/TourOS",
    val githubToken: String = "",
    val lastDesktopBuildTime: String = "v1.0.0 (Son Sürüm)",
    val lastWebDeployTime: String = "Canlıda (axileto.com & GitHub Pages)",
    val liveWebUrl: String = "https://axileto.com",
    val githubPagesUrl: String = "https://gokhanazat.github.io/TourOS/",
    val desktopDownloadUrl: String = "https://github.com/gokhanazat/TourOS/releases",
    val githubActionsUrl: String = "https://github.com/gokhanazat/TourOS/actions"
)

class AdminDeploymentViewModel(
    private val supabaseClient: SupabaseClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminDeploymentUiState())
    val uiState: StateFlow<AdminDeploymentUiState> = _uiState.asStateFlow()

    private val httpClient by lazy {
        HttpClient {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true; isLenient = true })
            }
        }
    }

    fun updateGithubToken(token: String) {
        _uiState.update { it.copy(githubToken = token) }
    }

    fun triggerDesktopBuild(versionTag: String = "v1.0.0") {
        viewModelScope.launch {
            _uiState.update { it.copy(isBuildingDesktop = true, statusMessage = null, errorMessage = null) }
            val repo = _uiState.value.githubRepo
            val token = _uiState.value.githubToken.trim()

            if (token.isNotBlank()) {
                try {
                    val response = httpClient.post("https://api.github.com/repos/$repo/actions/workflows/build-desktop.yml/dispatches") {
                        header("Authorization", "Bearer $token")
                        header("Accept", "application/vnd.github+json")
                        header("X-GitHub-Api-Version", "2022-11-28")
                        contentType(ContentType.Application.Json)
                        setBody(
                            buildJsonObject {
                                put("ref", "master")
                                putJsonObject("inputs") {
                                    put("version_tag", versionTag)
                                }
                            }.toString()
                        )
                    }

                    if (response.status.isSuccess()) {
                        _uiState.update {
                            it.copy(
                                isBuildingDesktop = false,
                                lastDesktopBuildTime = "Şimdi Tetiklendi ($versionTag)",
                                statusMessage = "🚀 Masaüstü EXE derleme işi GitHub Actions üzerinde başlatıldı! (~3-5 dk sürebilir)"
                            )
                        }
                    } else {
                        val body = response.bodyAsText()
                        _uiState.update {
                            it.copy(
                                isBuildingDesktop = false,
                                errorMessage = "⚠️ GitHub API Yanıtı (${response.status.value}): $body"
                            )
                        }
                    }
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            isBuildingDesktop = false,
                            errorMessage = "❌ Bağlantı hatası: ${e.message}"
                        )
                    }
                }
            } else {
                // Token girilmemişse yönlendirme & simülasyon modu
                kotlinx.coroutines.delay(1000)
                _uiState.update {
                    it.copy(
                        isBuildingDesktop = false,
                        statusMessage = "ℹ️ GitHub Actions paneline yönlendiriliyorsunuz. 'Run workflow' ile derlemeyi başlatabilirsiniz."
                    )
                }
            }
        }
    }

    fun triggerWebDeploy() {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeployingWeb = true, statusMessage = null, errorMessage = null) }
            val repo = _uiState.value.githubRepo
            val token = _uiState.value.githubToken.trim()

            if (token.isNotBlank()) {
                try {
                    val response = httpClient.post("https://api.github.com/repos/$repo/actions/workflows/deploy-web.yml/dispatches") {
                        header("Authorization", "Bearer $token")
                        header("Accept", "application/vnd.github+json")
                        header("X-GitHub-Api-Version", "2022-11-28")
                        contentType(ContentType.Application.Json)
                        setBody(
                            buildJsonObject {
                                put("ref", "master")
                            }.toString()
                        )
                    }

                    if (response.status.isSuccess()) {
                        _uiState.update {
                            it.copy(
                                isDeployingWeb = false,
                                lastWebDeployTime = "Şimdi Dağıtıldı",
                                statusMessage = "🌐 Web Deploy (axileto.com & GH Pages) başarıyla tetiklendi!"
                            )
                        }
                    } else {
                        val body = response.bodyAsText()
                        _uiState.update {
                            it.copy(
                                isDeployingWeb = false,
                                errorMessage = "⚠️ GitHub API Yanıtı (${response.status.value}): $body"
                            )
                        }
                    }
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            isDeployingWeb = false,
                            errorMessage = "❌ Bağlantı hatası: ${e.message}"
                        )
                    }
                }
            } else {
                kotlinx.coroutines.delay(1000)
                _uiState.update {
                    it.copy(
                        isDeployingWeb = false,
                        statusMessage = "ℹ️ GitHub Actions paneline yönlendiriliyorsunuz. 'Deploy WebApp' akışını tetikleyebilirsiniz."
                    )
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(statusMessage = null, errorMessage = null) }
    }
}
