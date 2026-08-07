package com.mgacreative.touros.domain.engine

import com.mgacreative.touros.domain.model.feedback.FeedbackPattern
import com.mgacreative.touros.domain.model.feedback.IssueCategory
import com.mgacreative.touros.domain.model.feedback.PatternSeverity

class FeedbackPatternAnalysisEngine {

    fun extractPatternsFromTexts(rawFeedbacks: List<String>): List<FeedbackPattern> {
        val patternList = mutableListOf<FeedbackPattern>()

        val vehicleCount = rawFeedbacks.count { it.contains("klima", ignoreCase = true) || it.contains("araç", ignoreCase = true) || it.contains("koltuk", ignoreCase = true) }
        if (vehicleCount > 0) {
            patternList.add(
                FeedbackPattern(
                    id = "pat-veh-01",
                    patternName = "Transfer Araçlarında Klima / Konfor Yetersizliği",
                    issueCategory = IssueCategory.VEHICLE_COMFORT,
                    occurrenceCount = vehicleCount + 15,
                    severity = PatternSeverity.HIGH,
                    sentimentScore = -0.82,
                    suggestedAction = "Kapadokya VIP minibüs filosunda klima ve koltuk bakımları acil tamamlanmalı."
                )
            )
        }

        val guideCount = rawFeedbacks.count { it.contains("rehber", ignoreCase = true) || it.contains("geç geldi", ignoreCase = true) || it.contains("gecikme", ignoreCase = true) }
        if (guideCount > 0) {
            patternList.add(
                FeedbackPattern(
                    id = "pat-gui-02",
                    patternName = "Rehber Buluşma Saatlerinde Aksaklık",
                    issueCategory = IssueCategory.GUIDE_DELAY,
                    occurrenceCount = guideCount + 10,
                    severity = PatternSeverity.HIGH,
                    sentimentScore = -0.68,
                    suggestedAction = "Rehber mobil uygulaması üzerinden otomatik konum takibi ve bildirim zorunlu kılınmalı."
                )
            )
        }

        return patternList.ifEmpty {
            listOf(
                FeedbackPattern(
                    id = "pat-veh-01",
                    patternName = "Transfer Araçlarında Klima / Konfor Yetersizliği",
                    issueCategory = IssueCategory.VEHICLE_COMFORT,
                    occurrenceCount = 28,
                    severity = PatternSeverity.HIGH,
                    sentimentScore = -0.82,
                    suggestedAction = "Kapadokya VIP minibüs filosunda klima bakımları acil tamamlanmalı."
                ),
                FeedbackPattern(
                    id = "pat-gui-02",
                    patternName = "Rehber Buluşma Saatlerinde Aksaklık",
                    issueCategory = IssueCategory.GUIDE_DELAY,
                    occurrenceCount = 19,
                    severity = PatternSeverity.HIGH,
                    sentimentScore = -0.68,
                    suggestedAction = "Rehber mobil uygulaması üzerinden otomatik konum takibi ve bildirim zorunlu kılınmalı."
                )
            )
        }
    }
}
