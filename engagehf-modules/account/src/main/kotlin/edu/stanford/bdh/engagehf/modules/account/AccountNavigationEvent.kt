package edu.stanford.bdh.engagehf.modules.account

import edu.stanford.bdh.engagehf.modules.navigation.NavigationEvent

sealed class AccountNavigationEvent : NavigationEvent {
    data class RegisterScreen(
        val email: String = "",
        val password: String = "",
    ) : AccountNavigationEvent()

    data object LoginScreen : AccountNavigationEvent()
}
