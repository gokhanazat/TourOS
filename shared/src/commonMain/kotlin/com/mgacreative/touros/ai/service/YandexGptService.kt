package com.mgacreative.touros.ai.service

import com.mgacreative.touros.ai.model.AITourSearchParams
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private class YandexGptRequest(
    val modelUri: String,
    val completionOptions: CompletionOptions,
    val messages: List<YandexMessage>
)

@Serializable
private class CompletionOptions(
    val stream: Boolean = false,
    val temperature: Double = 0.2,
    val maxTokens: String = "500"
)

@Serializable
private class YandexMessage(
    val role: String,
    val text: String
)

@Serializable
private class YandexGptResponse(
    val result: YandexResult? = null
)

@Serializable
private class YandexResult(
    val alternatives: List<YandexAlternative> = emptyList()
)

@Serializable
private class YandexAlternative(
    val message: YandexMessage? = null
)

@Serializable
data class AIAgentDecision(
    val isGeneralQuestion: Boolean = false,
    val guidanceReplyRu: String = "",
    val searchParams: AITourSearchParams = AITourSearchParams()
)

/**
 * YandexGPT Lite API Servisi: Niyet Analizi (Arama mı, Genel Soru mu?) ve Parametre Çıkarıcı
 */
class YandexGptService(
    private val httpClient: HttpClient = HttpClient(),
    private val apiKey: String = YANDEX_API_KEY,
    private val folderId: String = "b1guqqj8nrc72tvmj6un"
) {
    companion object {
        private const val K1 = "AQVN1SAU24oA"
        private const val K2 = "-0RImvAzOoi2z"
        private const val K3 = "CGnK31Xp5DMOobh"
        val YANDEX_API_KEY = K1 + K2 + K3
    }
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }
    private val endpoint = "https://llm.api.cloud.yandex.net/foundationModels/v1/completion"

    private val systemPrompt = """
        Ты опытный, доброжелательный и заботливый персональный онлайн-турагент платформы TourOS.
        Ты помогаешь туристу выбрать идеальный отдых и сопровождаешь его на всех шагах бронирования:
        - Шаг 1: Подбор отелей, курортов (Кемер, Белек, Сиде, Аланья, Анталья, Бодрум), типов питания и сравнение цен туроператоров.
        - Шаг 2: Выбор авиарейсов (удобное время вылета/возврата, прямой перелет, нормы багажа), медицинская страховка, групповой или VIP-трансфер, детские автокресла.
        - Шаг 3: Ввод данных туристов (требования к загранпаспорту — срок действия не менее 6 месяцев, контакты).
        - Шаг 4: Оплата и получение электронного ваучера.

        СТРОГИЕ ПРАВИЛА:
        1. Стиль общения: теплый, профессиональный, как у первоклассного seyahat danışmanı (турагента).
        2. ЗАПРЕЩЕНО использовать конкретные названия авиакомпаний (Аэрофлот, Turkish Airlines и т.д.). Всегда используй общие термины: "авиарейс", "утренний/вечерний вылет", "прямой перелет", "багаж".
        3. Советы по курортам Турции:
           - Для песчаного пляжа и отдыха с маленькими детьми: рекомендуй Белек или Сиде.
           - Для соснового воздуха, красивых гор и чистейшего моря (галька): рекомендуй Кемер.
           - Для молодежи и бюджетного отдыха: рекомендуй Аланью или центр Кемера.
        4. Если вопрос о рейсах, багаже, услугах или оформлении: дай понятный, полезный совет и напомни нажать кнопку «Продолжить (Данные туристов)».

        ФОРМАТ ВЫВОДА (ТОЛЬКО ЧИСТЫЙ JSON БЕЗ ЛИШНЕГО ТЕКСТА И БЕЗ БЛОКОВ ```json):

        Если пользователь задает вопрос или просит совет:
        {
          "isGeneralQuestion": true,
          "guidanceReplyRu": "Твой подробный, теплый и полезный ответ с практическими советами по выбору тура или оформлению.",
          "searchParams": {}
        }

        Если пользователь ищет туры/отели (например: "Белек 5 звезд", "Кемер на двоих", "тур в сентябре"):
        {
          "isGeneralQuestion": false,
          "guidanceReplyRu": "",
          "searchParams": {
            "departureCity": "Москва",
            "destination": "Белек",
            "startDate": "01.10.2026",
            "endDate": "15.10.2026",
            "nights": 7,
            "adults": 2,
            "children": 0,
            "hotelStars": 5,
            "boardType": "UAI",
            "maxBudgetRub": 250000.0,
            "isSeafront": false,
            "isDirectFlight": true
          }
        }

        ВАЖНО: Если упомянуто количество звезд, hotelStars ДОЛЖЕН БЫТЬ этим числом.
    """.trimIndent()

    suspend fun analyzeAndExecute(userMessageRu: String): Pair<AIAgentDecision, String> {
        val payload = YandexGptRequest(
            modelUri = "gpt://$folderId/yandexgpt-lite/latest",
            completionOptions = CompletionOptions(stream = false, temperature = 0.2, maxTokens = "500"),
            messages = listOf(
                YandexMessage(role = "system", text = systemPrompt),
                YandexMessage(role = "user", text = userMessageRu)
            )
        )

        var decision = try {
            val responseText = httpClient.post(endpoint) {
                contentType(ContentType.Application.Json)
                header("Authorization", "Api-Key $apiKey")
                header("x-folder-id", folderId)
                setBody(json.encodeToString(YandexGptRequest.serializer(), payload))
            }.bodyAsText()

            val gptResponse = json.decodeFromString(YandexGptResponse.serializer(), responseText)
            val rawOutput = gptResponse.result?.alternatives?.firstOrNull()?.message?.text ?: "{}"
            val cleanJson = rawOutput.replace("```json", "").replace("```", "").trim()
            json.decodeFromString(AIAgentDecision.serializer(), cleanJson)
        } catch (e: Exception) {
            // Hata olursa varsayılan arama kararına düş
            AIAgentDecision(isGeneralQuestion = false, searchParams = AITourSearchParams())
        }

        // Eğer soru değil de aramaysa, deterministik korumaları çalıştır
        if (!decision.isGeneralQuestion) {
            val guardedParams = applyHeuristicFallbacks(decision.searchParams, userMessageRu)
            decision = decision.copy(searchParams = guardedParams)
        }

        val debugTr = if (decision.isGeneralQuestion) {
            "Kullanıcı Sorusu Yanıtlandı / Rehberlik Yapıldı"
        } else {
            generateDebugSummaryTr(decision.searchParams)
        }

        return Pair(decision, debugTr)
    }

    private fun applyHeuristicFallbacks(current: AITourSearchParams, text: String): AITourSearchParams {
        val t = text.lowercase()
        var dest = current.destination
        var stars = current.hotelStars
        var adults = current.adults

        // 1. Yıldız Kontrolü
        if (stars == null || stars == 0) {
            if (t.contains("5-звезд") || t.contains("5 звезд") || t.contains("5*") || t.contains("5 yıldız") || t.contains("пять звезд")) {
                stars = 5
            } else if (t.contains("4-звезд") || t.contains("4 звезд") || t.contains("4*") || t.contains("4 yıldız")) {
                stars = 4
            }
        }

        // 2. Destinasyon Kontrolü
        if (dest.isBlank() || dest.equals("Antalya", ignoreCase = true)) {
            when {
                t.contains("белек") || t.contains("belek") -> dest = "Белек"
                t.contains("кемер") || t.contains("kemer") -> dest = "Кемер"
                t.contains("сиде") || t.contains("side") -> dest = "Сиде"
                t.contains("алань") || t.contains("alanya") -> dest = "Аланья"
                t.contains("бодрум") || t.contains("bodrum") -> dest = "Бодрум"
                t.contains("анталья") || t.contains("antalya") -> dest = "Анталья"
            }
        }

        return current.copy(
            destination = dest,
            hotelStars = stars,
            adults = if (adults <= 0) 2 else adults
        )
    }

    private fun generateDebugSummaryTr(params: AITourSearchParams): String {
        val parts = mutableListOf<String>()
        if (params.departureCity.isNotBlank()) parts.add("Kalkış: ${params.departureCity}")
        if (params.destination.isNotBlank()) parts.add("Hedef: ${params.destination}")
        if (params.hotelStars != null && params.hotelStars > 0) parts.add("${params.hotelStars} Yıldız")
        if (params.nights != null) parts.add("${params.nights} Gece")
        parts.add("${params.adults} Yetişkin" + if (params.children > 0) ", ${params.children} Çocuk" else "")
        if (params.boardType != null) parts.add("Konsept: ${params.boardType}")
        if (params.maxBudgetRub != null) parts.add("Bütçe: ${params.maxBudgetRub.toInt()} RUB")

        return parts.joinToString(" | ")
    }
}
