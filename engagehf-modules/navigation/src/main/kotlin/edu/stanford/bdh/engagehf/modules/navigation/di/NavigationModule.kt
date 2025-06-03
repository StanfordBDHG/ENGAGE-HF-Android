package edu.stanford.bdh.engagehf.modules.navigation.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import edu.stanford.bdh.engagehf.modules.navigation.Navigator
import edu.stanford.bdh.engagehf.modules.navigation.internal.NavigatorImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class NavigationModule {

    @Binds
    internal abstract fun bindNavigator(impl: NavigatorImpl): Navigator
}
