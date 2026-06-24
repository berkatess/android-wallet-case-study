package com.ba.walletcase.data.demo

import com.ba.walletcase.domain.demo.DataScenario
import com.ba.walletcase.domain.demo.DemoScenarioController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Demo-only — delete before production.
 *
 * The single source of truth for the demo scenario selection. The dev toggle in the
 * UI writes via [setScenario]; [MockWalletDataSource][com.ba.walletcase.data.remote.MockWalletDataSource]
 * reads the current value. Implements the `:domain` [DemoScenarioController] interface
 * so the feature module can drive it without depending on `:data`.
 */
@Singleton
class DemoScenarioStore @Inject constructor() : DemoScenarioController {

    private val _scenario = MutableStateFlow(DataScenario.LOADED)
    override val scenario: StateFlow<DataScenario> = _scenario.asStateFlow()

    override fun setScenario(scenario: DataScenario) {
        _scenario.value = scenario
    }
}
