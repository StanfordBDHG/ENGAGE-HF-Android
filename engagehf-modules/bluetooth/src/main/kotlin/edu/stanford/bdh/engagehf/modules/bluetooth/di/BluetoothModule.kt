package edu.stanford.bdh.engagehf.modules.bluetooth.di

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import edu.stanford.bdh.engagehf.modules.bluetooth.api.BLEService
import edu.stanford.bdh.engagehf.modules.bluetooth.domain.BLEServiceImpl

/**
 * Dagger Hilt module for providing Bluetooth-related dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
class BluetoothModule {

    /**
     * Provides the Bluetooth adapter.
     *
     * @return The Bluetooth adapter instance obtained from the system service.
     */
    @Provides
    fun provideSystemBluetoothAdapter(
        @ApplicationContext context: Context,
    ): BluetoothAdapter = context.getSystemService(BluetoothManager::class.java).adapter

    /**
     * Dagger Hilt module for providing bindings.
     */
    @Module
    @InstallIn(SingletonComponent::class)
    abstract class Bindings {

        /**
         * Binds the implementation of [BLEService] interface.
         *
         * @param impl The implementation of [BLEService].
         * @return An instance of [BLEService].
         */
        @Binds
        internal abstract fun bindSpeziBLEService(impl: BLEServiceImpl): BLEService
    }
}
