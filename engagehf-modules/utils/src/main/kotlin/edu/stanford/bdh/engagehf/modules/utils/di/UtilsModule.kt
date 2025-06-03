package edu.stanford.bdh.engagehf.modules.utils.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import edu.stanford.bdh.engagehf.modules.utils.BuildInfo
import edu.stanford.bdh.engagehf.modules.utils.BuildInfoImpl
import edu.stanford.bdh.engagehf.modules.utils.LocaleProvider
import edu.stanford.bdh.engagehf.modules.utils.LocaleProviderImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class UtilsModule {

    @Binds
    internal abstract fun bindLocaleProvider(impl: LocaleProviderImpl): LocaleProvider

    @Binds
    internal abstract fun bindBuildInfo(impl: BuildInfoImpl): BuildInfo
}
