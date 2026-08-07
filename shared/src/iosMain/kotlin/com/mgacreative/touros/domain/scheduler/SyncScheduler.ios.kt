package com.mgacreative.touros.domain.scheduler

/**
 * iOS BGTaskScheduler tabanlı OTASyncScheduler actual implementasyonu.
 */
actual class SyncScheduler actual constructor() {
    actual fun schedulePeriodicSync(intervalMinutes: Long, tenantId: String) {
        // iOS BGAppRefreshTaskRequest scheduling
    }

    actual fun scheduleRetrySync(accountId: String, tenantId: String) {
        // iOS BGProcessingTaskRequest retry scheduling
    }

    actual fun triggerManualSync(accountId: String, tenantId: String) {
        // Immediate async background task dispatch logic
    }

    actual fun cancelAllSync() {
        // BGTaskScheduler.sharedScheduler.cancelAllTaskRequests()
    }
}
