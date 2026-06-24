package com.ba.walletcase.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ba.walletcase.domain.demo.DataScenario
import com.ba.walletcase.domain.demo.DemoScenarioController
import com.ba.walletcase.domain.repository.WalletRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.math.BigDecimal
import javax.inject.Inject

/**
 * End-to-end test of the real `:data` pipeline:
 * `MockWalletDataSource` (reads bundled JSON assets) → DTO mapping →
 * `DefaultWalletRepository` → `WalletDashboard`. No fakes, no mocks — Hilt provides
 * the production wiring, so this verifies the actual decoding/mapping against the
 * real `mock_wallet.json` / `mock_wallet_empty.json` assets and the simulated error.
 *
 * Together with the compile-time `:feature:wallet → :domain` (never `:data`) boundary,
 * this proves "swap the mock for a real network source without touching the ViewModel"
 * both structurally and behaviourally.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class DataLayerIntegrationTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var repository: WalletRepository

    @Inject
    lateinit var scenarioController: DemoScenarioController

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun loadedScenario_returnsPopulatedDashboard() {
        scenarioController.setScenario(DataScenario.LOADED)

        val dashboard = runBlocking { repository.getDashboard() }

        assertTrue(
            "Expected a positive wallet balance but got ${dashboard.wallet.balance}",
            dashboard.wallet.balance > BigDecimal.ZERO,
        )
        assertTrue("Expected children to be present", dashboard.children.isNotEmpty())
        assertTrue("Expected recent transactions to be present", dashboard.recentTransactions.isNotEmpty())
        assertTrue(
            "Expected every child balance to be non-negative",
            dashboard.children.all { it.balance >= BigDecimal.ZERO },
        )
    }

    @Test
    fun emptyScenario_returnsEmptyDashboard() {
        scenarioController.setScenario(DataScenario.EMPTY)

        val dashboard = runBlocking { repository.getDashboard() }

        assertEquals(0, dashboard.wallet.balance.compareTo(BigDecimal.ZERO))
        assertTrue("Expected no children", dashboard.children.isEmpty())
        assertTrue("Expected no recent transactions", dashboard.recentTransactions.isEmpty())
    }

    @Test
    fun errorScenario_throwsIOException() {
        scenarioController.setScenario(DataScenario.ERROR)

        var caught: IOException? = null
        try {
            runBlocking { repository.getDashboard() }
        } catch (e: IOException) {
            caught = e
        }

        assertTrue("Expected an IOException to be thrown", caught != null)
    }
}
