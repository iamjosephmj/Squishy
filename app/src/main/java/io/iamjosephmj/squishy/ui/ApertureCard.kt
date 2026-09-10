package io.iamjosephmj.squishy.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import io.iamjosephmj.squishy.ui.theme.ApertureBg2
import io.iamjosephmj.squishy.ui.theme.ApertureLine
import io.iamjosephmj.squishy.ui.theme.AperturePanel2

@Composable
fun ApertureCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        AperturePanel2.copy(alpha = 0.85f),
                        ApertureBg2.copy(alpha = 0.85f),
                    ),
                ),
            )
            .border(1.dp, ApertureLine, RoundedCornerShape(16.dp)),
    ) {
        content()
    }
}
