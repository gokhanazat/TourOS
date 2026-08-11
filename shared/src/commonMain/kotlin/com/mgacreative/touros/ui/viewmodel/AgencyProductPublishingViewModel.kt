package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.data.database.entity.AgencyPublishedTourEntity
import com.mgacreative.touros.data.database.entity.UnifiedProductEntity
import com.mgacreative.touros.domain.model.User
import com.mgacreative.touros.domain.model.UserRole
import com.mgacreative.touros.domain.repository.AuthRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive

import com.mgacreative.touros.utils.LocalCatalogStorage

sealed class AgencyProductPublishingUiState {
    data object Loading : AgencyProductPublishingUiState()
    data class Success(
        val tours: List<AgencyPublishedTourEntity>,
        val importedProducts: List<UnifiedProductEntity> = emptyList()
    ) : AgencyProductPublishingUiState()
    data class Error(val message: String) : AgencyProductPublishingUiState()
}

class AgencyProductPublishingViewModel(
    private val supabaseClient: SupabaseClient,
    private val authRepository: AuthRepository? = null
) : ViewModel() {

    companion object {
        private val defaultJson = Json { ignoreUnknownKeys = true; isLenient = true; encodeDefaults = true }

        // Oturum ve güncellemeler boyunca tüm yüklenen verilerin saklandığı hafıza
        private val persistentMemoryProducts = mutableListOf<UnifiedProductEntity>()

        fun getPersistentProducts(): List<UnifiedProductEntity> {
            val diskProducts = runCatching {
                val rawJson = LocalCatalogStorage.loadCatalogJson()
                if (!rawJson.isNullOrBlank()) {
                    defaultJson.decodeFromString(
                        kotlinx.serialization.builtins.ListSerializer(UnifiedProductEntity.serializer()),
                        rawJson
                    )
                } else emptyList()
            }.getOrElse { emptyList() }

            val mergedMap = LinkedHashMap<String, UnifiedProductEntity>()
            diskProducts.filter { it.id.isNotBlank() }.forEach { mergedMap[it.id] = it }
            persistentMemoryProducts.filter { it.id.isNotBlank() }.forEach { mergedMap[it.id] = it }
            return mergedMap.values.toList()
        }
    }

    val currentUserState: StateFlow<User?> = authRepository?.observeAuthState() ?: MutableStateFlow(null).asStateFlow()

    private val _uiState = MutableStateFlow<AgencyProductPublishingUiState>(AgencyProductPublishingUiState.Loading)
    val uiState: StateFlow<AgencyProductPublishingUiState> = _uiState.asStateFlow()

    private val jsonParser = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    init {
        loadCatalog()
    }

    private fun saveToDiskCache(products: List<UnifiedProductEntity>) {
        runCatching {
            if (products.isEmpty()) return
            val jsonString = jsonParser.encodeToString(
                kotlinx.serialization.builtins.ListSerializer(UnifiedProductEntity.serializer()),
                products
            )
            LocalCatalogStorage.saveCatalogJson(jsonString)
        }.onFailure { err ->
            println("⚠️ Yerel katalog disk kaydedici uyarısı: ${err.message}")
        }
    }

    private fun loadFromDiskCache(): List<UnifiedProductEntity> {
        return runCatching {
            val rawJson = LocalCatalogStorage.loadCatalogJson()
            if (!rawJson.isNullOrBlank()) {
                jsonParser.decodeFromString(
                    kotlinx.serialization.builtins.ListSerializer(UnifiedProductEntity.serializer()),
                    rawJson
                )
            } else emptyList()
        }.getOrElse { err ->
            println("⚠️ Yerel katalog disk okuma uyarısı: ${err.message}")
            emptyList()
        }
    }

    private fun mapToPublishedTour(p: UnifiedProductEntity): AgencyPublishedTourEntity {
        return AgencyPublishedTourEntity(
            id = p.id,
            tourId = p.id,
            tourTitle = "${p.safeHotelName.ifBlank { p.safeTourName }} (${p.safeRegion.ifBlank { p.safeDepartureCity }})",
            tourCode = "OP-${p.safeOperatorId}-${p.id.take(6)}",
            operatorName = p.safeOperatorName.ifBlank { "Operatör" },
            basePrice = p.safePrice,
            calculatedPrice = p.customPriceOverride ?: (p.safePrice * 1.125),
            isPublished = p.isPublished,
            customPriceOverride = p.customPriceOverride
        )
    }

    fun loadCatalog() {
        viewModelScope.launch {
            _uiState.value = AgencyProductPublishingUiState.Loading
            
            // 1. Önce yerel disk önbelleğini yükle (Uygulama açılışı ve güncellemelerde anında veri koruma)
            val diskProducts = loadFromDiskCache()
            if (diskProducts.isNotEmpty()) {
                val mergedMap = LinkedHashMap<String, UnifiedProductEntity>()
                persistentMemoryProducts.filter { it.id.isNotBlank() }.forEach { mergedMap[it.id] = it }
                diskProducts.filter { it.id.isNotBlank() }.forEach { mergedMap[it.id] = it }

                persistentMemoryProducts.clear()
                persistentMemoryProducts.addAll(mergedMap.values)

                val catalogList = persistentMemoryProducts.map { p -> mapToPublishedTour(p) }
                _uiState.value = AgencyProductPublishingUiState.Success(
                    tours = catalogList,
                    importedProducts = persistentMemoryProducts.toList()
                )
            }

            var dbProducts = emptyList<UnifiedProductEntity>()

            // 2. Supabase marketplace_products tablosunu çek (20.000 limitle tüm yüklemeleri çek)
            runCatching {
                supabaseClient.postgrest["marketplace_products"]
                    .select {
                        range(0, 20000)
                    }
                    .decodeList<UnifiedProductEntity>()
            }.onSuccess { list ->
                dbProducts = list
            }.onFailure { err ->
                println("⚠️ Supabase marketplace_products çekme hatası: ${err.message}")
            }

            // 3. Veritabanı verileri, yerel disk verileri ve RAM verilerini harmanla
            val mergedMap = LinkedHashMap<String, UnifiedProductEntity>()
            persistentMemoryProducts.filter { it.id.isNotBlank() }.forEach { mergedMap[it.id] = it }
            dbProducts.filter { it.id.isNotBlank() }.forEach { dbItem ->
                val existing = mergedMap[dbItem.id]
                mergedMap[dbItem.id] = if (existing != null) {
                    dbItem.copy(
                        isPublished = existing.isPublished,
                        customPriceOverride = existing.customPriceOverride ?: dbItem.customPriceOverride
                    )
                } else dbItem
            }

            val mergedProducts = mergedMap.values.toList()
            if (mergedProducts.isNotEmpty()) {
                persistentMemoryProducts.clear()
                persistentMemoryProducts.addAll(mergedProducts)
                saveToDiskCache(mergedProducts)
            }

            val finalProducts = if (mergedProducts.isNotEmpty()) mergedProducts else persistentMemoryProducts.toList()
            val finalTours = finalProducts.map { p -> mapToPublishedTour(p) }

            _uiState.value = AgencyProductPublishingUiState.Success(
                tours = finalTours,
                importedProducts = finalProducts
            )
        }
    }

    fun togglePublishStatus(tourId: String, newPublishedState: Boolean, customPrice: Double? = null) {
        viewModelScope.launch {
            val currentState = (uiState.value as? AgencyProductPublishingUiState.Success) ?: return@launch
            
            val updatedProducts = currentState.importedProducts.map { item ->
                if (item.id == tourId) {
                    item.copy(isPublished = newPublishedState, customPriceOverride = customPrice)
                } else item
            }
            persistentMemoryProducts.clear()
            persistentMemoryProducts.addAll(updatedProducts)
            saveToDiskCache(updatedProducts)

            val updatedTours = currentState.tours.map { item ->
                if (item.tourId == tourId) {
                    item.copy(isPublished = newPublishedState, customPriceOverride = customPrice)
                } else item
            }

            _uiState.value = currentState.copy(
                tours = updatedTours,
                importedProducts = persistentMemoryProducts.toList()
            )

            // Supabase veritabanındaki duruma güncelleme gönder (UPSERT)
            val targetProduct = persistentMemoryProducts.find { it.id == tourId && it.id.isNotBlank() }
            if (targetProduct != null) {
                runCatching {
                    supabaseClient.postgrest["marketplace_products"].upsert(targetProduct)
                }.onFailure { err ->
                    println("⚠️ Supabase yayınlama durumu güncelleme uyarısı: ${err.message}")
                }
            }
        }
    }

    /**
     * Kataloğu, Yerel Önbelleği ve Veritabanındaki (Supabase SQL) Tüm Ürünleri Temizler / Sıfırlar
     */
    fun clearCatalog(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            persistentMemoryProducts.clear()
            LocalCatalogStorage.clearCatalogJson()

            // Supabase 'marketplace_products' SQL tablosundaki TÜM kayıtları sil
            runCatching {
                supabaseClient.postgrest["marketplace_products"].delete {
                    filter { neq("product_type", "NON_EXISTENT") }
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
     * Tüm yüklenen verileri birleştirip (append/upsert) yerel disk önbelleğine ve Supabase'e kaydeder.
     */
    fun importRawJsonPayload(
        rawContent: String,
        onSuccess: (count: Int) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // 1. JSON parsing işlemini hızlıca tamamla (Dispatchers.Default)
                val parsedEntities = withContext(Dispatchers.Default) {
                    parseContentToEntities(rawContent.trim()).filter { it.id.isNotBlank() }
                }

                if (parsedEntities.isEmpty()) {
                    onError("Geçerli otel/tur/uçuş verisi ayrıştırılamadı.")
                    return@launch
                }

                // 2. RAM, Yerel Disk Önbelleği ve UI'ı ANINDA Güncelle (Bekleme olmaksızın)
                persistentMemoryProducts.removeAll { old -> parsedEntities.any { it.id == old.id } }
                persistentMemoryProducts.addAll(0, parsedEntities)

                val activeProducts = persistentMemoryProducts.filter { it.id.isNotBlank() }.distinctBy { it.id }

                // Verileri ANINDA cihaz diski / local storage'a yaz (Güncellemede silinmeyi %100 engeller)
                saveToDiskCache(activeProducts)

                val newTours = activeProducts.map { p -> mapToPublishedTour(p) }

                _uiState.value = AgencyProductPublishingUiState.Success(
                    tours = newTours,
                    importedProducts = activeProducts
                )

                // UI Modalını hemen kapatıp kullanıcıya başarı bilgisini ver
                onSuccess(parsedEntities.size)

                // 3. Supabase DB Upsert İşlemini ARKA PLANDA (Asenkron) Çalıştır
                viewModelScope.launch(Dispatchers.IO) {
                    val chunks = parsedEntities.chunked(250)
                    var savedDbCount = 0
                    for (chunk in chunks) {
                        val validChunk = chunk.filter { it.id.isNotBlank() }
                        if (validChunk.isNotEmpty()) {
                            val upsertResult = runCatching {
                                supabaseClient.postgrest["marketplace_products"].upsert(validChunk)
                            }
                            if (upsertResult.isSuccess) {
                                savedDbCount += validChunk.size
                            } else {
                                println("⚠️ Supabase Paket Upsert Uyarısı: ${upsertResult.exceptionOrNull()?.message}, Teker teker aktarılıyor...")
                                for (singleItem in validChunk) {
                                    runCatching {
                                        supabaseClient.postgrest["marketplace_products"].upsert(singleItem)
                                    }.onSuccess { savedDbCount++ }
                                }
                            }
                        }
                    }
                    println("✅ Supabase DB Arka Plan Kalıcı Aktarımı Tamamlandı: $savedDbCount / ${parsedEntities.size} ürün veritabanına işlendi.")
                }
            } catch (err: Throwable) {
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

    private fun sanitizeLatitude(lat: Double?): Double? {
        if (lat == null || lat.isNaN() || lat.isInfinite()) return null
        return if (lat in -90.0..90.0) lat else null
    }

    private fun sanitizeLongitude(lng: Double?): Double? {
        if (lng == null || lng.isNaN() || lng.isInfinite()) return null
        return if (lng in -180.0..180.0) lng else null
    }

    private fun sanitizeJsonContent(rawContent: String): String {
        return rawContent
            .replace("\uFEFF", "")
            .replace("\u200B", "")
            .trim()
    }

    private fun parseContentToEntities(content: String): List<UnifiedProductEntity> {
        val result = mutableListOf<UnifiedProductEntity>()
        try {
            val sanitized = sanitizeJsonContent(content)
            val jsonElement = jsonParser.parseToJsonElement(sanitized)

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
        val name = extractStringValue(obj["name"])
        val price = (obj["price"]?.jsonPrimitive?.doubleOrNull ?: 0.0).coerceAtLeast(0.0)
        val fuelCharge = (obj["fuelCharge"]?.jsonPrimitive?.doubleOrNull ?: 0.0).coerceAtLeast(0.0)
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
        val lat = sanitizeLatitude(commonObj?.get("latitude")?.jsonPrimitive?.doubleOrNull)
        val lng = sanitizeLongitude(commonObj?.get("longitude")?.jsonPrimitive?.doubleOrNull)
        val rawDate = extractStringValue(obj["date"])

        val rawId = obj["id"]?.jsonPrimitive?.content
        val id = if (!rawId.isNullOrBlank()) rawId else {
            val key = "${opId}_${name}_${hotelName}_${countryName}_${regionName}_${departureCity}_${rawDate}_${price}_${roomType}_${mealType}"
            "tour-${key.hashCode().toUInt().toString(16)}"
        }

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
        val lat = sanitizeLatitude(hotelObj["latitude"]?.jsonPrimitive?.doubleOrNull)
        val lng = sanitizeLongitude(hotelObj["longitude"]?.jsonPrimitive?.doubleOrNull)

        val countryName = extractStringValue(hotelObj["country"])
        val regionName = extractStringValue(hotelObj["region"])
        val subRegionName = extractStringValue(hotelObj["subRegion"])

        val hotelEntityId = if (hotelId > 0) "hotel-$hotelId" else "hotel-${hotelName.hashCode().toUInt().toString(16)}"

        // 1. Ana Otel Kaydı (HOTEL)
        list.add(
            UnifiedProductEntity(
                id = hotelEntityId,
                productType = "HOTEL",
                operatorName = extractStringValue(hotelObj["operator"]).ifBlank { "Direct Contract" },
                price = (hotelObj["price"]?.jsonPrimitive?.doubleOrNull ?: 0.0).coerceAtLeast(0.0),
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

        // 2. Otelin Paket Turları (PACKAGE_TOUR)
        val toursArray = (hotelObj["tours"] ?: hotelObj["packages"] ?: hotelObj["offers"] ?: hotelObj["prices"]) as? JsonArray
        if (toursArray != null && toursArray.isNotEmpty()) {
            toursArray.forEach { tourElement ->
                if (tourElement is JsonObject) {
                    val rawTourId = tourElement["id"]?.jsonPrimitive?.content
                    val tourName = extractStringValue(tourElement["name"])

                    val price = (tourElement["price"]?.jsonPrimitive?.doubleOrNull ?: (hotelObj["price"]?.jsonPrimitive?.doubleOrNull ?: 0.0)).coerceAtLeast(0.0)
                    val fuelCharge = (tourElement["fuelCharge"]?.jsonPrimitive?.doubleOrNull ?: 0.0).coerceAtLeast(0.0)
                    val currency = extractStringValue(tourElement["currency"]).ifBlank { extractStringValue(hotelObj["currency"]) }.ifBlank { "RUB" }
                    val nights = tourElement["nights"]?.jsonPrimitive?.intOrNull ?: 7

                    val opElem = tourElement["operator"] ?: hotelObj["operator"] ?: tourElement["operatorName"]
                    val opName = extractStringValue(opElem).ifBlank { "Direct Contract" }
                    val opId = if (opElem is JsonObject) opElem["id"]?.jsonPrimitive?.intOrNull ?: 0 else 0

                    val mealType = extractStringValue(tourElement["meal"])
                    val depCity = extractStringValue(tourElement["departure"])
                    val roomType = extractStringValue(tourElement["roomType"]).ifBlank { extractStringValue(tourElement["room"]) }
                    val rawDate = extractStringValue(tourElement["date"])

                    val tourId = if (!rawTourId.isNullOrBlank()) rawTourId else {
                        val key = "${hotelId}_${tourName}_${roomType}_${mealType}_${depCity}_${rawDate}_${price}"
                        "tour-${hotelId}-${key.hashCode().toUInt().toString(16)}"
                    }

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
        }
        return list.filter { it.id.isNotBlank() }
    }

    private fun parseFlightsJsonObject(flightsObj: JsonObject): List<UnifiedProductEntity> {
        val list = mutableListOf<UnifiedProductEntity>()
        val flightsArray = flightsObj["flights"] as? JsonArray ?: return emptyList()

        flightsArray.forEach { elem ->
            if (elem is JsonObject) {
                val forwardArray = elem["forward"] as? JsonArray
                val firstForward = forwardArray?.firstOrNull() as? JsonObject
                val flightNo = firstForward?.get("number")?.jsonPrimitive?.content ?: "FL-00"
                
                val companyObj = firstForward?.get("company") as? JsonObject
                val airline = extractStringValue(companyObj).ifBlank { "Airline" }

                val priceObj = elem["price"] as? JsonObject
                val price = (priceObj?.get("value")?.jsonPrimitive?.doubleOrNull ?: 0.0).coerceAtLeast(0.0)
                val currency = priceObj?.get("currency")?.jsonPrimitive?.content ?: "RUB"

                val depPortObj = (firstForward?.get("departure") as? JsonObject)?.get("port") as? JsonObject
                val depCity = depPortObj?.get("shortName")?.jsonPrimitive?.content ?: ""

                val arrPortObj = (firstForward?.get("arrival") as? JsonObject)?.get("port") as? JsonObject
                val arrCity = arrPortObj?.get("shortName")?.jsonPrimitive?.content ?: ""

                val rawFlightId = elem["id"]?.jsonPrimitive?.content
                val flightId = if (!rawFlightId.isNullOrBlank()) rawFlightId else {
                    val key = "${flightNo}_${airline}_${depCity}_${arrCity}_${price}"
                    "flight-${key.hashCode().toUInt().toString(16)}"
                }

                list.add(
                    UnifiedProductEntity(
                        id = flightId,
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
        return list.filter { it.id.isNotBlank() }
    }
}

