package com.ba.walletcase.data

import com.ba.walletcase.data.repository.DefaultWalletRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.IOException

class DefaultWalletRepositoryTest {

    private lateinit var fakeSource: FakeWalletDataSource
    private lateinit var repo: DefaultWalletRepository

    @Before
    fun setUp() {
        fakeSource = FakeWalletDataSource()
        repo = DefaultWalletRepository(fakeSource)
    }

    @Test
    fun `getDashboard returns mapped WalletDashboard when source succeeds`() = runTest {
        val result = repo.getDashboard()
        assertEquals("w-test", result.wallet.id)
        assertEquals("TRY", result.wallet.currency)
        assertEquals(0, result.wallet.balance.compareTo(java.math.BigDecimal("500")))
    }

    @Test
    fun `getDashboard propagates IOException when source throws`() = runTest {
        fakeSource.shouldThrow = true
        var caught: IOException? = null
        try {
            repo.getDashboard()
        } catch (e: IOException) {
            caught = e
        }
        assertEquals("test error", caught?.message)
    }
}
