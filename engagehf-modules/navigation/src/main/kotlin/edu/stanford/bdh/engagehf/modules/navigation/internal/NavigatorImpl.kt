package edu.stanford.bdh.engagehf.modules.navigation.internal

import edu.stanford.bdh.engagehf.modules.navigation.NavigationEvent
import edu.stanford.bdh.engagehf.modules.navigation.Navigator
import edu.stanford.spezi.core.coroutines.Dispatching
import edu.stanford.spezi.core.logging.speziLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class NavigatorImpl @Inject constructor(
    @Dispatching.IO private val scope: CoroutineScope,
) : Navigator {
    private val logger by speziLogger()

    private val _events = MutableSharedFlow<NavigationEvent>(replay = 10)
    override val events: SharedFlow<NavigationEvent> = _events

    override fun navigateTo(event: NavigationEvent) {
        scope.launch {
            logger.i { "Navigate to: $event" }
            _events.emit(event)
        }
    }
}
