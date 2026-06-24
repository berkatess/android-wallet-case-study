package com.ba.walletcase.domain.demo

import kotlinx.coroutines.flow.StateFlow

/**
 * Demo-only seam kept out of the production [com.ba.walletcase.domain.repository.WalletRepository]
 * contract. The dev toggle in the UI writes the [scenario]; the mock data source in
 * `:data` reads it. Declared in `:domain` so `:feature:wallet` can drive it without
 * ever depending on `:data`; the singleton implementation lives in `:data`.
 */
interface DemoScenarioController {
    val scenario: StateFlow<DataScenario>

    fun setScenario(scenario: DataScenario)
}
