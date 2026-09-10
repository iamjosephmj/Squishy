package io.iamjosephmj.squishy.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.iamjosephmj.squishy.state.OverScrollState
import io.iamjosephmj.squishy.ui.theme.Aurora
import io.iamjosephmj.squishy.ui.theme.TextBright
import io.iamjosephmj.squishy.ui.theme.TextMuted2
import kotlin.math.abs

/** Live readout of the shared state: offset px, progress, scroll, ACTIVE/IDLE. */
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
