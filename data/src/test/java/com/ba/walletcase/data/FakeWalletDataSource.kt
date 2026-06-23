package com.ba.walletcase.data

import com.ba.walletcase.data.remote.WalletDataSource
import com.ba.walletcase.data.remote.dto.WalletDto
import com.ba.walletcase.data.remote.dto.WalletResponseDto
import java.io.IOException

class FakeWalletDataSource : WalletDataSource {

    var shouldThrow = false
    var response: WalletResponseDto = WalletResponseDto(
        wallet = WalletDto(
            id = "w-test",
            currency = "TRY",
            balance = 500.0,
            createdAt = "2025-01-01T00:00:00Z",
        ),
    )

    override suspend fun getDashboard(): WalletResponseDto {
        if (shouldThrow) throw IOException("test error")
        return response
    }
}
