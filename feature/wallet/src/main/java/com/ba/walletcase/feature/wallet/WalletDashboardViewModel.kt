package com.ba.walletcase.feature.wallet

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ba.walletcase.domain.demo.DataScenario
import com.ba.walletcase.domain.demo.DemoScenarioController
import com.ba.walletcase.domain.repository.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val KEY_SCENARIO = "selected_scenario"

@HiltViewModel
class WalletDashboardViewModel @Inject constructor(
    private val repository: WalletRepository,
    private val scenarioController: DemoScenarioController,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow<WalletDashboardUiState>(WalletDashboardUiState.Loading)

    // WhileSubscribed(5_000) keeps the flow alive for 5 s after the last subscriber
    // (survives a config change, drops after the screen is genuinely gone).
    val uiState = _uiState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = WalletDashboardUiState.Loading,
        )

    // Derives the current scenario from SavedStateHandle so the Route can observe it
    // as a State without the Screen needing to know about the ViewModel or SSH directly.
    val selectedScenario = savedStateHandle
        .getStateFlow(KEY_SCENARIO, DataScenario.LOADED.ordinal)
        .map { DataScenario.entries[it] }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DataScenario.LOADED,
        )

    init {
        // Restore scenario from SavedStateHandle (process-death survival).
        // Stored as Int ordinal because SavedStateHandle only persists Bundle-safe types.
        val savedOrdinal = savedStateHandle.get<Int>(KEY_SCENARIO)
        if (savedOrdinal != null) {
            val restored = DataScenario.entries[savedOrdinal]
            scenarioController.setScenario(restored)
        }
        load()
    }

    fun onAction(action: WalletDashboardAction) {
        when (action) {
            is WalletDashboardAction.Retry -> load()
            is WalletDashboardAction.TopUp -> Unit // snackbar handled by the UI layer
            is WalletDashboardAction.SelectScenario -> {
                savedStateHandle[KEY_SCENARIO] = action.scenario.ordinal
                scenarioController.setScenario(action.scenario)
                load()
            }
        }
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.value = WalletDashboardUiState.Loading
            _uiState.value = try {
                val dashboard = repository.getDashboard()
                if (dashboard.children.isEmpty() && dashboard.recentTransactions.isEmpty()) {
                    WalletDashboardUiState.Empty
                } else {
                    WalletDashboardUiState.Success(dashboard)
                }
            } catch (e: Exception) {
                WalletDashboardUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}
