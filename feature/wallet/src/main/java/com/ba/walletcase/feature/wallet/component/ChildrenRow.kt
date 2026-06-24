package com.ba.walletcase.feature.wallet.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ba.walletcase.domain.model.Child

@Composable
fun ChildrenRow(
    children: List<Child>,
    modifier: Modifier = Modifier,
) {
    // No horizontal contentPadding here: the parent LazyColumn already insets every
    // item by 16.dp, so the first card aligns with the BalanceCard's left edge.
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(items = children, key = { it.id }) { child ->
            ChildProfileCard(child = child)
        }
    }
}
