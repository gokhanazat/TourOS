package com.mgacreative.touros.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgacreative.touros.data.database.entity.RoomTypeEntity
import com.mgacreative.touros.data.util.isValidUuid
import com.mgacreative.touros.domain.model.Hotel
import com.mgacreative.touros.domain.repository.HotelRepository
import com.mgacreative.touros.domain.usecase.GetCurrentUserUseCase
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import com.mgacreative.touros.utils.DateUtils

val STANDARD_ROOM_TYPES = listOf(
    "Single Room",
    "Double Room",
    "Twin Room",
    "Triple Room",
    "Quad Room",
    "Standard Room",
    "Superior Room",
    "Deluxe Room",
    "Studio Room",
    "Executive Room",
    "Junior Suite",
    "Suite",
    "Family Room",
    "Connecting Rooms",
    "Presidential Suite",
    "Swim-up Room"
)

data class PeriodRoomItem(
    val id: String = "",
    val roomTypeName: String = "Double Room",
    val allotment: Int = 0,
    val costPrice: Double = 0.0,
    val salePrice: Double = 0.0
)

data class HotelPeriodItem(
    val id: String = "",
    val periodName: String = "1. Periyot (Tüm Yıl)",
    val startDate: String = "${DateUtils.getCurrentYear()}-01-01",
    val endDate: String = "${DateUtils.getCurrentYear()}-12-31",
    val rooms: List<PeriodRoomItem> = listOf(
        PeriodRoomItem(id = "rm-1", roomTypeName = "Double Room", allotment = 0, costPrice = 0.0, salePrice = 0.0)
    )
)

data class HotelFormUiState(
    val hotelId: String? = null,
    val name: String = "",
    val city: String = "",
    val country: String = "Türkiye",
    val starRating: Int = 4,
    val boardType: String = "HB",
    val description: String = "",
    val coverImageUrl: String = "",
    val address: String = "",
    val phone: String = "",
    val email: String = "",
    val website: String = "",
    val periods: List<HotelPeriodItem> = listOf(HotelPeriodItem(id = "p-1")),
    val isLoading: Boolean = false,
    val isSavedSuccess: Boolean = false,
    val errorMessage: String? = null
)

class HotelFormViewModel(
    private val hotelRepository: HotelRepository,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val supabaseClient: SupabaseClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(HotelFormUiState())
    val uiState: StateFlow<HotelFormUiState> = _uiState.asStateFlow()

    fun updateName(name: String) { _uiState.value = _uiState.value.copy(name = name) }
    fun updateCity(city: String) { _uiState.value = _uiState.value.copy(city = city) }
    fun updateCountry(country: String) { _uiState.value = _uiState.value.copy(country = country) }
    fun updateStarRating(rating: Int) { _uiState.value = _uiState.value.copy(starRating = rating) }
    fun updateBoardType(boardType: String) { _uiState.value = _uiState.value.copy(boardType = boardType) }
    fun updateDescription(desc: String) { _uiState.value = _uiState.value.copy(description = desc) }
    fun updateCoverImageUrl(url: String) { _uiState.value = _uiState.value.copy(coverImageUrl = url) }
    fun updateAddress(addr: String) { _uiState.value = _uiState.value.copy(address = addr) }
    fun updatePhone(phone: String) { _uiState.value = _uiState.value.copy(phone = phone) }
    fun updateEmail(email: String) { _uiState.value = _uiState.value.copy(email = email) }
    fun updateWebsite(web: String) { _uiState.value = _uiState.value.copy(website = web) }

    // ── PERİYOT YÖNETİMİ ───────────────────────────────────────────────
    fun addPeriod() {
        val currentPeriods = _uiState.value.periods.toMutableList()
        val newIdx = currentPeriods.size + 1
        val currentYear = DateUtils.getCurrentYear()
        val newPeriod = HotelPeriodItem(
            id = "p-${(100000..999999).random()}",
            periodName = "$newIdx. Periyot",
            startDate = "$currentYear-01-01",
            endDate = "$currentYear-12-31",
            rooms = listOf(PeriodRoomItem(id = "rm-${(100000..999999).random()}", roomTypeName = "Double Room", allotment = 0, costPrice = 0.0, salePrice = 0.0))
        )
        currentPeriods.add(newPeriod)
        _uiState.value = _uiState.value.copy(periods = currentPeriods)
    }

    fun removePeriod(periodId: String) {
        val currentPeriods = _uiState.value.periods.filterNot { it.id == periodId }
        _uiState.value = _uiState.value.copy(periods = currentPeriods)
    }

    fun updatePeriodHeader(periodId: String, name: String, startDate: String, endDate: String) {
        val updated = _uiState.value.periods.map { p ->
            if (p.id == periodId) p.copy(periodName = name, startDate = startDate, endDate = endDate) else p
        }
        _uiState.value = _uiState.value.copy(periods = updated)
    }

    // ── PERİYOT ALTI ODA TİPİ YÖNETİMİ ─────────────────────────────────
    fun addRoomToPeriod(periodId: String) {
        val updated = _uiState.value.periods.map { p ->
            if (p.id == periodId) {
                val newRooms = p.rooms.toMutableList()
                newRooms.add(PeriodRoomItem(id = "rm-${(100000..999999).random()}", roomTypeName = "Double Room", allotment = 0, costPrice = 0.0, salePrice = 0.0))
                p.copy(rooms = newRooms)
            } else p
        }
        _uiState.value = _uiState.value.copy(periods = updated)
    }

    fun removeRoomFromPeriod(periodId: String, roomId: String) {
        val updated = _uiState.value.periods.map { p ->
            if (p.id == periodId) {
                p.copy(rooms = p.rooms.filterNot { it.id == roomId })
            } else p
        }
        _uiState.value = _uiState.value.copy(periods = updated)
    }

    fun updatePeriodRoom(periodId: String, roomId: String, roomTypeName: String, allotment: Int, costPrice: Double, salePrice: Double) {
        val updated = _uiState.value.periods.map { p ->
            if (p.id == periodId) {
                val updatedRooms = p.rooms.map { r ->
                    if (r.id == roomId) r.copy(roomTypeName = roomTypeName, allotment = allotment, costPrice = costPrice, salePrice = salePrice) else r
                }
                p.copy(rooms = updatedRooms)
            } else p
        }
        _uiState.value = _uiState.value.copy(periods = updated)
    }

    fun loadHotelForEdit(hotelId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            hotelRepository.getHotelById(hotelId).onSuccess { hotel ->
                val seasonRates = hotelRepository.getSeasonRatesForHotel(hotelId).getOrDefault(emptyList())

                val loadedPeriods = if (seasonRates.isNotEmpty()) {
                    seasonRates.groupBy { it.seasonName.ifBlank { "1. Periyot" } }.entries.mapIndexed { idx, entry ->
                        HotelPeriodItem(
                            id = "p-${idx + 1}",
                            periodName = entry.key,
                            startDate = entry.value.firstOrNull()?.startDate ?: "2026-01-01",
                            endDate = entry.value.firstOrNull()?.endDate ?: "2026-12-31",
                            rooms = entry.value.mapIndexed { rIdx, rate ->
                                PeriodRoomItem(
                                    id = rate.id.ifBlank { "rm-${rIdx + 1}" },
                                    roomTypeName = rate.roomTypeName?.ifBlank { "Double Room" } ?: "Double Room",
                                    allotment = rate.allotment,
                                    costPrice = if (rate.costPrice > 0) rate.costPrice else rate.singlePrice,
                                    salePrice = if (rate.salePrice > 0) rate.salePrice else rate.doublePrice
                                )
                            }
                        )
                    }
                } else {
                    listOf(HotelPeriodItem(id = "p-1"))
                }

                _uiState.value = HotelFormUiState(
                    hotelId = hotel.id,
                    name = hotel.name,
                    city = hotel.city ?: "",
                    country = hotel.country,
                    starRating = hotel.starRating ?: 4,
                    description = hotel.description ?: "",
                    coverImageUrl = hotel.coverImageUrl ?: "",
                    address = hotel.address ?: "",
                    phone = hotel.phone ?: "",
                    email = hotel.email ?: "",
                    website = hotel.website ?: "",
                    periods = loadedPeriods,
                    isLoading = false
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Otel bilgisi getirilemedi")
            }
        }
    }

    fun saveHotel() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val state = _uiState.value
            val user = getCurrentUserUseCase()
            val tenantId = user?.tenantId?.takeIf { it.isValidUuid() } ?: "00000000-0000-0000-0000-000000000001"

            val hotelToSave = Hotel(
                id = state.hotelId ?: "",
                name = state.name,
                city = state.city,
                country = state.country,
                starRating = state.starRating,
                description = state.description,
                coverImageUrl = state.coverImageUrl,
                address = state.address,
                phone = state.phone,
                email = state.email,
                website = state.website,
                tenantId = tenantId
            )

            val result = if (state.hotelId.isNullOrBlank()) {
                hotelRepository.createHotel(hotelToSave)
            } else {
                hotelRepository.updateHotel(hotelToSave)
            }

            result.onSuccess { savedHotel ->
                val hotelId = savedHotel.id
                val validTenant = if (savedHotel.tenantId.isValidUuid()) savedHotel.tenantId else tenantId

                var saveFailed = false
                var errorDetails: String? = null

                // 1. Önce bu otele ait eski sezon fiyatlarını temizle (Çakışma veya silinmiş oda hatalarını önler)
                hotelRepository.deleteSeasonRatesForHotel(hotelId)

                // 2. Formdaki güncel Periyot ve Oda Tiplerini kaydet
                state.periods.forEach { period ->
                    period.rooms.forEach { room ->
                        runCatching {
                            val rateToSave = com.mgacreative.touros.domain.model.HotelSeasonRate(
                                id = "", // Her zaman yeni temiz kayıt olarak oluştur
                                hotelId = hotelId,
                                roomTypeName = room.roomTypeName,
                                seasonName = period.periodName,
                                startDate = period.startDate,
                                endDate = period.endDate,
                                singlePrice = room.costPrice,
                                doublePrice = room.salePrice,
                                costPrice = room.costPrice,
                                salePrice = room.salePrice,
                                allotment = room.allotment,
                                tenantId = validTenant
                            )
                            val rateRes = hotelRepository.createSeasonRate(rateToSave)
                            rateRes.onFailure { err ->
                                saveFailed = true
                                errorDetails = err.message
                            }
                        }.onFailure { err ->
                            saveFailed = true
                            errorDetails = err.message
                        }
                    }
                }

                // 3. Oda tiplerini room_types tablosuna da ekle/güncelle
                state.periods.flatMap { it.rooms }.map { it.roomTypeName }.distinct().forEach { rName ->
                    val rSalePrice = state.periods.flatMap { it.rooms }.firstOrNull { it.roomTypeName == rName }?.salePrice ?: 0.0
                    runCatching {
                        hotelRepository.createRoomType(
                            com.mgacreative.touros.domain.model.RoomType(
                                hotelId = hotelId,
                                name = rName,
                                basePricePerNight = rSalePrice,
                                tenantId = validTenant
                            )
                        )
                    }
                }

                if (saveFailed) {
                    _uiState.value = state.copy(
                        hotelId = hotelId,
                        isLoading = false,
                        errorMessage = "Otel kaydedildi ancak periyot fiyatları eklenirken hata oluştu: ${errorDetails ?: "Bilinmeyen hata"}"
                    )
                } else {
                    _uiState.value = state.copy(
                        hotelId = hotelId,
                        isLoading = false,
                        isSavedSuccess = true,
                        errorMessage = null
                    )
                }
            }.onFailure { err ->
                _uiState.value = state.copy(isLoading = false, errorMessage = err.message ?: "Kayıt işlemi başarısız")
            }
        }
    }
}
