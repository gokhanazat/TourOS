package com.mgacreative.touros.domain.scheduler

actual class SyncScheduler actual constructor() {
    actual fun schedulePeriodicSync(intervalMinutes: Long, tenantId: String) {}
    actual fun scheduleRetrySync(accountId: String, tenantId: String) {}
    actual fun triggerManualSync(accountId: String, tenantId: String) {}
    actual fun cancelAllSync() {}
}
