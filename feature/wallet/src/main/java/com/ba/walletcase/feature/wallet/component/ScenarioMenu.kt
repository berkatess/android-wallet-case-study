package com.ba.walletcase.feature.wallet.component

// Demo-only — delete before production (together with DataScenario and DemoScenarioStore)

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.ba.walletcase.domain.demo.DataScenario

@Composable
fun ScenarioMenu(
    currentScenario: DataScenario,
    onScenarioSelected: (DataScenario) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    IconButton(
        onClick = { expanded = true },
        modifier = modifier,
    ) {
        Icon(
            imageVector = Icons.Rounded.MoreVert,
            contentDescription = "Select demo scenario",
        )
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
    ) {
        DropdownMenuItem(
            text = { Text("Loaded") },
            onClick = {
                onScenarioSelected(DataScenario.LOADED)
                expanded = false
            },
        )
        DropdownMenuItem(
            text = { Text("Empty") },
            onClick = {
                onScenarioSelected(DataScenario.EMPTY)
                expanded = false
            },
        )
        DropdownMenuItem(
            text = { Text("Error") },
            onClick = {
                onScenarioSelected(DataScenario.ERROR)
                expanded = false
            },
        )
    }
}
