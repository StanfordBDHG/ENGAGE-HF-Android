package edu.stanford.bdh.engagehf.modules.notification.fcm

import android.content.ComponentName
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.messaging.FirebaseMessaging
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.components.SingletonComponent
import edu.stanford.bdh.engagehf.modules.notification.notifier.Notifications
import edu.stanford.bdh.engagehf.modules.testing.SpeziTestScope
import edu.stanford.bdh.engagehf.modules.testing.mockTask
import edu.stanford.bdh.engagehf.modules.utils.BuildInfo
import edu.stanford.spezi.storage.credential.InMemoryKeyValueStorage
import edu.stanford.spezi.storage.credential.putSerializable
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.Locale
import java.util.TimeZone

@HiltAndroidTest
class DeviceRegistrationServiceTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    private val context by lazy { InstrumentationRegistry.getInstrumentation().targetContext }
    private val packageInfo by lazy {
        context.packageManager.getPackageInfo(context.packageName, 0)
    }
    private val functions: FirebaseFunctions = mockk(relaxed = true)
    private val firebaseMessaging: FirebaseMessaging = mockk(relaxed = true)
    private val buildInfo: BuildInfo = mockk(relaxed = true)
    private val storage = InMemoryKeyValueStorage()
    private val storageKey = "fcm-notification-token-body"
    private val registerFunction = "registerDevice"
    private val unregisterFunction = "unregisterDevice"

    private lateinit var service: DeviceRegistrationService

    @Before
    fun setup() {
        hiltRule.inject()
        every { buildInfo.getOsVersion() } returns "tiramisu"

        service = DeviceRegistrationServiceImpl(
            context = context,
            functions = functions,
            firebaseMessaging = firebaseMessaging,
            coroutineScope = SpeziTestScope(),
            storage = storage,
            buildInfo = buildInfo,
        )
    }

    @Test
    fun `it should handle registerDevice correctly`() {
        // given
        val token = "test_token"
        val expectedBody = notificationTokenBody(token)
        val map = slot<Map<String, String>>()
        coEvery {
            functions.getHttpsCallable(registerFunction).call(capture(map))
        } returns mockTask(mockk())

        // when
        service.registerDevice(token)

        // then
        coVerify { functions.getHttpsCallable(registerFunction).call(capture(map)) }
        val body = map.captured
        assertThat(body["notificationToken"]).isEqualTo(token)
        assertThat(storage.getValue(storageKey)).isEqualTo(expectedBody)
    }

    @Test
    fun `it should ignore registerDevice if same token is stored`() {
        // given
        val storedBody = notificationTokenBody("stored_token")
        storage.putSerializable(storageKey, storedBody)

        // when
        service.registerDevice(storedBody.notificationToken)

        // then
        coVerify(exactly = 0) { functions.getHttpsCallable(registerFunction).call(any()) }
    }

    @Test
    fun `it should trigger registerDevice if same token is stored but app version changed`() {
        // given
        val token = "stored_token"
        val storedBody = notificationTokenBody(token)
        storage.putSerializable(storageKey, storedBody)
        packageInfo.versionName = "new-version-name"
        val map = slot<Map<String, String>>()
        coEvery {
            functions.getHttpsCallable(registerFunction).call(capture(map))
        } returns mockTask(mockk())
        val newBody = notificationTokenBody(token)

        // when
        service.registerDevice(token)

        // then
        verify(exactly = 1) { functions.getHttpsCallable(registerFunction).call(any()) }
        assertThat(storage.getValue(storageKey)).isEqualTo(newBody)
    }

    @Test
    fun `it should handle unregisterDevice correctly when token is stored`() = runTest {
        // given
        val token = "test_token"
        storage.putSerializable(storageKey, notificationTokenBody(token))
        val map = slot<Map<String, String>>()
        coEvery {
            functions.getHttpsCallable(unregisterFunction).call(capture(map))
        } returns mockTask(mockk())

        // when
        service.unregisterDevice()

        // then
        val body = map.captured
        assertThat(body["notificationToken"]).isEqualTo(token)
        assertThat(body["platform"]).isEqualTo("Android")
        assertThat(storage.getValue(storageKey)).isNull()
    }

    @Test
    fun `unregisterDevice should not call unregisterDevice if no token is stored`() = runTest {
        // given
        storage.delete(storageKey)

        // when
        service.unregisterDevice()

        // then
        coVerify(exactly = 0) { functions.getHttpsCallable(unregisterFunction).call(any()) }
    }

    @Test
    fun `refreshDeviceToken should fetch and register new token`() {
        // given
        val newToken = "new_test_token"
        coEvery { firebaseMessaging.token } returns mockTask(newToken)
        val map = slot<Map<String, String>>()
        coEvery { functions.getHttpsCallable(registerFunction).call(capture(map)) } returns mockTask(mockk())

        // when
        service.refreshDeviceToken()

        // then
        val body = map.captured
        assertThat(body["notificationToken"]).isEqualTo(newToken)
        assertThat(storage.getValue(storageKey)).isEqualTo(notificationTokenBody(newToken))
    }

    private fun notificationTokenBody(
        token: String,
    ) = DeviceRegistrationServiceImpl.NotificationTokenBody(
        notificationToken = token,
        osVersion = buildInfo.getOsVersion(),
        appVersion = packageInfo.versionName ?: "unknown",
        appBuild = packageInfo.longVersionCode.toString(),
        language = Locale.getDefault().toLanguageTag(),
        timeZone = TimeZone.getDefault().id
    )

    @Module
    @InstallIn(SingletonComponent::class)
    class TestModule {

        @Notifications.TargetActivity
        @Provides
        fun provideTargetActivityComponentName(): ComponentName = ComponentName("package.name", "class.name")
    }
}
