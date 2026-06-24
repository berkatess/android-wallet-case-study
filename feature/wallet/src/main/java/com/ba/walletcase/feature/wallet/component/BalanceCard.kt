package com.ba.walletcase.feature.wallet.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import com.ba.walletcase.core.designsystem.theme.WalletCaseTheme
import com.ba.walletcase.core.util.CurrencyFormatter
import com.ba.walletcase.feature.wallet.preview.previewWallet
import java.math.BigDecimal

@Composable
fun BalanceCard(
    balance: BigDecimal,
    onTopUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "Total Balance",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = CurrencyFormatter.format(balance),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = onTopUp) {
                Text(text = "Top Up")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BalanceCardPreviewLight() {
    WalletCaseTheme {
        BalanceCard(balance = previewWallet.balance, onTopUp = {})
    }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun BalanceCardPreviewDark() {
    WalletCaseTheme {
        BalanceCard(balance = previewWallet.balance, onTopUp = {})
    }
}
