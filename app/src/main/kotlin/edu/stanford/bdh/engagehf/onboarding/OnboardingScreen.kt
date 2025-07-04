package edu.stanford.bdh.engagehf.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import edu.stanford.bdh.engagehf.R
import edu.stanford.bdh.engagehf.modules.onboarding.onboarding.OnboardingView
import edu.stanford.spezi.ui.CommonScaffold

@Composable
fun OnboardingScreen() {
    CommonScaffold(title = stringResource(R.string.onboarding)) {
        OnboardingView()
    }
}
