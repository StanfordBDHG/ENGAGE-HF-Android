package edu.stanford.bdh.engagehf.di

import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import edu.stanford.bdh.engagehf.modules.notification.NotificationPermissions
import edu.stanford.bdh.engagehf.modules.notification.di.NotificationModule

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [NotificationModule.NotificationPermissionsBinding::class]
)
class AppTestModule {

    /**
     * Since our instrumented tests run for android versions 31 and 34, we can't use grant rule
     * to grant notification permission by default as it would fail for version 31. Hence, we simply
     * replace the dependency to return empty permissions
     */
    @Provides
    fun provideNotificationPermissions(): NotificationPermissions {
        return object : NotificationPermissions {
            override fun getRequiredPermissions(): Set<String> = emptySet()
        }
    }
}
