package io.iamjosephmj.squishy.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.iamjosephmj.squishy.OverScrollState
import io.iamjosephmj.squishy.ui.theme.ApertureBg2
import io.iamjosephmj.squishy.ui.theme.ApertureLine
import io.iamjosephmj.squishy.ui.theme.ApertureLine2
import io.iamjosephmj.squishy.ui.theme.AperturePanel2
import io.iamjosephmj.squishy.ui.theme.Aurora
import io.iamjosephmj.squishy.ui.theme.Starlight
import io.iamjosephmj.squishy.ui.theme.TextBright
import io.iamjosephmj.squishy.ui.theme.TextMuted
import io.iamjosephmj.squishy.ui.theme.TextMuted2
import io.iamjosephmj.squishy.ui.theme.TextSoft
import kotlin.math.abs

@Composable
fun Eyebrow(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = TextSoft,
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .border(1.dp, ApertureLine2, RoundedCornerShape(999.dp))
            .background(Color(0x08FFFFFF))
            .padding(horizontal = 12.dp, vertical = 5.dp),
    )
}

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = Aurora,
        modifier = modifier,
    )
}

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
                    colors = listOf(AperturePanel2.copy(alpha = 0.85f), ApertureBg2.copy(alpha = 0.85f)),
                ),
            )
            .border(1.dp, ApertureLine, RoundedCornerShape(16.dp)),
    ) {
        content()
    }
}

@Composable
fun TelemetryRow(state: OverScrollState, modifier: Modifier = Modifier) {
    val offset = state.overscrollOffset
    val progress = if (state.maxOverscroll > 0f) {
        (abs(offset) / state.maxOverscroll).coerceIn(0f, 1f)
    } else 0f
    val status = if (state.isOverscrolling) "ACTIVE" else "IDLE"
    ApertureCard(modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TelemetryCell("OVERSCROLL", "%+.1f px".format(offset))
            TelemetryCell("PROGRESS", "%.2f".format(progress))
            TelemetryCell("SCROLL", "${state.scrollValue}/${state.maxScrollValue}")
            TelemetryCell("STATUS", status, highlight = state.isOverscrolling)
        }
    }
}

@Composable
private fun TelemetryCell(key: String, value: String, highlight: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = key,
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted2,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            color = if (highlight) Aurora else TextBright,
            fontWeight = FontWeight.Medium,
        )
    }
}

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

@Composable
fun CircularBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Color(0x9905070D))
            .border(1.dp, ApertureLine2, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "‹",
            color = TextBright,
            fontSize = 22.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}
