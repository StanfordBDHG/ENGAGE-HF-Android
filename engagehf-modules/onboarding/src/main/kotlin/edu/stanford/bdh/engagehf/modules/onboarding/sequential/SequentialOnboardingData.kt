package edu.stanford.bdh.engagehf.modules.onboarding.sequential

data class SequentialOnboardingData(
    val steps: List<Step>,
    val actionText: String,
    val onAction: () -> Unit,
)
