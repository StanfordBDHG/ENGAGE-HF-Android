package edu.stanford.bdh.engagehf.modules.notification

import edu.stanford.bdh.engagehf.modules.navigation.NavigationEvent

sealed class NotificationNavigationEvent : NavigationEvent {

    data object NotificationSettings : NotificationNavigationEvent()
}
