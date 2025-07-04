package edu.stanford.bdh.engagehf.modules.onboarding.onboarding

import com.google.common.truth.Truth.assertThat
import edu.stanford.bdh.engagehf.modules.testing.CoroutineTestRule
import edu.stanford.bdh.engagehf.modules.testing.runTestUnconfined
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test

class OnboardingViewModelTest {

    private val repository: OnboardingRepository = mockk(relaxed = true)

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private val onboardingData =
        OnboardingData(
            areas = emptyList(),
            title = "Onboarding Title",
            subTitle = "Onboarding Subtitle"
        )

    @Test
    fun `it should fetch Onboarding Data on init`(): Unit = runTestUnconfined {
        // given
        coEvery { repository.getOnboardingData() } returns Result.success(onboardingData)
        // when
        var onboardingViewModel = OnboardingViewModel(repository)
        // then
        verify { runBlocking { repository.getOnboardingData() } }
    }

    @Test
    fun `it should invoke continueButtonAction on ContinueButtonAction`() = runTestUnconfined {
        // given
        val continueButtonAction: () -> Unit = mockk(relaxed = true)
        coEvery { repository.getOnboardingData() } returns Result.success(
            onboardingData.copy(
                continueButtonAction = continueButtonAction
            )
        )
        val onboardingViewModel = OnboardingViewModel(repository)
        val action = OnboardingAction.Continue

        // when
        onboardingViewModel.onAction(action)

        // then
        verify { continueButtonAction.invoke() }
    }

    @Test
    fun `it should update uiState with data on init when getOnboardingData is successful`() =
        runTestUnconfined {
            // given setup
            coEvery { repository.getOnboardingData() } returns Result.success(onboardingData)
            // when init view model
            val onboardingViewModel = OnboardingViewModel(repository)
            // then
            val uiState = onboardingViewModel.uiState.first()
            assertThat(onboardingData.areas).isEqualTo(uiState.areas)
            assertThat(onboardingData.title).isEqualTo(uiState.title)
            assertThat(onboardingData.subTitle).isEqualTo(uiState.subtitle)
            assertThat(onboardingData.continueButtonText).isEqualTo(uiState.continueButtonText)
            assertThat(onboardingData.continueButtonAction).isEqualTo(uiState.continueAction)
        }

    @Test
    fun `it should update uiState with error on init when getOnboardingData is failed`() =
        runTestUnconfined {
            // given
            coEvery { repository.getOnboardingData() } returns Result.failure(Throwable("Error"))

            // when
            val onboardingViewModel = OnboardingViewModel(repository)

            // then
            val uiState = onboardingViewModel.uiState.first()
            assertThat(uiState.error).isNotNull()
            assertThat(uiState.error).isEqualTo("Error")
        }
}
