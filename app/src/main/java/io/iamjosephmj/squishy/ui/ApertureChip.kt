package io.iamjosephmj.squishy.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.iamjosephmj.squishy.ui.theme.ApertureLine2
import io.iamjosephmj.squishy.ui.theme.Starlight
import io.iamjosephmj.squishy.ui.theme.TextMuted

@Composable
fun ApertureChip(
    selected: Boolean,
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = if (selected) Starlight else TextMuted,
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .then(
                if (selected) {
                    Modifier.border(1.dp, Starlight.copy(alpha = 0.45f), RoundedCornerShape(999.dp))
                } else {
                    Modifier.border(1.dp, ApertureLine2, RoundedCornerShape(999.dp))
                }
            )
            .background(
                if (selected) Starlight.copy(alpha = 0.08f) else Color(0x04FFFFFF),
                RoundedCornerShape(999.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    )
}
