package io.iamjosephmj.squishy.screens

import io.iamjosephmj.squishy.child.*
import io.iamjosephmj.squishy.physics.*
import io.iamjosephmj.squishy.scroll.*
import io.iamjosephmj.squishy.state.*
import io.iamjosephmj.squishy.visual.*

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.iamjosephmj.squishy.ui.ApertureCard
import io.iamjosephmj.squishy.ui.CosmicBackground
import io.iamjosephmj.squishy.ui.Eyebrow
import io.iamjosephmj.squishy.ui.theme.SerifItalic
import io.iamjosephmj.squishy.ui.theme.ApertureLine
import io.iamjosephmj.squishy.ui.theme.Starlight
import io.iamjosephmj.squishy.ui.theme.TextBright
import io.iamjosephmj.squishy.ui.theme.TextMuted2
import io.iamjosephmj.squishy.ui.theme.TextSoft

enum class DemoScreen(val num: String, val title: String, val blurb: String, val detail: String) {
    Container("01", "Container", "Path 1 basics — a plain column that squishes past its edges, container visual on.", "A plain column that scrolls and then squishes past its edges — the container itself carries the overscroll visual. Watch the telemetry while dragging past the top or bottom, and fling into an edge to see the velocity bounce settle back."),
    ChildMode("02", "Child mode", "containerEffect off — every item carries the overscroll instead of the box.", "The container stays completely still — every item carries the overscroll instead. All rows share one state, so the whole list responds to the same pull while the box never moves."),
    LazyColumn("03", "Lazy column", "OverScrollArea wrapping LazyColumn — edge leftovers feed the effect.", "LazyColumn owns its scroll pipeline internally, so it runs through OverScrollArea: edge leftovers are routed into the effect and the platform stretch is suppressed. Works with any nested-scroll-aware list."),
    Playground("04", "Plugin playground", "Swap overscroll visuals per child — built-ins and a custom one, live.", "Pick a visual — every built-in plus combinations, applied per child on a rubber band. Switch the chips to swap the effect live while the same state drives the list."),
    Curves("05", "Curves & edges", "Tune the drag curve, per-edge limits and max overscroll in real time.", "Tune the physics live: the drag curve (linear, rubber band, heavy band, half gain), per-edge enable, and the max overscroll limit. Change something, then drag the list to feel the difference."),
    Roles("06", "Tagged roles", "setTag()-style routing — name an item, register its animation, done.", "Every element is tagged with a role name, and the registry maps each name to an animation. Rows also carry their index, so one registered transform varies per position — pull past an edge to see the layers fan out."),
}

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
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
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
                        drawCircle(color = Starlight, radius = size.minDimension / 2f, style = Stroke1)
                        drawCircle(color = Starlight, radius = size.minDimension / 3.2f, style = Stroke1)
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

private val Stroke1 = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.4f)

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
                fontWeight = FontWeight.Medium,
            )
        }
    }
}
