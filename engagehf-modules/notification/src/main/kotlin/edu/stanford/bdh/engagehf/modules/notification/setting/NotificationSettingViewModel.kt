package edu.stanford.bdh.engagehf.modules.notification.setting

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import edu.stanford.bdh.engagehf.modules.navigation.NavigationEvent
import edu.stanford.bdh.engagehf.modules.navigation.Navigator
import edu.stanford.bdh.engagehf.modules.notification.NotificationPermissions
import edu.stanford.bdh.engagehf.modules.notification.R
import edu.stanford.bdh.engagehf.modules.notification.fcm.DeviceRegistrationService
import edu.stanford.bdh.engagehf.modules.utils.MessageNotifier
import edu.stanford.spezi.core.logging.speziLogger
import edu.stanford.spezi.ui.PendingActions
import edu.stanford.spezi.ui.StringResource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for notification settings.
 */
@Suppress("LongParameterList")
@HiltViewModel
internal class NotificationSettingViewModel @Inject constructor(
    private val repository: NotificationSettingsRepository,
    private val navigator: Navigator,
    private val notificationSettingsMapper: NotificationSettingsStateMapper,
    private val messageNotifier: MessageNotifier,
    private val notificationPermissions: NotificationPermissions,
    @ApplicationContext private val context: Context,
    private val deviceRegistrationService: DeviceRegistrationService,
) : ViewModel() {
    private val logger by speziLogger()
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)

    val uiState = _uiState.asStateFlow()

    init {
        loadNotificationSettings()
    }

    private fun loadNotificationSettings() {
        viewModelScope.launch {
            val missingPermissions = notificationPermissions.getRequiredPermissions()
            if (missingPermissions.isNotEmpty()) {
                _uiState.update { UiState.MissingPermissions(missingPermissions) }
            } else {
                repository.observeNotificationSettings().collect { result ->
                    result.onFailure {
                        _uiState.update { UiState.Error(StringResource(R.string.notification_error_message)) }
                    }.onSuccess { successResult ->
                        _uiState.update {
                            UiState.NotificationSettingsLoaded(
                                notificationSettings = successResult,
                            )
                        }
                    }
                }
            }
        }
    }

    fun onAction(action: Action) {
        when (action) {
            Action.Back -> {
                navigator.navigateTo(NavigationEvent.PopBackStack)
            }

            is Action.SwitchChanged -> {
                val currentState = _uiState.value
                if (currentState is UiState.NotificationSettingsLoaded) {
                    val currentSettings = currentState.notificationSettings
                    val newSettings =
                        notificationSettingsMapper.mapSwitchChanged(action, currentSettings)
                    _uiState.update {
                        UiState.NotificationSettingsLoaded(
                            newSettings,
                            currentState.pendingActions.plus(action)
                        )
                    }
                    viewModelScope.launch {
                        repository.saveNotificationSettings(newSettings).onFailure {
                            messageNotifier.notify("Failed to save notification settings")
                        }
                        _uiState.update {
                            UiState.NotificationSettingsLoaded(
                                notificationSettings = newSettings,
                                pendingActions = currentState.pendingActions.minus(action)
                            )
                        }
                    }
                }
            }

            is Action.PermissionResult -> {
                handlePermissionResult(permission = action.permission, granted = action.granted)
            }

            Action.AppSettings -> {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                launch(intent = intent)
            }
        }
    }

    private fun launch(intent: Intent) {
        runCatching {
            context.startActivity(intent.apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
        }.onFailure {
            logger.e(it) { "Failed to launch intent ${intent.action}" }
        }
    }

    private fun handlePermissionResult(permission: String, granted: Boolean) {
        _uiState.update { currentState ->
            if (currentState is UiState.MissingPermissions) {
                val missingPermissions = currentState.missingPermissions.filterNot { granted && it == permission }
                if (missingPermissions.isEmpty()) {
                    deviceRegistrationService.refreshDeviceToken()
                    loadNotificationSettings()
                    currentState
                } else {
                    UiState.MissingPermissions(missingPermissions.toSet())
                }
            } else {
                currentState
            }
        }
    }

    sealed interface Action {
        data object Back : Action

        data class SwitchChanged(
            val notificationType: NotificationType,
            val isChecked: Boolean,
        ) : Action

        data object AppSettings : Action
        data class PermissionResult(val permission: String, val granted: Boolean) : Action
    }

    sealed interface UiState {
        data object Loading : UiState
        data class Error(val message: StringResource) : UiState
        data class MissingPermissions(val missingPermissions: Set<String>) : UiState

        data class NotificationSettingsLoaded(
            val notificationSettings: NotificationSettings,
            val pendingActions: PendingActions<Action.SwitchChanged> = PendingActions(),
        ) : UiState
    }
}
