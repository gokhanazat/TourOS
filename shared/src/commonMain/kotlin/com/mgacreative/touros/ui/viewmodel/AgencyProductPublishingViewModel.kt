package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.data.database.entity.AgencyPublishedTourEntity
import com.mgacreative.touros.data.database.entity.UnifiedProductEntity
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive

sealed class AgencyProductPublishingUiState {
    data object Loading : AgencyProductPublishingUiState()
    data class Success(
        val tours: List<AgencyPublishedTourEntity>,
        val importedProducts: List<UnifiedProductEntity> = emptyList()
    ) : AgencyProductPublishingUiState()
    data class Error(val message: String) : AgencyProductPublishingUiState()
}

class AgencyProductPublishingViewModel(
    private val supabaseClient: SupabaseClient
) : ViewModel() {

    companion object {
        // Oturum boyunca tüm yüklenen verilerin bir arada saklandığı runtime hafıza
        private val persistentMemoryProducts = mutableListOf<UnifiedProductEntity>()

        fun getPersistentProducts(): List<UnifiedProductEntity> {
            return persistentMemoryProducts.toList()
        }
    }

    private val _uiState = MutableStateFlow<AgencyProductPublishingUiState>(AgencyProductPublishingUiState.Loading)
    val uiState: StateFlow<AgencyProductPublishingUiState> = _uiState.asStateFlow()

    private val jsonParser = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    init {
        loadCatalog()
    }

    fun loadCatalog() {
        viewModelScope.launch {
            _uiState.value = AgencyProductPublishingUiState.Loading
            
            var catalogList = emptyList<AgencyPublishedTourEntity>()
            var dbProducts = emptyList<UnifiedProductEntity>()

            // 1. Supabase marketplace_products tablosunu çek
            runCatching {
                supabaseClient.postgrest["marketplace_products"]
                    .select()
                    .decodeList<UnifiedProductEntity>()
            }.onSuccess { list ->
                dbProducts = list
            }.onFailure { err ->
                println("⚠️ Supabase marketplace_products çekme hatası: ${err.message}")
            }

            // 2. Veritabanından gelenler ve hafızadaki ürünleri birleştir
            val mergedProducts = (dbProducts + persistentMemoryProducts).distinctBy { it.id }

            if (mergedProducts.isNotEmpty()) {
                catalogList = mergedProducts.map { p ->
                    AgencyPublishedTourEntity(
                        id = p.id,
                        tourId = p.id,
                        tourTitle = "${p.hotelName.ifBlank { p.tourName }} (${p.region.ifBlank { p.departureCity }})",
                        tourCode = "OP-${p.operatorId}-${p.id.take(6)}",
                        operatorName = p.operatorName.ifBlank { "Operatör" },
                        basePrice = p.price,
                        calculatedPrice = p.price * 1.125,
                        isPublished = true
                    )
                }
            }

            _uiState.value = AgencyProductPublishingUiState.Success(
                tours = catalogList,
                importedProducts = mergedProducts
            )
        }
    }

    fun togglePublishStatus(tourId: String, newPublishedState: Boolean, customPrice: Double? = null) {
        viewModelScope.launch {
            val currentState = (uiState.value as? AgencyProductPublishingUiState.Success) ?: return@launch
            val updatedTours = currentState.tours.map { item ->
                if (item.tourId == tourId) {
                    item.copy(isPublished = newPublishedState, customPriceOverride = customPrice)
                } else item
            }
            _uiState.value = currentState.copy(tours = updatedTours)
        }
    }

    /**
     * Kataloğu ve Veritabanındaki (Supabase SQL) Tüm Ürünleri Tamamen Temizler / Sıfırlar
     */
    fun clearCatalog(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            persistentMemoryProducts.clear()

            // Supabase 'marketplace_products' SQL tablosundaki TÜM kayıtları sil
            runCatching {
                supabaseClient.postgrest["marketplace_products"].delete {
                    filter { neq("id", "DONT_MATCH_ANYTHING_NON_EXISTENT") }
                }
            }.onSuccess {
                println("✅ Supabase marketplace_products tablosundaki tüm kayıtlar silindi.")
            }.onFailure { err ->
                println("⚠️ Supabase silme uyarısı: ${err.message}")
            }

            _uiState.value = AgencyProductPublishingUiState.Success(
                tours = emptyList(),
                importedProducts = emptyList()
            )
            onComplete()
        }
    }

    /**
     * Ham JSON / TXT Verisini Özyinelemeli (Recursive) Olarak Parse Edip Yükler.
     * replaceExisting = false (varsayılan) ise eski yüklemeleri SILMEZ, üstüne birleştirir (append).
     */
    fun importRawJsonPayload(
        rawContent: String,
        replaceExisting: Boolean = false,
        onSuccess: (count: Int) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            runCatching {
                val parsedEntities = parseContentToEntities(rawContent.trim())
                if (parsedEntities.isEmpty()) {
                    throw IllegalArgumentException("Geçerli otel/tur/uçuş verisi ayrıştırılamadı.")
                }

                if (replaceExisting) {
                    persistentMemoryProducts.clear()
                    persistentMemoryProducts.addAll(parsedEntities)

                    runCatching {
                        supabaseClient.postgrest["marketplace_products"].delete {
                            filter { neq("id", "") }
                        }
                    }
                } else {
                    // Çoklu Dosya Yükleme (Birleştir & Üst Üste Ekle)
                    persistentMemoryProducts.removeAll { old -> parsedEntities.any { it.id == old.id } }
                    persistentMemoryProducts.addAll(0, parsedEntities)
                }

                // Supabase veritabanına 500'erli paketler halinde kalıcı kaydet
                val chunks = parsedEntities.chunked(500)
                for (chunk in chunks) {
                    val insertResult = runCatching {
                        supabaseClient.postgrest["marketplace_products"].insert(chunk)
                    }
                    if (insertResult.isFailure) {
                        println("⚠️ Supabase Ekleme Uyarısı: ${insertResult.exceptionOrNull()?.message}")
                    }
                }

                val activeProducts = persistentMemoryProducts.distinctBy { it.id }

                val newTours = activeProducts.map { p ->
                    AgencyPublishedTourEntity(
                        id = p.id,
                        tourId = p.id,
                        tourTitle = "${p.hotelName.ifBlank { p.tourName }} (${p.region.ifBlank { p.departureCity }})",
                        tourCode = "IMP-${p.id.take(6)}",
                        operatorName = p.operatorName.ifBlank { "Operatör Verisi" },
                        basePrice = p.price,
                        calculatedPrice = p.price * 1.125,
                        isPublished = true
                    )
                }

                _uiState.value = AgencyProductPublishingUiState.Success(
                    tours = newTours,
                    importedProducts = activeProducts
                )

                parsedEntities.size
            }.onSuccess { count ->
                onSuccess(count)
            }.onFailure { err ->
                onError(err.message ?: "Veri içeri aktarılırken hata oluştu.")
            }
        }
    }

    private fun extractStringValue(element: JsonElement?): String {
        if (element == null) return ""
        return when (element) {
            is JsonPrimitive -> element.content
            is JsonObject -> {
                element["fullName"]?.jsonPrimitive?.content
                    ?: element["name"]?.jsonPrimitive?.content
                    ?: element["russianName"]?.jsonPrimitive?.content
                    ?: ""
            }
            else -> ""
        }
    }

    private fun parseContentToEntities(content: String): List<UnifiedProductEntity> {
        val result = mutableListOf<UnifiedProductEntity>()
        try {
            val jsonElement = jsonParser.parseToJsonElement(content)

            fun processElement(elem: JsonElement) {
                if (elem is JsonArray) {
                    elem.forEach { item -> processElement(item) }
                } else if (elem is JsonObject) {
                    if (elem.containsKey("flights")) {
                        result.addAll(parseFlightsJsonObject(elem))
                    } else if (elem.containsKey("hotels") && elem["hotels"] is JsonArray) {
                        (elem["hotels"] as JsonArray).forEach { h ->
                            if (h is JsonObject) result.addAll(parseHotelJsonObject(h))
                        }
                    } else if (elem.containsKey("tours") && (elem.containsKey("category") || elem.containsKey("name"))) {
                        result.addAll(parseHotelJsonObject(elem))
                    } else if (elem.containsKey("hotel") && elem["hotel"] is JsonObject) {
                        result.add(parseSingleTourJsonObject(elem))
                    } else {
                        val parsed = parseHotelJsonObject(elem)
                        if (parsed.isNotEmpty()) {
                            result.addAll(parsed)
                        } else {
                            result.add(parseSingleTourJsonObject(elem))
                        }
                    }
                }
            }

            processElement(jsonElement)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result.distinctBy { it.id }
    }

    private fun formatDateForPostgres(rawDate: String): String? {
        if (rawDate.isBlank()) return null
        val parts = rawDate.split(".")
        return if (parts.size == 3 && parts[0].length == 2 && parts[2].length == 4) {
            "${parts[2]}-${parts[1]}-${parts[0]}"
        } else {
            rawDate
        }
    }

    private fun parseSingleTourJsonObject(obj: JsonObject): UnifiedProductEntity {
        val id = obj["id"]?.jsonPrimitive?.content ?: "tour-${(100000..999999).random()}"
        val name = extractStringValue(obj["name"])
        val price = obj["price"]?.jsonPrimitive?.doubleOrNull ?: 0.0
        val fuelCharge = obj["fuelCharge"]?.jsonPrimitive?.doubleOrNull ?: 0.0
        val currency = extractStringValue(obj["currency"]).ifBlank { "RUB" }
        val nights = obj["nights"]?.jsonPrimitive?.intOrNull ?: 7
        val adults = obj["adults"]?.jsonPrimitive?.intOrNull ?: 2
        val childs = obj["childs"]?.jsonPrimitive?.intOrNull ?: 0
        val roomType = extractStringValue(obj["roomType"])
        val picture = extractStringValue(obj["picture"])
        val operatorLink = extractStringValue(obj["operatorLink"])

        val opElem = obj["operator"] ?: obj["operatorName"]
        val opName = extractStringValue(opElem).ifBlank { "Anex Tour" }
        val opId = if (opElem is JsonObject) opElem["id"]?.jsonPrimitive?.intOrNull ?: 0 else 0

        val hotelObj = obj["hotel"] as? JsonObject
        val hotelId = hotelObj?.get("id")?.jsonPrimitive?.intOrNull ?: 0
        val hotelName = extractStringValue(hotelObj?.get("name")).ifBlank { name }
        val hotelCat = hotelObj?.get("category")?.jsonPrimitive?.intOrNull ?: 5

        val countryName = extractStringValue(hotelObj?.get("country"))
        val regionName = extractStringValue(hotelObj?.get("region"))
        val subRegionName = extractStringValue(hotelObj?.get("subRegion"))

        val departureCity = extractStringValue(obj["departure"])
        val mealType = extractStringValue(obj["meal"])

        val commonObj = hotelObj?.get("common") as? JsonObject
        val lat = commonObj?.get("latitude")?.jsonPrimitive?.doubleOrNull
        val lng = commonObj?.get("longitude")?.jsonPrimitive?.doubleOrNull
        val rawDate = extractStringValue(obj["date"])

        return UnifiedProductEntity(
            id = id,
            productType = "PACKAGE_TOUR",
            tourName = name,
            operatorId = opId,
            operatorName = opName,
            operatorLink = operatorLink,
            price = price,
            fuelCharge = fuelCharge,
            currency = currency,
            hotelId = hotelId,
            hotelName = hotelName,
            hotelCategory = hotelCat,
            country = countryName,
            region = regionName,
            subRegion = subRegionName,
            roomType = roomType,
            mealType = mealType,
            departureCity = departureCity,
            departureDate = formatDateForPostgres(rawDate),
            nights = nights,
            adults = adults,
            childs = childs,
            isCharter = obj["isCharter"]?.jsonPrimitive?.booleanOrNull ?: true,
            isPromo = obj["isPromo"]?.jsonPrimitive?.booleanOrNull ?: false,
            pictureUrl = picture,
            latitude = lat,
            longitude = lng
        )
    }

    private fun parseHotelJsonObject(hotelObj: JsonObject): List<UnifiedProductEntity> {
        val list = mutableListOf<UnifiedProductEntity>()
        val hotelId = hotelObj["id"]?.jsonPrimitive?.intOrNull ?: 0
        val hotelName = extractStringValue(hotelObj["name"]).ifBlank { extractStringValue(hotelObj["hotelName"]) }
        if (hotelName.isBlank() && hotelId == 0) return emptyList()

        val hotelCat = hotelObj["category"]?.jsonPrimitive?.intOrNull ?: 5
        val pictureLink = extractStringValue(hotelObj["picturelink"]).ifBlank { extractStringValue(hotelObj["picture"]) }
        val lat = hotelObj["latitude"]?.jsonPrimitive?.doubleOrNull
        val lng = hotelObj["longitude"]?.jsonPrimitive?.doubleOrNull

        val countryName = extractStringValue(hotelObj["country"])
        val regionName = extractStringValue(hotelObj["region"])
        val subRegionName = extractStringValue(hotelObj["subRegion"])

        val toursArray = (hotelObj["tours"] ?: hotelObj["packages"] ?: hotelObj["offers"] ?: hotelObj["prices"]) as? JsonArray
        if (toursArray != null && toursArray.isNotEmpty()) {
            toursArray.forEachIndexed { idx, tourElement ->
                if (tourElement is JsonObject) {
                    val tourId = tourElement["id"]?.jsonPrimitive?.content ?: "tour-${hotelId}-${idx + 1}"
                    val tourName = extractStringValue(tourElement["name"])
                    val price = tourElement["price"]?.jsonPrimitive?.doubleOrNull ?: (hotelObj["price"]?.jsonPrimitive?.doubleOrNull ?: 0.0)
                    val fuelCharge = tourElement["fuelCharge"]?.jsonPrimitive?.doubleOrNull ?: 0.0
                    val currency = extractStringValue(tourElement["currency"]).ifBlank { extractStringValue(hotelObj["currency"]) }.ifBlank { "RUB" }
                    val nights = tourElement["nights"]?.jsonPrimitive?.intOrNull ?: 7

                    val opElem = tourElement["operator"] ?: hotelObj["operator"] ?: tourElement["operatorName"]
                    val opName = extractStringValue(opElem).ifBlank { "Direct Contract" }
                    val opId = if (opElem is JsonObject) opElem["id"]?.jsonPrimitive?.intOrNull ?: 0 else 0

                    val mealType = extractStringValue(tourElement["meal"])
                    val depCity = extractStringValue(tourElement["departure"])
                    val roomType = extractStringValue(tourElement["roomType"]).ifBlank { extractStringValue(tourElement["room"]) }
                    val rawDate = extractStringValue(tourElement["date"])

                    list.add(
                        UnifiedProductEntity(
                            id = tourId,
                            productType = "PACKAGE_TOUR",
                            tourName = tourName,
                            operatorId = opId,
                            operatorName = opName,
                            price = price,
                            fuelCharge = fuelCharge,
                            currency = currency,
                            hotelId = hotelId,
                            hotelName = hotelName,
                            hotelCategory = hotelCat,
                            country = countryName,
                            region = regionName,
                            subRegion = subRegionName,
                            roomType = roomType,
                            mealType = mealType,
                            departureCity = depCity,
                            departureDate = formatDateForPostgres(rawDate),
                            nights = nights,
                            pictureUrl = pictureLink,
                            latitude = lat,
                            longitude = lng
                        )
                    )
                }
            }
        } else {
            // Otel bazlı tek kayıt
            list.add(
                UnifiedProductEntity(
                    id = "hotel-$hotelId",
                    productType = "HOTEL",
                    operatorName = "Direct Contract",
                    price = hotelObj["price"]?.jsonPrimitive?.doubleOrNull ?: 0.0,
                    currency = extractStringValue(hotelObj["currency"]).ifBlank { "RUB" },
                    hotelId = hotelId,
                    hotelName = hotelName,
                    hotelCategory = hotelCat,
                    country = countryName,
                    region = regionName,
                    subRegion = subRegionName,
                    pictureUrl = pictureLink,
                    latitude = lat,
                    longitude = lng
                )
            )
        }
        return list
    }

    private fun parseFlightsJsonObject(flightsObj: JsonObject): List<UnifiedProductEntity> {
        val list = mutableListOf<UnifiedProductEntity>()
        val flightsArray = flightsObj["flights"] as? JsonArray ?: return emptyList()

        flightsArray.forEachIndexed { index, elem ->
            if (elem is JsonObject) {
                val forwardArray = elem["forward"] as? JsonArray
                val firstForward = forwardArray?.firstOrNull() as? JsonObject
                val flightNo = firstForward?.get("number")?.jsonPrimitive?.content ?: "FL-$index"
                
                val companyObj = firstForward?.get("company") as? JsonObject
                val airline = extractStringValue(companyObj).ifBlank { "Airline" }

                val priceObj = elem["price"] as? JsonObject
                val price = priceObj?.get("value")?.jsonPrimitive?.doubleOrNull ?: 0.0
                val currency = priceObj?.get("currency")?.jsonPrimitive?.content ?: "RUB"

                val depPortObj = (firstForward?.get("departure") as? JsonObject)?.get("port") as? JsonObject
                val depCity = depPortObj?.get("shortName")?.jsonPrimitive?.content ?: ""

                val arrPortObj = (firstForward?.get("arrival") as? JsonObject)?.get("port") as? JsonObject
                val arrCity = arrPortObj?.get("shortName")?.jsonPrimitive?.content ?: ""

                list.add(
                    UnifiedProductEntity(
                        id = "flight-$index-${flightNo}",
                        productType = "FLIGHT",
                        operatorName = airline,
                        price = price,
                        currency = currency,
                        hotelName = "Uçuş: $depCity ➔ $arrCity ($flightNo)",
                        departureCity = depCity,
                        region = arrCity,
                        airlineName = airline,
                        flightNumber = flightNo,
                        baggageKg = firstForward?.get("baggage")?.jsonPrimitive?.intOrNull ?: 20
                    )
                )
            }
        }
        return list
    }
}
