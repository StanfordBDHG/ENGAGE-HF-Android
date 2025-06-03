package edu.stanford.bdh.engagehf.modules.onboarding.onboarding

/**
 * Repository for fetching onboarding data.
 * @see edu.stanford.bdh.engagehf.onboarding.EngageOnboardingRepository
 */
interface OnboardingRepository {

    /**
     * Fetches the areas of the onboarding screen.
     * @return A list of [edu.stanford.bdh.engagehf.modules.onboarding.onboarding.Area] objects.
     * @see edu.stanford.bdh.engagehf.modules.onboarding.onboarding.Area
     */
    suspend fun getOnboardingData(): Result<OnboardingData>
}
