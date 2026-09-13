package io.iamjosephmj.squishy.chamber.nestedaxes

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.iamjosephmj.squishy.navigation.DemoScreen
import io.iamjosephmj.squishy.physics.OverScrollConfig
import io.iamjosephmj.squishy.scroll.OverScrollArea
import io.iamjosephmj.squishy.scroll.overScroll
import io.iamjosephmj.squishy.state.rememberOverScrollState
import io.iamjosephmj.squishy.ui.ApertureCard
import io.iamjosephmj.squishy.ui.DemoRow
import io.iamjosephmj.squishy.ui.ScreenScaffold
import io.iamjosephmj.squishy.ui.TelemetryRow
import io.iamjosephmj.squishy.ui.theme.TextBright
import io.iamjosephmj.squishy.ui.theme.TextSoft

/** Chamber 07 — a vertical scroll with a horizontal scrollable row mid-list. */
@Composable
fun NestedAxesScreen(onBack: () -> Unit) {
    val verticalState = rememberOverScrollState(
        config = remember { OverScrollConfig(maxOverscroll = 700f) },
    )
    val horizontalState = rememberOverScrollState(
        config = remember {
            OverScrollConfig(orientation = Orientation.Horizontal, maxOverscroll = 500f)
        },
    )
    ScreenScaffold(DemoScreen.NestedAxes.title, onBack, intro = DemoScreen.NestedAxes.detail) {
        Column(Modifier.fillMaxSize()) {
            Box(Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
                TelemetryRow(verticalState)
            }
            Box(Modifier.padding(horizontal = 20.dp)) {
                TelemetryRow(horizontalState)
            }
            OverScrollArea(verticalState, Modifier.fillMaxSize()) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    repeat(3) { index ->
                        DemoRow(index)
                    }
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .overScroll(horizontalState),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        repeat(10) { index ->
                            ApertureCard(Modifier.width(160.dp).fillMaxHeight()) {
                                Column(Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Card %02d".format(index + 1),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = TextBright,
                                    )
                                    Text(
                                        text = "Flick sideways — this row squishes on its own.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSoft,
                                    )
                                }
                            }
                        }
                    }
                    repeat(4) { index ->
                        DemoRow(index + 3)
                    }
                }
            }
        }
    }
}
