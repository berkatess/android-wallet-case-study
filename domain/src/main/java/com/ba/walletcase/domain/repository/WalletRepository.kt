package com.ba.walletcase.domain.repository

import com.ba.walletcase.domain.model.WalletDashboard

/**
 * The decoupling seam between presentation and data.
 *
 * Returns domain models only — the wire/DTO shape never leaks past this boundary.
 * Today an implementation in `:data` reads bundled mock JSON; swapping it for a
 * real network client touches only `:data`, never the ViewModel or UI.
 */
interface WalletRepository {
    suspend fun getDashboard(): WalletDashboard
}
