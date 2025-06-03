package edu.stanford.bdh.engagehf.modules.account.manager

interface InvitationAuthManager {
    suspend fun checkInvitationCode(invitationCode: String): Result<Unit>
}
