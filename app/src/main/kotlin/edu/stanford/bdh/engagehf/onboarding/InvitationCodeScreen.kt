package edu.stanford.bdh.engagehf.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import edu.stanford.bdh.engagehf.modules.onboarding.R
import edu.stanford.bdh.engagehf.modules.onboarding.invitation.InvitationCodeView
import edu.stanford.spezi.ui.CommonScaffold

@Composable
fun InvitationCodeScreen() {
    CommonScaffold(title = stringResource(R.string.onboarding_invitation_code)) {
        InvitationCodeView()
    }
}
