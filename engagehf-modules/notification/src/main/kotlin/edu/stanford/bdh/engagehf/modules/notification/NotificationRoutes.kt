package edu.stanford.bdh.engagehf.modules.notification

import kotlinx.serialization.Serializable

@Serializable
sealed class NotificationRoutes {
    @Serializable
    data object NotificationSetting : NotificationRoutes()
}
