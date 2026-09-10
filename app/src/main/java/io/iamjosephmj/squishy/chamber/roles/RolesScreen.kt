package io.iamjosephmj.squishy.chamber.roles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.iamjosephmj.squishy.child.overscrollRole
import io.iamjosephmj.squishy.child.rememberOverScrollRoles
import io.iamjosephmj.squishy.navigation.DemoScreen
import io.iamjosephmj.squishy.physics.OverScrollConfig
import io.iamjosephmj.squishy.physics.OverscrollCurve
import io.iamjosephmj.squishy.scroll.overScroll
import io.iamjosephmj.squishy.state.rememberOverScrollState
import io.iamjosephmj.squishy.ui.ApertureCard
import io.iamjosephmj.squishy.ui.ScreenScaffold
import io.iamjosephmj.squishy.ui.SectionLabel
import io.iamjosephmj.squishy.ui.TelemetryRow
import io.iamjosephmj.squishy.ui.theme.TextBright
import io.iamjosephmj.squishy.ui.theme.TextMuted
import io.iamjosephmj.squishy.ui.theme.TextMuted2

@Composable
fun RolesScreen(onBack: () -> Unit) {
    val state = rememberOverScrollState(
        config = remember {
            OverScrollConfig(maxOverscroll = 520f, curve = OverscrollCurve.RubberBand())
        },
    )
    val roles = rememberOverScrollRoles {
        transform("header") { translationY = value * 0.10f }
        transform("section") { translationY = value * 0.16f }
        transform("row") { translationY = value * (0.22f + index * 0.012f) }
    }
    ScreenScaffold(DemoScreen.Roles.title, onBack, intro = DemoScreen.Roles.detail) {
        Column(Modifier.fillMaxSize()) {
            Box(Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
                TelemetryRow(state)
            }
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .overScroll(state, containerEffect = false),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, top = 4.dp)
                        .overscrollRole(state, roles, "header"),
                ) {
                    Text(
                        text = "Tagged roles",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextBright,
                    )
                    Text(
                        text = "Each element is tagged. The pull moves them at three depths.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                    )
                }
                SectionLabel(
                    "rows",
                    Modifier
                        .padding(start = 4.dp)
                        .overscrollRole(state, roles, "section"),
                )
                repeat(12) { row ->
                    ApertureCard(
                        Modifier
                            .fillMaxWidth()
                            .overscrollRole(state, roles, "row", index = row),
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                text = "Row %02d".format(row + 1),
                                style = MaterialTheme.typography.titleMedium,
                                color = TextBright,
                            )
                            Text(
                                text = "tag: row · index %d · depth %.2fx".format(row, 0.22f + row * 0.012f),
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted2,
                            )
                        }
                    }
                }
                Text(
                    text = "header 0.10x · section 0.16x · row 0.22x + 0.012 per index",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted2,
                    modifier = Modifier
                        .padding(start = 4.dp, bottom = 24.dp)
                        .overscrollRole(state, roles, "section"),
                )
            }
        }
    }
}
