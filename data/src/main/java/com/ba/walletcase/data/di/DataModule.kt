package com.ba.walletcase.data.di

import com.ba.walletcase.data.demo.DemoScenarioStore
import com.ba.walletcase.data.remote.MockWalletDataSource
import com.ba.walletcase.data.remote.WalletDataSource
import com.ba.walletcase.data.repository.DefaultWalletRepository
import com.ba.walletcase.domain.demo.DemoScenarioController
import com.ba.walletcase.domain.repository.WalletRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    abstract fun bindWalletRepository(impl: DefaultWalletRepository): WalletRepository

    @Binds
    abstract fun bindWalletDataSource(impl: MockWalletDataSource): WalletDataSource

    @Binds
    abstract fun bindDemoScenarioController(impl: DemoScenarioStore): DemoScenarioController

    companion object {

        @Provides
        @Singleton
        fun provideJson(): Json = Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
    }
}
