package io.iamjosephmj.squishy.screens

import io.iamjosephmj.squishy.child.*
import io.iamjosephmj.squishy.physics.*
import io.iamjosephmj.squishy.scroll.*
import io.iamjosephmj.squishy.state.*
import io.iamjosephmj.squishy.visual.*

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Slider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.iamjosephmj.squishy.ui.ApertureChip
import io.iamjosephmj.squishy.ui.ApertureCard
import io.iamjosephmj.squishy.ui.SectionLabel
import io.iamjosephmj.squishy.ui.TelemetryRow

@Composable
fun PlaygroundScreen(onBack: () -> Unit) {
    val visuals: List<Pair<String, OverscrollVisual>> = listOf(
        "PushDown" to OverscrollVisuals.pushDown(),
        "Zoom" to OverscrollVisuals.zoom(),
        "Rotate" to OverscrollVisuals.rotate(),
        "Tilt" to OverscrollVisuals.tilt(),
        "Blur" to OverscrollVisuals.blur(),
        "Zoom+Fade" to (OverscrollVisuals.zoom() + OverscrollVisuals.fade()),
    )
    var selected by remember { mutableIntStateOf(0) }
    val config = remember {
        OverScrollConfig(maxOverscroll = 600f, curve = OverscrollCurve.RubberBand())
    }
    val state = rememberOverScrollState(config = config)
    ScreenScaffold(DemoScreen.Playground.title, onBack, intro = DemoScreen.Playground.detail) {
        Column(Modifier.fillMaxSize()) {
            LazyRow(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(visuals.size, key = { it }) { index ->
                    ApertureChip(
                        selected = index == selected,
                        onClick = { selected = index },
                        text = visuals[index].first,
                    )
                }
            }
            SectionLabel(
                "visual · " + visuals[selected].first.lowercase(),
                Modifier.padding(horizontal = 20.dp),
            )
            Box(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                TelemetryRow(state)
            }
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .overScroll(state, containerEffect = false),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                repeat(14) { index ->
                    DemoRow(index, Modifier.childOverScrollSupport(state, visual = visuals[selected].second))
                }
            }
        }
    }
}

@Composable
fun CurvesLabScreen(onBack: () -> Unit) {
    var curveIndex by remember { mutableIntStateOf(0) }
    var topEnabled by remember { mutableStateOf(true) }
    var bottomEnabled by remember { mutableStateOf(true) }
    var maxOverscroll by remember { mutableFloatStateOf(800f) }
    val curveNames = listOf("Linear", "Rubber band", "Heavy band", "Half gain")
    val curve: OverscrollCurve = remember(curveIndex) {
        when (curveIndex) {
            0 -> OverscrollCurve.Linear
            1 -> OverscrollCurve.RubberBand()
            2 -> OverscrollCurve.RubberBand(stiffness = 9f)
            else -> OverscrollCurve.Custom { rawDelta, _, _ -> rawDelta * 0.5f }
        }
    }
    val config = remember(curve, topEnabled, bottomEnabled, maxOverscroll) {
        OverScrollConfig(
            maxOverscroll = maxOverscroll,
            curve = curve,
            topEdge = EdgeConfig(enabled = topEnabled),
            bottomEdge = EdgeConfig(enabled = bottomEnabled),
        )
    }
    val state = rememberOverScrollState(config = config)
    var editing by remember { mutableStateOf(true) }
    val summary = "${curveNames[curveIndex].uppercase()} · " +
        (if (topEnabled) "TOP ON" else "TOP OFF") + " · " +
        (if (bottomEnabled) "BOTTOM ON" else "BOTTOM OFF") + " · " +
        "${maxOverscroll.toInt()} PX"
    ScreenScaffold(DemoScreen.Curves.title, onBack, intro = DemoScreen.Curves.detail) {
        Column(Modifier.fillMaxSize()) {
            AnimatedVisibility(
                visible = editing,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                ApertureCard(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                ) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SectionLabel("curve")
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            curveNames.forEachIndexed { index, name ->
                                ApertureChip(
                                    selected = index == curveIndex,
                                    onClick = { curveIndex = index },
                                    text = name,
                                )
                            }
                        }
                        SectionLabel("edges")
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            ApertureChip(selected = topEnabled, onClick = { topEnabled = !topEnabled }, text = "Top on")
                            ApertureChip(selected = bottomEnabled, onClick = { bottomEnabled = !bottomEnabled }, text = "Bottom on")
                        }
                        SectionLabel("max overscroll · ${maxOverscroll.toInt()} px")
                        Slider(
                            value = maxOverscroll,
                            onValueChange = { maxOverscroll = it },
                            valueRange = 200f..1500f,
                        )
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color(0xFFF1E7C2), Color(0xFFD8C690)),
                                    ),
                                    RoundedCornerShape(10.dp),
                                )
                                .clickable { editing = false },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "APPLY",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF14171F),
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(vertical = 14.dp),
                            )
                        }
                    }
                }
            }
            AnimatedVisibility(
                visible = !editing,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SectionLabel(summary, Modifier.weight(1f))
                    ApertureChip(selected = false, onClick = { editing = true }, text = "Edit")
                }
            }
            Box(Modifier.padding(horizontal = 20.dp)) {
                TelemetryRow(state)
            }
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
                    .overScroll(state),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                repeat(14) { index ->
                    DemoRow(index)
                }
            }
        }
    }
}
