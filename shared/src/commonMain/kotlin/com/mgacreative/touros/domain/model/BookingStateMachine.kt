package com.mgacreative.touros.domain.model

/**
 * 1.4.1 Booking Durum Makinesi (State Machine).
 * Bekliyor, Opsiyon, Onaylandı, İptal ve Tamamlandı durumları arasındaki geçiş kurallarını yönetir.
 */
object BookingStateMachine {

    private val allowedTransitions: Map<BookingStatus, Set<BookingStatus>> = mapOf(
        BookingStatus.BEKLIYOR to setOf(
            BookingStatus.OPSIYON,
            BookingStatus.ONAYLANDI,
            BookingStatus.IPTAL,
            BookingStatus.TAMAMLANDI
        ),
        BookingStatus.OPSIYON to setOf(
            BookingStatus.ONAYLANDI,
            BookingStatus.IPTAL,
            BookingStatus.BEKLIYOR,
            BookingStatus.TAMAMLANDI
        ),
        BookingStatus.ONAYLANDI to setOf(
            BookingStatus.TAMAMLANDI,
            BookingStatus.IPTAL,
            BookingStatus.BEKLIYOR
        ),
        BookingStatus.IPTAL to setOf(
            BookingStatus.BEKLIYOR
        ),
        BookingStatus.TAMAMLANDI to setOf(
            BookingStatus.ONAYLANDI,
            BookingStatus.BEKLIYOR
        )
    )

    /**
     * İki durum arasındaki geçişin geçerli olup olmadığını sorgular.
     */
    fun canTransition(from: BookingStatus, to: BookingStatus): Boolean {
        if (from == to) return true
        return allowedTransitions[from]?.contains(to) ?: false
    }

    /**
     * Mevcut durumdan geçilebilecek izinli durumların listesini döndürür.
     */
    fun getAllowedNextStatuses(current: BookingStatus): List<BookingStatus> {
        return allowedTransitions[current]?.toList() ?: emptyList()
    }

    /**
     * Durum değişikliğini dener; geçersiz geçişlerde Hata (Result.failure) döndürür.
     */
    fun transition(current: BookingStatus, target: BookingStatus): Result<BookingStatus> {
        return if (canTransition(current, target)) {
            Result.success(target)
        } else {
            Result.failure(
                IllegalStateException(
                    "'${current.displayName}' durumundan '${target.displayName}' durumuna geçiş yapılamaz."
                )
            )
        }
    }
}
