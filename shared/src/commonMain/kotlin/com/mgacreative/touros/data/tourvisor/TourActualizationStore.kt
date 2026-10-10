package com.mgacreative.touros.data.tourvisor

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Tourvisor tek tur sorgusunu (canlı tur detayı) yöneten depo.
 *
 * Akış:
 *  1. request_tour_actualization(tour_id) RPC'si çağrılır (önbellek 30 dk; gerekirse sunucu servisine NOTIFY gider).
 *  2. Sonuç "pending" ise tour_actualizations tablosu 1,5 sn aralıkla en fazla ~30 sn okunur.
 *  3. Hiçbir durumda rezervasyon akışı durmaz: bilgi gelmezse panel "TO'dan onay alın" gösterir.
 *
 * ViewModel değil, uygulama ömrü boyunca yaşayan tekil (single) sınıftır; ekranlar arasında aynı sonuç paylaşılır.
 * Search modülüne ait değildir.
 */
class TourActualizationStore(
    private val supabaseClient: SupabaseClient
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val states = mutableMapOf<String, MutableStateFlow<TourActualizationState>>()
    private val jobs = mutableMapOf<String, Job>()

    private val notApplicable = MutableStateFlow<TourActualizationState>(TourActualizationState.NotApplicable)

    /** Ürün için durum akışı. Tourvisor ürünü değilse her zaman NotApplicable. */
    fun stateFor(productId: String?): StateFlow<TourActualizationState> {
        val tourId = tourvisorTourIdOf(productId) ?: return notApplicable.asStateFlow()
        return flowFor(tourId).asStateFlow()
    }

    /** Sorguyu başlatır. Aynı tur için devam eden bir sorgu varsa yenisi açılmaz. */
    fun load(productId: String?, force: Boolean = false) {
        val tourId = tourvisorTourIdOf(productId) ?: return
        val flow = flowFor(tourId)
        if (!force && (flow.value is TourActualizationState.Ready || jobs[tourId]?.isActive == true)) return
        jobs[tourId]?.cancel()
        jobs[tourId] = scope.launch {
            flow.value = TourActualizationState.Loading
            flow.value = fetch(tourId)
        }
    }

    private fun flowFor(tourId: String): MutableStateFlow<TourActualizationState> =
        states.getOrPut(tourId) { MutableStateFlow(TourActualizationState.Loading) }

    private suspend fun fetch(tourId: String): TourActualizationState {
        val first = runCatching {
            val params = buildJsonObject { put("p_tour_id", tourId) }
            supabaseClient.postgrest.rpc("request_tour_actualization", params).decodeAs<TourActualizationDto>()
        }.getOrElse { err ->
            return TourActualizationState.Unavailable("network", err.message)
        }

        var current = first
        var attempts = 0
        while (current.isPending && attempts < MAX_POLLS) {
            delay(POLL_INTERVAL_MS)
            attempts++
            val next = runCatching {
                supabaseClient.postgrest["tour_actualizations"]
                    .select { filter { eq("tour_id", tourId) } }
                    .decodeSingleOrNull<TourActualizationDto>()
            }.getOrNull()
            if (next != null) current = next
        }

        return when {
            current.isOk -> TourActualizationState.Ready(current)
            current.isPending -> TourActualizationState.Unavailable("timeout")
            else -> TourActualizationState.Unavailable(current.status, current.errorMessage)
        }
    }

    private companion object {
        const val POLL_INTERVAL_MS = 1500L
        const val MAX_POLLS = 20 // ~30 sn
    }
}
