package edu.stanford.bdh.engagehf.modules.navigation

import kotlinx.coroutines.flow.SharedFlow

interface Navigator {
    val events: SharedFlow<NavigationEvent>

    fun navigateTo(event: NavigationEvent)
}
