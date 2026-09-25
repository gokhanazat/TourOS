package com.mgacreative.touros.ai.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.ai.adapter.AITourSearchAdapter
import com.mgacreative.touros.ai.model.AIAssistantMessage
import com.mgacreative.touros.ai.model.AITourSearchParams
import com.mgacreative.touros.ai.service.YandexGptService
import com.mgacreative.touros.data.database.entity.UnifiedProductEntity
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AIAssistantUiState(
    val isOpen: Boolean = false,
    val isLoading: Boolean = false,
    val selectedCategory: String = "ALL", // "ALL", "PACKAGE_TOUR", "FLIGHT", "HOTEL"
    val messages: List<AIAssistantMessage> = emptyList(),
    val currentSuggestedProducts: List<UnifiedProductEntity> = emptyList(),
    val currentGroupedHotels: List<com.mgacreative.touros.ai.model.AIGroupedHotelOffer> = emptyList(),
    val isDebugPanelVisible: Boolean = false, // Geliştirici için Türkçe panel
    val errorMessage: String? = null,
    val isCompactMode: Boolean = false // 2. ve 3. adımlarda true (%50 kompakt boyut)
)

class AIAssistantViewModel(
    private val yandexGptService: YandexGptService,
    private val supabaseClient: SupabaseClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(AIAssistantUiState())
    val uiState: StateFlow<AIAssistantUiState> = _uiState.asStateFlow()

    private var cachedCatalog: List<UnifiedProductEntity> = emptyList()

    init {
        loadCatalogFromDatabase()
        // Karşılama mesajı (Kişisel Tur Danışmanı kimliğiyle)
        val welcomeMsg = AIAssistantMessage(
            id = "msg-welcome",
            sender = "AGENT",
            textRu = "Здравствуйте! Я ваш персональный онлайн-турагент TourOS.\nВыберите категорию (Пакетные туры, Авиабилеты, Отели) или напишите свободный запрос на русском языке. Я подберу лучшие варианты и помогу забронировать!",
            debugTranslationTr = "Merhaba! Ben kişisel TourOS online seyahat danışmanınızım. Kategori seçebilir veya serbest arama yapabilirsiniz (Paket tur, Uçak bileti, Otel)."
        )
        _uiState.value = _uiState.value.copy(messages = listOf(welcomeMsg))
    }

    fun toggleChat() {
        _uiState.value = _uiState.value.copy(isOpen = !_uiState.value.isOpen)
    }

    fun toggleDebugPanel() {
        _uiState.value = _uiState.value.copy(isDebugPanelVisible = !_uiState.value.isDebugPanelVisible)
    }

    /**
     * Kategori haplarına (Pills) tıklandığında kategoriyi ayarlar
     */
    fun selectCategory(category: String) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    /**
     * Sohbet geçmişini ve arama sonuçlarını temizler, yeni aramaya hazırlar.
     */
    fun clearChat() {
        val welcomeMsg = AIAssistantMessage(
            id = "msg-welcome-${currentTimeMillis()}",
            sender = "AGENT",
            textRu = "Здравствуйте! Я ваш персональный онлайн-турагент TourOS.\nНапишите ваши пожелания по туру, авиабилету или отелю!",
            debugTranslationTr = "Sohbet sıfırlandı. Danışman yeni sorular için hazır."
        )
        _uiState.value = _uiState.value.copy(
            messages = listOf(welcomeMsg),
            currentSuggestedProducts = emptyList(),
            currentGroupedHotels = emptyList(),
            errorMessage = null,
            isCompactMode = false
        )
    }

    fun sendMessage(userTextRu: String) {
        if (userTextRu.isBlank() || _uiState.value.isLoading) return

        val userMsg = AIAssistantMessage(
            id = "msg-${currentTimeMillis()}-user",
            sender = "USER",
            textRu = userTextRu.trim()
        )

        val activeCategory = _uiState.value.selectedCategory
        val updatedMessages = _uiState.value.messages + userMsg
        _uiState.value = _uiState.value.copy(
            messages = updatedMessages,
            isLoading = true,
            errorMessage = null
        )

        viewModelScope.launch {
            try {
                // 1. YandexGPT'den Niyet ve Parametre Kararını Al (Aktif Kategori ile)
                val (decision, debugTr) = yandexGptService.analyzeAndExecute(userTextRu, activeCategory)

                // 2. Eğer kullanıcı genel bir soru sorduysa doğrudan cevap ver (Arama yapma)
                if (decision.isGeneralQuestion && decision.guidanceReplyRu.isNotBlank()) {
                    val guidanceAgentMsg = AIAssistantMessage(
                        id = "msg-${currentTimeMillis()}-guide",
                        sender = "AGENT",
                        textRu = decision.guidanceReplyRu,
                        debugTranslationTr = "Rehberlik Yanıtı: $debugTr"
                    )
                    _uiState.value = _uiState.value.copy(
                        messages = _uiState.value.messages + guidanceAgentMsg,
                        isLoading = false
                    )
                    return@launch
                }

                // 3. Arama Talebi İse: Gerçek veritabanı ürünlerini filtrele
                val params = decision.searchParams
                if (cachedCatalog.isEmpty()) {
                    loadCatalogFromDatabase()
                }

                val matchedProducts = AITourSearchAdapter.filterProducts(cachedCatalog, params)
                val groupedHotels = AITourSearchAdapter.groupProductsByHotel(matchedProducts).take(12)

                val replyTextRu = if (!params.missingInfoQuestionRu.isNullOrBlank()) {
                    params.missingInfoQuestionRu
                } else if (groupedHotels.isNotEmpty()) {
                    when (params.category.uppercase()) {
                        "FLIGHT" -> "Я нашел для вас ${groupedHotels.size} подходящих авиарейсов. Нажмите на рейс для просмотра деталей и перейдите к бронированию:"
                        "HOTEL" -> "Я нашел для вас ${groupedHotels.size} отличных отелей. Выберите отель и нажмите «Забронировать номер»:"
                        else -> "Я нашел для вас ${groupedHotels.size} отличных предложений. Нажмите на карточку, чтобы сравнить цены операторов и оформить бронирование:"
                    }
                } else {
                    "К сожалению, по вашим критериям ничего не найдено. Попробуйте изменить параметры или выбрать другую категорию."
                }

                val categoryTr = when (params.category.uppercase()) {
                    "FLIGHT" -> "Uçuş Arama"
                    "HOTEL" -> "Sadece Otel"
                    "PACKAGE_TOUR" -> "Paket Tur"
                    else -> "Tüm Kategoriler"
                }

                val agentReplyMsg = AIAssistantMessage(
                    id = "msg-${currentTimeMillis()}-agent",
                    sender = "AGENT",
                    textRu = replyTextRu,
                    debugTranslationTr = if (!params.missingInfoQuestionRu.isNullOrBlank()) {
                        "Eksik Bilgi Tamamlama Sorusu Soruldu ($debugTr)"
                    } else {
                        "Kategori: $categoryTr | Sonuç: ${groupedHotels.size} seçenek bulundu. [${matchedProducts.size} toplam teklif] ($debugTr)"
                    },
                    extractedParams = params,
                    foundProductsCount = groupedHotels.size
                )

                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages + agentReplyMsg,
                    currentSuggestedProducts = matchedProducts,
                    currentGroupedHotels = groupedHotels,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Ошибка обработки: ${e.message}"
                )
            }
        }
    }

    /**
     * Kullanıcı bir tur seçtiğinde 2. aşama (Uçuş/Ek Hizmetler) için zengin AI rehberlik mesajı
     */
    fun onProductSelectedForBooking(product: UnifiedProductEntity) {
        val operator = product.safeOperatorName.ifBlank { "Biblioglobus" }
        val stars = if (product.hotelCategory > 0) "${product.hotelCategory}★" else ""
        val room = product.safeRoomType.ifBlank { "Standard Room" }
        val meal = product.safeMealType.ifBlank { "Все Включено" }
        val price = "${product.price.toInt()} ${product.currency}"
        val city = product.departureCity.ifBlank { "Yekaterinburg" }

        val guidanceTextRu = buildString {
            appendLine("✅ **Отличный выбор тура!**")
            appendLine("• **Отель:** ${product.safeHotelName} $stars")
            appendLine("• **Вылет из:** $city (${product.nights} ночей)")
            appendLine("• **Номер и питание:** $room, $meal")
            appendLine("• **Туроператор:** $operator")
            appendLine("• **Базовая цена:** $price")
            appendLine()
            appendLine("✈️ **Шаг 2: Выбор авиарейсов и услуг**")
            appendLine("Сейчас вы перешли к выбору удобных перелетов и дополнительных опций:")
            appendLine("1. 🛫 **Авиарейсы**: Выберите подходящую пару рейсов (вылет и возврат). Обратите внимание на удобное время вылета и включенный багаж.")
            appendLine("2. 🛡️ **Услуги**: Проверьте включенные опции (медицинская страховка, групповой трансфер). При желании можно добавить индивидуальный VIP-трансфер или детское автокресло.")
            appendLine("3. 💰 **Итого к оплате**: Внизу страницы проверьте общую сумму с учетом сборов.")
            appendLine("4. 👥 **Переход к туристам**: Нажмите синюю кнопку **«Продолжить (Данные туристов)»** в правом нижнем углу для ввода паспортов.")
            appendLine()
            append("💬 *Я на связи! Если у вас есть вопросы по багажу, рейсам или трансферу — просто напишите мне сюда.*")
        }

        val guidanceMsg = AIAssistantMessage(
            id = "msg-${currentTimeMillis()}-guide",
            sender = "AGENT",
            textRu = guidanceTextRu,
            debugTranslationTr = "2. Aşama Rehberliği: ${product.safeHotelName} ($operator) seçildi. Uçuş çifti, bagaj, sigorta/transfer ve 'Продолжить (Данные туристов)' adımları danışman üslubuyla aktarıldı."
        )

        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + guidanceMsg,
            currentGroupedHotels = emptyList(), // Arama kartları gizlenerek rehberlik ve sohbet öne çıkarılır
            isCompactMode = true // 2. aşamada otomatik %50 genişliğe küçülür
        )
    }

    /**
     * 3. Aşama (Yolcu Bilgileri) için AI rehberlik mesajı
     */
    fun onProceedToPassengerCheckout() {
        val guidanceMsg = AIAssistantMessage(
            id = "msg-${currentTimeMillis()}-guide-step3",
            sender = "AGENT",
            textRu = """
                👥 **Шаг 3: Ввод данных туристов**
                Пожалуйста, внимательно заполните данные пассажиров:
                1. 📝 **ФИО латиницей** в точности как в загранпаспорте.
                2. 📅 **Номер паспорта, дата рождения и срок действия** (загранпаспорт должен действовать не менее 6 месяцев).
                3. 📱 **Контакты**: укажите номер телефона и e-mail для получения маршрутной квитанции и ваучера.

                После заполнения нажмите кнопку перехода к завершению и оплате бронирования.
            """.trimIndent(),
            debugTranslationTr = "3. Aşama Rehberliği: Yolcu bilgileri (Pasaport, Ad/Soyad, İletişim) giriş ekranı rehberliği."
        )
        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + guidanceMsg,
            isCompactMode = true // 3. aşamada %50 genişlik korunur
        )
    }

    private fun loadCatalogFromDatabase() {
        viewModelScope.launch {
            try {
                val fetched = supabaseClient.postgrest["marketplace_products"]
                    .select { range(0, 20000) }
                    .decodeList<UnifiedProductEntity>()
                if (fetched.isNotEmpty()) {
                    cachedCatalog = fetched
                }
            } catch (e: Exception) {
                // Hata durumunda var olan RAM'i koru
            }
        }
    }

    private fun currentTimeMillis(): Long = kotlin.time.TimeSource.Monotonic.markNow().hashCode().toLong()
}
