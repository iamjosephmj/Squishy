package io.iamjosephmj.squishy.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import io.iamjosephmj.squishy.child.OverScrollRoles
import io.iamjosephmj.squishy.child.overscrollRole
import io.iamjosephmj.squishy.child.rememberOverScrollRoles
import io.iamjosephmj.squishy.navigation.DemoScreen
import io.iamjosephmj.squishy.physics.OverScrollConfig
import io.iamjosephmj.squishy.physics.OverscrollCurve
import io.iamjosephmj.squishy.scroll.OverScrollArea
import io.iamjosephmj.squishy.state.OverScrollState
import io.iamjosephmj.squishy.state.rememberOverScrollState
import io.iamjosephmj.squishy.ui.ApertureCard
import io.iamjosephmj.squishy.ui.CosmicBackground
import io.iamjosephmj.squishy.ui.Eyebrow
import io.iamjosephmj.squishy.ui.theme.SerifItalic
import io.iamjosephmj.squishy.ui.theme.Starlight
import io.iamjosephmj.squishy.ui.theme.TextBright
import io.iamjosephmj.squishy.ui.theme.TextMuted2
import io.iamjosephmj.squishy.ui.theme.TextSoft

private val WordmarkStroke = Stroke(width = 1.4f)

@Composable
fun HomeScreen(onOpen: (DemoScreen) -> Unit) {
    val state = rememberOverScrollState(
        config = remember {
            OverScrollConfig(maxOverscroll = 520f, curve = OverscrollCurve.RubberBand())
        },
    )
    val roles = rememberOverScrollRoles {
        transform("header") { translationY = value * 0.10f }
        transform("row") { translationY = value * (0.22f + index * 0.012f) }
    }
    Box {
        CosmicBackground(Modifier.fillMaxSize())
        OverScrollArea(state, Modifier.fillMaxSize(), containerEffect = false) {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    top = 36.dp,
                    bottom = 28.dp,
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.overscrollRole(state, roles, "header"),
                    ) {
                        Canvas(Modifier.size(30.dp)) {
                            drawCircle(color = Starlight, radius = size.minDimension / 2f, style = WordmarkStroke)
                            drawCircle(color = Starlight, radius = size.minDimension / 3.2f, style = WordmarkStroke)
                            drawCircle(color = Starlight, radius = size.minDimension / 9f)
                        }
                        Spacer(Modifier.padding(4.dp))
                        Text(
                            text = "Squishy",
                            style = MaterialTheme.typography.headlineMedium,
                            color = TextBright,
                        )
                    }
                }
                item { Spacer(Modifier.height(8.dp)) }
                item {
                    Eyebrow(
                        "overscroll toolkit · jetpack compose",
                        Modifier.overscrollRole(state, roles, "header"),
                    )
                }
                item {
                    Column(
                        modifier = Modifier.overscrollRole(state, roles, "header"),
                    ) {
                        Text(
                            text = "Overscroll,",
                            style = MaterialTheme.typography.headlineMedium,
                            color = TextBright,
                        )
                        Text(
                            text = "reimagined.",
                            style = SerifItalic.copy(
                                fontSize = MaterialTheme.typography.headlineMedium.fontSize,
                                lineHeight = MaterialTheme.typography.headlineMedium.lineHeight,
                            ),
                            color = Starlight,
                        )
                    }
                }
                item {
                    Text(
                        text = "A plugin system for Jetpack Compose overscroll — swap visuals, " +
                            "tune the physics, animate every item. Pick a chamber below.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSoft,
                        modifier = Modifier.padding(top = 10.dp, bottom = 18.dp),
                    )
                }
                DemoScreen.entries.forEachIndexed { position, demo ->
                    item(key = demo.name) {
                        DemoCard(demo, state, roles, position) { onOpen(demo) }
                    }
                }
            }
        }
    }
}

@Composable
private fun DemoCard(
    demo: DemoScreen,
    state: OverScrollState,
    roles: OverScrollRoles,
    index: Int,
    onOpen: () -> Unit,
) {
    ApertureCard(
        Modifier
            .fillMaxWidth()
            .overscrollRole(state, roles, "row", index = index)
            .clickable(onClick = onOpen),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 28.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = demo.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = TextBright,
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = demo.blurb,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSoft,
                )
            }
            Spacer(Modifier.padding(6.dp))
            Text(
                text = demo.num,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted2,
            )
        }
    }
}
