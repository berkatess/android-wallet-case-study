package com.ba.walletcase.feature.wallet.component

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ba.walletcase.core.designsystem.component.SectionHeader
import com.ba.walletcase.domain.model.Transaction

@Composable
fun TransactionsSection(
    transactions: List<Transaction>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        SectionHeader(title = "Recent Transactions")
        transactions.forEachIndexed { index, transaction ->
            TransactionItem(transaction = transaction)
            if (index < transactions.lastIndex) {
                HorizontalDivider()
            }
        }
    }
}
