package com.ba.walletcase.core.designsystem.modifier

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Animates a left-to-right gradient sweep over [MaterialTheme.colorScheme.surfaceVariant]
 * placeholders. No external dependency — pure Compose animation API.
 *
 * Usage: `Modifier.shimmer()` on any placeholder Box/Row/etc.
 */
fun Modifier.shimmer(): Modifier = composed {
    val base = MaterialTheme.colorScheme.surfaceVariant
    val highlight = Color.White.copy(alpha = 0.55f)

    val transition = rememberInfiniteTransition(label = "shimmer")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1_200, easing = LinearEasing),
        ),
        label = "shimmerProgress",
    )

    // Map the 0→1 progress to an x-offset spanning three widths so the highlight
    // enters from the left, sweeps across, and exits to the right cleanly.
    val brush = Brush.linearGradient(
        colors = listOf(base, highlight, base),
        start = Offset(progress * 3_000f - 1_000f, 0f),
        end = Offset(progress * 3_000f, 0f),
    )

    background(brush)
}
