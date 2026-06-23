package com.ba.walletcase.data.repository

import com.ba.walletcase.data.mapper.toDomain
import com.ba.walletcase.data.remote.WalletDataSource
import com.ba.walletcase.domain.model.WalletDashboard
import com.ba.walletcase.domain.repository.WalletRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultWalletRepository @Inject constructor(
    private val dataSource: WalletDataSource,
) : WalletRepository {

    override suspend fun getDashboard(): WalletDashboard =
        dataSource.getDashboard().toDomain()
}
