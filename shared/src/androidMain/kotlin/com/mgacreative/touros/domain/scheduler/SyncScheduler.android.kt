package com.mgacreative.touros.domain.scheduler

/**
 * Android WorkManager tabanlı OTASyncScheduler actual implementasyonu.
 */
actual class SyncScheduler actual constructor() {
    actual fun schedulePeriodicSync(intervalMinutes: Long, tenantId: String) {
        // Android WorkManager PeriodicWorkRequest scheduling
    }

    actual fun scheduleRetrySync(accountId: String, tenantId: String) {
        // Android WorkManager OneTimeWorkRequest retry scheduling
    }

    actual fun triggerManualSync(accountId: String, tenantId: String) {
        // Immediate WorkManager enqueue logic
    }

    actual fun cancelAllSync() {
        // WorkManager cancelAllWorkByTag logic
    }
}
