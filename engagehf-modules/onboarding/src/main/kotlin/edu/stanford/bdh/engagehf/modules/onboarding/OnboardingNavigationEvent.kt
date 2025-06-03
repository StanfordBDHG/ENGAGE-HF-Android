package edu.stanford.bdh.engagehf.modules.onboarding

import edu.stanford.bdh.engagehf.modules.navigation.NavigationEvent

sealed class OnboardingNavigationEvent : NavigationEvent {

    data object InvitationCodeScreen : OnboardingNavigationEvent()
    data class OnboardingScreen(val clearBackStack: Boolean) : OnboardingNavigationEvent()
    data object SequentialOnboardingScreen : OnboardingNavigationEvent()
    data object ConsentScreen : OnboardingNavigationEvent()
}
