package edu.stanford.bdh.engagehf.modules.onboarding.simulator

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onChild
import androidx.compose.ui.test.performClick
import edu.stanford.bdh.engagehf.modules.onboarding.onboarding.OnboardingScreenTestIdentifier
import edu.stanford.spezi.testing.ui.onNodeWithIdentifier

class OnboardingScreenSimulator(
    private val composeTestRule: ComposeTestRule,
) {
    private val root = composeTestRule.onNodeWithIdentifier(OnboardingScreenTestIdentifier.ROOT)
    private val title = composeTestRule.onNodeWithIdentifier(OnboardingScreenTestIdentifier.TITLE)
    private val subtitle =
        composeTestRule.onNodeWithIdentifier(OnboardingScreenTestIdentifier.SUBTITLE)
    private val areasList =
        composeTestRule.onNodeWithIdentifier(OnboardingScreenTestIdentifier.AREAS_LIST)
    private val button = composeTestRule.onNodeWithIdentifier(
        identifier = OnboardingScreenTestIdentifier.LEARN_MORE_BUTTON,
        useUnmergedTree = true,
    )

    fun assertDisplayed() {
        root.assertIsDisplayed()
    }

    fun assertTitle(text: String) {
        title
            .assertIsDisplayed()
            .assertTextEquals(text)
    }

    fun assertSubtitle(text: String) {
        subtitle
            .assertIsDisplayed()
            .assertTextEquals(text)
    }

    fun assertContinueButtonTitle(text: String) {
        button
            .onChild()
            .assert(hasText(text))
    }

    fun onAreasList(scope: AreasSimulator.() -> Unit) {
        AreasSimulator().apply(scope)
    }

    fun clickContinueButton() {
        button
            .assertHasClickAction()
            .performClick()
    }

    inner class AreasSimulator {
        fun assertDisplayed() {
            areasList.assertIsDisplayed()
        }

        fun assertAreaTitle(title: String) {
            composeTestRule
                .onNodeWithIdentifier(OnboardingScreenTestIdentifier.AREA_TITLE, title)
                .assertIsDisplayed()
        }
    }
}
