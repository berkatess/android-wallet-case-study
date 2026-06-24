package com.ba.walletcase.data.remote

import com.ba.walletcase.data.remote.dto.WalletResponseDto

/**
 * The "network" boundary inside `:data`. Returns the raw wire model
 * ([WalletResponseDto]); today a mock implementation reads bundled JSON, tomorrow a
 * Retrofit implementation can replace it without touching the repository or above.
 */
interface WalletDataSource {
    suspend fun getDashboard(): WalletResponseDto
}
