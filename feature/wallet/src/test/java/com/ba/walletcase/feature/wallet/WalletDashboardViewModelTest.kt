package com.ba.walletcase.feature.wallet

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.ba.walletcase.domain.demo.DataScenario
import com.ba.walletcase.domain.demo.DemoScenarioController
import com.ba.walletcase.domain.model.Child
import com.ba.walletcase.domain.model.Transaction
import com.ba.walletcase.domain.model.TransactionCategory
import com.ba.walletcase.domain.model.TransactionType
import com.ba.walletcase.domain.model.Wallet
import com.ba.walletcase.domain.model.WalletDashboard
import com.ba.walletcase.domain.repository.WalletRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.io.IOException
import java.math.BigDecimal
import java.time.Instant

// ---------------------------------------------------------------------------
// Test infrastructure
// ---------------------------------------------------------------------------

class MainDispatcherRule(
    val dispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }
    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

private class FakeWalletRepository : WalletRepository {
    var shouldThrow = false
    var dashboard: WalletDashboard = WalletDashboard(
        wallet = Wallet(
            id = "w1",
            currency = "TRY",
            balance = BigDecimal("1250.75"),
            createdAt = Instant.parse("2025-01-01T00:00:00Z"),
        ),
        children = listOf(
            Child(
                id = "c1",
                firstName = "Test",
                lastName = "Child",
                avatarUrl = null,
                age = 10,
                grade = "5th",
                schoolName = "Test School",
                balance = BigDecimal("50.00"),
            )
        ),
        recentTransactions = listOf(
            Transaction(
                id = "t1",
                type = TransactionType.EXPENSE,
                category = TransactionCategory.CAFETERIA,
                description = "Lunch",
                amount = BigDecimal("15.00"),
                currency = "TRY",
                date = Instant.parse("2026-03-01T10:00:00Z"),
                childId = "c1",
            )
        ),
    )

    override suspend fun getDashboard(): WalletDashboard {
        if (shouldThrow) throw IOException("test error")
        return dashboard
    }
}

private class FakeDemoScenarioController : DemoScenarioController {
    private val _scenario = MutableStateFlow(DataScenario.LOADED)
    override val scenario: StateFlow<DataScenario> = _scenario.asStateFlow()
    override fun setScenario(scenario: DataScenario) {
        _scenario.value = scenario
    }
}

// ---------------------------------------------------------------------------
// Tests
// ---------------------------------------------------------------------------

class WalletDashboardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeRepo: FakeWalletRepository
    private lateinit var fakeController: FakeDemoScenarioController

    private val emptyDashboard = WalletDashboard(
        wallet = Wallet(
            id = "w1",
            currency = "TRY",
            balance = BigDecimal.ZERO,
            createdAt = Instant.parse("2025-01-01T00:00:00Z"),
        ),
        children = emptyList(),
        recentTransactions = emptyList(),
    )

    @Before
    fun setUp() {
        fakeRepo = FakeWalletRepository()
        fakeController = FakeDemoScenarioController()
    }

    private fun createViewModel() = WalletDashboardViewModel(
        repository = fakeRepo,
        scenarioController = fakeController,
        savedStateHandle = SavedStateHandle(),
    )

    @Test
    fun `initial state is Loading`() = runTest {
        // Use a repo that suspends indefinitely so init's load() never resolves,
        // keeping _uiState at Loading when Turbine subscribes.
        val blockingRepo = object : WalletRepository {
            override suspend fun getDashboard(): WalletDashboard =
                CompletableDeferred<WalletDashboard>().await()
        }
        val viewModel = WalletDashboardViewModel(
            repository = blockingRepo,
            scenarioController = fakeController,
            savedStateHandle = SavedStateHandle(),
        )
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue("Expected Loading but got: $state", state is WalletDashboardUiState.Loading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `LOADED scenario produces Success with correct wallet balance`() = runTest {
        val viewModel = createViewModel()
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue("Expected Success but got: $state", state is WalletDashboardUiState.Success)
            val success = state as WalletDashboardUiState.Success
            assertEquals(BigDecimal("1250.75"), success.dashboard.wallet.balance)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `EMPTY scenario produces Empty when children and transactions are absent`() = runTest {
        fakeRepo.dashboard = emptyDashboard
        val viewModel = createViewModel()
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue("Expected Empty but got: $state", state is WalletDashboardUiState.Empty)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `ERROR scenario produces Error when repository throws`() = runTest {
        fakeRepo.shouldThrow = true
        val viewModel = createViewModel()
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue("Expected Error but got: $state", state is WalletDashboardUiState.Error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Retry action re-triggers load and returns Success`() = runTest {
        fakeRepo.shouldThrow = true
        val viewModel = createViewModel()
        viewModel.uiState.test {
            // Initial Error from init
            assertTrue(awaitItem() is WalletDashboardUiState.Error)

            // Fix the repo and retry — with UnconfinedTestDispatcher the new load
            // coroutine runs eagerly and StateFlow conflates the transient Loading,
            // so only the final Success is observable.
            fakeRepo.shouldThrow = false
            viewModel.onAction(WalletDashboardAction.Retry)

            assertTrue(awaitItem() is WalletDashboardUiState.Success)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `SelectScenario action changes scenario and reloads`() = runTest {
        val viewModel = createViewModel()
        viewModel.uiState.test {
            // Initial Success
            assertTrue(awaitItem() is WalletDashboardUiState.Success)

            // Switch to ERROR — configure repo before dispatching so the eagerly-run
            // load coroutine sees the new behaviour immediately. StateFlow conflates
            // the transient Loading under UnconfinedTestDispatcher, so only the final
            // Error is observable.
            fakeRepo.shouldThrow = true
            viewModel.onAction(WalletDashboardAction.SelectScenario(DataScenario.ERROR))

            assertTrue(awaitItem() is WalletDashboardUiState.Error)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
