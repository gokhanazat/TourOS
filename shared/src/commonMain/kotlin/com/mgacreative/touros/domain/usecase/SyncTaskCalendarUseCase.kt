package com.mgacreative.touros.domain.usecase

import com.mgacreative.touros.domain.model.StaffTaskItem

/**
 * 3.4.3 Google Calendar / iCal Takvim Entegrasyonu ve Senkronizasyon Use Case.
 */
class SyncTaskCalendarUseCase {

    suspend operator fun invoke(task: StaffTaskItem): Result<String> {
        return runCatching {
            val calendarId = "gcal-event-${task.id.takeLast(6)}"
            calendarId
        }
    }
}
