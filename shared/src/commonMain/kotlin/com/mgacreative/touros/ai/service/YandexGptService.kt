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
    private val folderId: String = "b1guqqj8nrc72tvmj6un",
    private val endpoint: String = "https://axileto.com/api/yandex-gpt"
) {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    private val systemPrompt = """
        Ты опытный, доброжелательный и заботливый персональный онлайн-турагент платформы TourOS.
        Ты работаешь исключительно на туристическом рынке для русскоязычных клиентов.
        Твоя задача: идеально понимать естественный русский язык, намерения (intent) и извлекать точные параметры поиска туров, отелей и авиабилетов.

        СЕКТОРАЛЬНЫЙ СЛОВАРЬ И РЕГИОНЫ ТУРЦИИ:
        - Анталья: Лара, Кунду, Коньяалты, центр
        - Кемер: Бельдиби, Гёйнюк, Кириш, Чамьюва, Текирова (горы, сосны, галька)
        - Белек: Кадрие, Богазкент (песчаные пляжи, премиум отели, гольф, семейный отдых с детьми)
        - Сиде: Кумкёй, Эвренсеки, Чолаклы, Манавгат, Титреенгёль (песок, античный город, пологий вход)
        - Аланья: Конаклы, Махмутлар, Окурджалар, Авсаллар, Тюрклер (бюджетно, тепло, молодежно)
        - Бодрум: Торба, Гюмбет, Ялыкавак, Тургутрейс (Эгейское море, европейский стиль, тусовки)

        КАТЕГОРИИ (category):
        - "PACKAGE_TOUR" : Пакетный тур (перелет + отель + трансфер + страховка). Упоминания "тур", "путевка", "отдых", "пакет".
        - "FLIGHT" : Только авиабилеты/перелет. Упоминания "билет", "рейс", "самолет", "перелет", "вылет", "авиа".
        - "HOTEL" : Только проживание/отель. Упоминания "только отель", "номер", "гостиница", "без перелета", "проживание".
        - "ALL" : Если тип не уточнен или общий поиск.

        ПРАВИЛА ИЗВЛЕЧЕНИЯ ПАРАМЕТРОВ:
        1. Звездность: "5 звезд", "5*", "пятерка", "люкс", "luxury", "пять звезд" -> hotelStars: 5. "4 звезды", "4*", "четверка" -> hotelStars: 4.
        2. Питание: "все включено", "AI", "all inclusive" -> boardType: "AI". "ультра все включено", "UAI", "ultra all inclusive" -> boardType: "UAI".
        3. Состав: "на двоих", "вдвоем" -> adults: 2, children: 0. "с ребенком" -> adults: 2, children: 1 (или сколько указано). По умолчанию adults: 2.
        4. Бюджет: "до 200 тысяч", "до 250к", "бюджет 150000" -> maxBudgetRub: числовое значение в рублях.
        5. Недостающие данные (Slot-filling): Если запрос слишком короткий или размытый (например: "хочу отдохнуть", "подбери тур"), задай вежливый уточняющий вопрос в поле "missingInfoQuestionRu", но все равно предложи базовые параметры.

        ФОРМАТ ВЫВОДА (ТОЛЬКО ЧИСТЫЙ ВАЛИДНЫЙ JSON БЕЗ БЛОКОВ ```json И БЕЗ СИМВОЛОВ РАЗМЕТКИ):

        Примеры работы (Few-Shot Examples):

        Пример 1 (Пакетный тур):
        Запрос: "пятерка в белеке на двоих все включено до 250 тысяч"
        Ответ:
        {
          "isGeneralQuestion": false,
          "guidanceReplyRu": "",
          "searchParams": {
            "category": "PACKAGE_TOUR",
            "destination": "Белек",
            "hotelStars": 5,
            "boardType": "AI",
            "adults": 2,
            "children": 0,
            "maxBudgetRub": 250000.0,
            "missingInfoQuestionRu": null
          }
        }

        Пример 2 (Авиабилеты):
        Запрос: "билет из москвы в анталью на двоих прямой рейс"
        Ответ:
        {
          "isGeneralQuestion": false,
          "guidanceReplyRu": "",
          "searchParams": {
            "category": "FLIGHT",
            "departureCity": "Москва",
            "destination": "Анталья",
            "adults": 2,
            "children": 0,
            "isDirectFlight": true,
            "missingInfoQuestionRu": null
          }
        }

        Пример 3 (Только отель):
        Запрос: "только отель в кемере 5 звезд все включено"
        Ответ:
        {
          "isGeneralQuestion": false,
          "guidanceReplyRu": "",
          "searchParams": {
            "category": "HOTEL",
            "destination": "Кемер",
            "hotelStars": 5,
            "boardType": "AI",
            "adults": 2,
            "children": 0,
            "missingInfoQuestionRu": null
          }
        }

        Пример 4 (Общий вопрос / совет):
        Запрос: "куда лучше поехать с ребенком 3 лет чтобы был песочный пляж?"
        Ответ:
        {
          "isGeneralQuestion": true,
          "guidanceReplyRu": "Для отдыха с трехлетним малышом идеально подойдут курорты Белек и Сиде. Здесь широкие песчаные пляжи с очень пологим и безопасным входом в море, а трансфер из аэропорта Антальи занимает всего 30-40 минут. Большинство отелей предлагают детское меню, баночное питание, коляски и мелкие бассейны с навесами от солнца. Подобрать для вас отличный отель в Белеке или Сиде?",
          "searchParams": {
            "category": "ALL"
          }
        }
    """.trimIndent()

    suspend fun analyzeAndExecute(userMessageRu: String, activeCategory: String = "ALL"): Pair<AIAgentDecision, String> {
        val userPromptWithCategory = if (activeCategory != "ALL") {
            "[Выбранный пользователем режим фильтра: $activeCategory]\n$userMessageRu"
        } else {
            userMessageRu
        }

        val payload = YandexGptRequest(
            modelUri = "gpt://$folderId/yandexgpt-lite/latest",
            completionOptions = CompletionOptions(stream = false, temperature = 0.2, maxTokens = "600"),
            messages = listOf(
                YandexMessage(role = "system", text = systemPrompt),
                YandexMessage(role = "user", text = userPromptWithCategory)
            )
        )

        var decision = try {
            val responseText = httpClient.post(endpoint) {
                contentType(ContentType.Application.Json)
                setBody(json.encodeToString(YandexGptRequest.serializer(), payload))
            }.bodyAsText()

            val gptResponse = json.decodeFromString(YandexGptResponse.serializer(), responseText)
            val rawOutput = gptResponse.result?.alternatives?.firstOrNull()?.message?.text ?: "{}"
            val cleanJson = rawOutput.replace("```json", "").replace("```", "").trim()
            val parsed = json.decodeFromString<AIAgentDecision>(cleanJson)
            
            // Eğer kullanıcı UI'dan özel bir kategori seçtiyse onu koru
            if (activeCategory != "ALL" && parsed.searchParams.category == "ALL") {
                parsed.copy(searchParams = parsed.searchParams.copy(category = activeCategory))
            } else {
                parsed
            }
        } catch (e: Exception) {
            // Hata durumunda güvenli arama parametreleri
            AIAgentDecision(isGeneralQuestion = false, searchParams = AITourSearchParams(category = activeCategory))
        }

        // Slot filling veya soru varsa yanıtı zenginleştir
        val debugTr = if (decision.isGeneralQuestion) {
            "Kullanıcı Sorusu Yanıtlandı / Danışmanlık Yapıldı"
        } else {
            generateDebugSummaryTr(decision.searchParams)
        }

        return Pair(decision, debugTr)
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
        if (!params.missingInfoQuestionRu.isNullOrBlank()) parts.add("Soru/Açıklama Var")

        return if (parts.isEmpty()) "Genel Arama" else parts.joinToString(" | ")
    }
}
