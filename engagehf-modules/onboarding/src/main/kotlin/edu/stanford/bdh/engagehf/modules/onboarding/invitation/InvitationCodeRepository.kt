package edu.stanford.bdh.engagehf.modules.onboarding.invitation

interface InvitationCodeRepository {
    fun getScreenData(): InvitationCodeViewData
}
