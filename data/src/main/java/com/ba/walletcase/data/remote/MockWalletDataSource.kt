package com.ba.walletcase.data.remote

import android.content.Context
import com.ba.walletcase.data.demo.DemoScenarioStore
import com.ba.walletcase.data.remote.dto.WalletResponseDto
import com.ba.walletcase.domain.demo.DataScenario
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Mock "network" data source: reads bundled JSON assets and simulates network
 * latency/failure. The served payload is chosen by the current demo scenario
 * (read single-shot via [DemoScenarioStore.scenario]`.value`, not collected), so a
 * reviewer can flip Loaded/Empty/Error live.
 */
@Singleton
class MockWalletDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
    private val demoScenarioStore: DemoScenarioStore,
) : WalletDataSource {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun getDashboard(): WalletResponseDto = withContext(Dispatchers.IO) {
        when (demoScenarioStore.scenario.value) {
            DataScenario.LOADED -> {
                delay(SIMULATED_LATENCY_MS)
                decodeAsset(LOADED_ASSET)
            }

            DataScenario.EMPTY -> {
                delay(SIMULATED_LATENCY_MS)
                decodeAsset(EMPTY_ASSET)
            }

            DataScenario.ERROR -> {
                delay(SIMULATED_LATENCY_MS)
                throw IOException("Simulated network error")
            }
        }
    }

    private fun decodeAsset(fileName: String): WalletResponseDto {
        val raw = context.assets.open(fileName).bufferedReader().use { it.readText() }
        return json.decodeFromString(WalletResponseDto.serializer(), raw)
    }

    private companion object {
        const val SIMULATED_LATENCY_MS = 600L
        const val LOADED_ASSET = "mock_wallet.json"
        const val EMPTY_ASSET = "mock_wallet_empty.json"
    }
}
