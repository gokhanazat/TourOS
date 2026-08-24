package com.mgacreative.touros.domain.repository

import com.mgacreative.touros.domain.model.HelpGuide
import kotlinx.coroutines.flow.Flow

/**
 * Sayfa İçi Yardım Rehberi Repository Arayüzü.
 */
interface HelpGuideRepository {
    /**
     * Belirli bir ekran rotası ve dile göre aktif yardım rehberlerini getirir.
     */
    suspend fun getHelpGuidesForScreen(screenRoute: String, lang: String = "tr"): Result<List<HelpGuide>>

    /**
     * Tüm ekranlar için aktif yardım rehberlerini getirir.
     */
    suspend fun getAllHelpGuides(lang: String = "tr"): Result<List<HelpGuide>>
}
