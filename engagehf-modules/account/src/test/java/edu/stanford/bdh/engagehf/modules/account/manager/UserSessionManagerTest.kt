package edu.stanford.bdh.engagehf.modules.account.manager

import com.google.common.truth.Truth.assertThat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import edu.stanford.bdh.engagehf.modules.account.AccountEvents
import edu.stanford.bdh.engagehf.modules.testing.SpeziTestScope
import edu.stanford.bdh.engagehf.modules.testing.mockTask
import edu.stanford.bdh.engagehf.modules.testing.runTestUnconfined
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Before
import org.junit.Test
import java.util.UUID

class UserSessionManagerTest {
    private val firebaseStorage: FirebaseStorage = mockk()
    private val firebaseAuth: FirebaseAuth = mockk()
    private val firestore: FirebaseFirestore = mockk()
    private val accountEvents: AccountEvents = mockk(relaxed = true)

    private lateinit var userSessionManager: UserSessionManager

    @Before
    fun setup() {
        every { firebaseAuth.addAuthStateListener(any()) } just Runs
        every { firebaseAuth.removeAuthStateListener(any()) } just Runs
    }

    @Test
    fun `it should have not initialized user if currentUser is null`() = runTestUnconfined {
        // given
        every { firebaseAuth.currentUser } returns null
        createUserSessionManager()

        // when
        val state = userSessionManager.getUserState()

        // then
        assertThat(state).isEqualTo(UserState.NotInitialized)
    }

    @Test
    fun `it should reflect registered user without uid state correctly`() = runTestUnconfined {
        // given
        val firebaseUser: FirebaseUser = mockk {
            every { isAnonymous } returns false
        }
        every { firebaseAuth.currentUser } returns firebaseUser
        every { firebaseAuth.uid } returns null
        createUserSessionManager()
        val expectedUserState =
            UserState.Registered(hasInvitationCodeConfirmed = false, disabled = false, phoneNumbers = emptyList())

        // when
        val state = userSessionManager.getUserState()

        // then
        assertThat(state).isEqualTo(expectedUserState)
    }

    @Test
    fun `it should not upload consent pdf if current user is not available`() = runTestUnconfined {
        // given
        every { firebaseAuth.currentUser } returns null
        createUserSessionManager()

        // when
        val result = userSessionManager.uploadConsentPdf(byteArrayOf())

        // then
        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun `it should emit SignOutSuccess when signOut is successful`() = runTestUnconfined {
        // given
        every { firebaseAuth.signOut() } just Runs
        createUserSessionManager()

        // when
        userSessionManager.signOut()

        // then
        verify { accountEvents.emit(AccountEvents.Event.SignOutSuccess) }
    }

    @Test
    fun `it should emit SignOutFailure when signOut is unsuccessful`() = runTestUnconfined {
        // given
        every { firebaseAuth.signOut() } throws Exception()
        createUserSessionManager()

        // when
        userSessionManager.signOut()

        // then
        verify { accountEvents.emit(AccountEvents.Event.SignOutFailure) }
    }

    @Test
    fun `it should handle successful upload of consent pdf correctly`() = runTestUnconfined {
        // given
        setupPDFUpload(successful = true)
        createUserSessionManager()

        // when
        val result = userSessionManager.uploadConsentPdf(byteArrayOf())

        // then
        assertThat(result.isSuccess).isTrue()
    }

    @Test
    fun `it should handle non successful upload consent pdf correctly`() = runTestUnconfined {
        // given
        setupPDFUpload(successful = false)
        createUserSessionManager()

        // when
        val result = userSessionManager.uploadConsentPdf(byteArrayOf())

        // then
        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun `it should emit NotInitialized when currentUser is null`() = runTestUnconfined {
        // given
        every { firebaseAuth.currentUser } returns null

        // when
        createUserSessionManager()

        // then
        assertObservedState(userState = UserState.NotInitialized, scope = this)
    }

    @Test
    fun `it should emit NotInitialized when currentUser is anonymous`() = runTestUnconfined {
        // given
        val firebaseUser: FirebaseUser = mockk {
            every { isAnonymous } returns true
        }
        every { firebaseAuth.currentUser } returns firebaseUser

        // when
        createUserSessionManager()

        // then
        assertObservedState(userState = UserState.NotInitialized, scope = this)
    }

    @Test
    fun `it should emit correct Registered when uid is null`() =
        runTestUnconfined {
            // given
            val firebaseUser: FirebaseUser = mockk {
                every { isAnonymous } returns false
            }
            every { firebaseAuth.currentUser } returns firebaseUser
            every { firebaseAuth.uid } returns null

            // when
            createUserSessionManager()

            // then
            assertObservedState(
                userState = UserState.Registered(
                    hasInvitationCodeConfirmed = false,
                    disabled = false,
                    phoneNumbers = emptyList()
                ),
                scope = this
            )
        }

    @Test
    fun `it should emit correct Registered user when invitation code confirmed and not disabled`() =
        runTestUnconfined {
            // given
            val invitationCodeConfirmed = true
            val disabled = false
            setupUserResponse(
                invitationCodeConfirmed = invitationCodeConfirmed,
                disabled = disabled
            )

            // when
            createUserSessionManager()

            // then
            assertObservedState(
                userState = UserState.Registered(
                    hasInvitationCodeConfirmed = invitationCodeConfirmed,
                    disabled = disabled,
                    phoneNumbers = emptyList(),
                ),
                scope = this
            )
        }

    @Test
    fun `it should emit correct Registered user when invitation code not confirmed and disabled`() =
        runTestUnconfined {
            // given
            val invitationCodeConfirmed = false
            val disabled = true
            setupUserResponse(
                invitationCodeConfirmed = invitationCodeConfirmed,
                disabled = disabled
            )

            // when
            createUserSessionManager()

            // then
            assertObservedState(
                userState = UserState.Registered(
                    hasInvitationCodeConfirmed = invitationCodeConfirmed,
                    disabled = disabled,
                    phoneNumbers = emptyList(),
                ),
                scope = this
            )
        }

    @Test
    fun `it should observe registered user correctly`() = runTestUnconfined {
        // given
        val uid = "some-uid"
        val firebaseUser: FirebaseUser = mockk {
            every { isAnonymous } returns false
        }
        every { firebaseAuth.currentUser } returns firebaseUser
        every { firebaseAuth.uid } returns uid
        val invitationCode = "1234"
        val disabled = false
        val phoneNumbers = listOf("1", "2", "3")
        val snapshot: DocumentSnapshot = mockk()
        val listenerSlot = slot<EventListener<DocumentSnapshot>>()
        val registrationListener: ListenerRegistration = mockk(relaxed = true)
        every {
            firestore.collection("users")
                .document(uid)
                .addSnapshotListener(capture(listenerSlot))
        } returns registrationListener
        every { snapshot.getString("invitationCode") } returns invitationCode
        every { snapshot.getBoolean("disabled") } returns disabled
        every { snapshot.get("phoneNumbers") } returns phoneNumbers
        var response: UserState.Registered? = null
        createUserSessionManager()

        val job = launch {
            userSessionManager.observeRegisteredUser().collect {
                response = it
            }
        }

        // when
        listenerSlot.captured.onEvent(snapshot, null)

        // then
        assertThat(response).isEqualTo(
            UserState.Registered(
                hasInvitationCodeConfirmed = true,
                disabled = disabled,
                phoneNumbers = phoneNumbers,
            )
        )

        job.cancel()
    }

    @Test
    fun `it should return the correct user uid`() {
        // given
        val uid = UUID.randomUUID().toString()
        every { firebaseAuth.uid } returns uid
        createUserSessionManager()

        // when
        val result = userSessionManager.getUserUid()

        // then
        assertThat(result).isEqualTo(uid)
    }

    @Test
    fun `it should return the correct user info`() {
        // given
        val eMail = "test@test.de"
        val name = "Test User"
        val firebaseUser: FirebaseUser = mockk {
            every { email } returns eMail
            every { displayName } returns name
        }
        every { firebaseAuth.currentUser } returns firebaseUser
        createUserSessionManager()

        // when
        val result = userSessionManager.getUserInfo()

        // then
        assertThat(result.email).isEqualTo(eMail)
        assertThat(result.name).isEqualTo(name)
    }

    private suspend fun assertObservedState(userState: UserState, scope: TestScope) {
        val slot = slot<FirebaseAuth.AuthStateListener>()
        var capturedState: UserState? = null
        every { firebaseAuth.addAuthStateListener(capture(slot)) } just Runs
        val job = scope.launch {
            capturedState = userSessionManager.observeUserState().first()
        }

        // when
        slot.captured.onAuthStateChanged(firebaseAuth)

        // then
        assertThat(capturedState).isEqualTo(userState)
        job.join()
    }

    @Suppress("UnusedPrivateMember")
    private fun setupConsented() {
        val uid = "uid"
        val location = "users/$uid/consent/consent.pdf"
        val firebaseUser: FirebaseUser = mockk {
            every { isAnonymous } returns false
        }
        val storageReference: StorageReference = mockk()
        every { storageReference.metadata } returns mockTask(mockk())
        every { firebaseAuth.currentUser } returns firebaseUser
        every { firebaseAuth.uid } returns uid
        every { firebaseStorage.getReference(location) } returns storageReference
    }

    private fun setupUserResponse(
        invitationCodeConfirmed: Boolean,
        disabled: Boolean,
        phoneNumbers: List<String> = emptyList(),
    ) {
        val uid = "uid"
        val document: DocumentSnapshot = mockk {
            every { getString("invitationCode") } returns "1234".takeIf { invitationCodeConfirmed }
            every { getBoolean("disabled") } returns disabled
            every { this@mockk.get("phoneNumbers") } returns phoneNumbers
        }
        val firebaseUser: FirebaseUser = mockk {
            every { isAnonymous } returns false
        }
        every { firebaseAuth.currentUser } returns firebaseUser
        every { firebaseAuth.uid } returns uid
        every { firestore.collection("users").document(uid).get() } returns mockTask(document)
    }

    private fun setupPDFUpload(successful: Boolean) {
        val uid = "uid"
        val location = "users/$uid/consent/consent.pdf"
        val firebaseUser: FirebaseUser = mockk()
        every { firebaseUser.uid } returns uid
        every { firebaseAuth.currentUser } returns firebaseUser

        val taskResult: UploadTask.TaskSnapshot = mockk()
        val storageTask: StorageTask<UploadTask.TaskSnapshot> = mockk()
        every { storageTask.isSuccessful } returns successful
        val uploadTask: UploadTask = mockk {
            every { isComplete } returns true
            every { isCanceled } returns false
            every { exception } returns null
            every { result } returns taskResult
            every { result.task } returns storageTask
        }
        val storageReference: StorageReference = mockk()
        every { storageReference.putStream(any()) } returns uploadTask
        every { firebaseStorage.getReference(location) } returns storageReference
    }

    private fun createUserSessionManager() {
        userSessionManager = UserSessionManagerImpl(
            firebaseStorage = firebaseStorage,
            firebaseAuth = firebaseAuth,
            ioDispatcher = UnconfinedTestDispatcher(),
            coroutineScope = SpeziTestScope(),
            firestore = firestore,
            accountEvents = accountEvents,
        )
    }
}
