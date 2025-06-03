package edu.stanford.bdh.engagehf.modules.onboarding.invitation

import edu.stanford.spezi.ui.StringResource

data class InvitationCodeViewData(
    val description: StringResource,
    val redeemAction: () -> Unit,
)
