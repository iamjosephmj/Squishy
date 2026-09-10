package io.iamjosephmj.squishy.chamber.playground

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.iamjosephmj.squishy.child.childOverScrollSupport
import io.iamjosephmj.squishy.navigation.DemoScreen
import io.iamjosephmj.squishy.physics.OverScrollConfig
import io.iamjosephmj.squishy.physics.OverscrollCurve
import io.iamjosephmj.squishy.scroll.overScroll
import io.iamjosephmj.squishy.state.rememberOverScrollState
import io.iamjosephmj.squishy.ui.ApertureChip
import io.iamjosephmj.squishy.ui.DemoRow
import io.iamjosephmj.squishy.ui.ScreenScaffold
import io.iamjosephmj.squishy.ui.SectionLabel
import io.iamjosephmj.squishy.ui.TelemetryRow
import io.iamjosephmj.squishy.visual.OverscrollVisual
import io.iamjosephmj.squishy.visual.OverscrollVisuals
import io.iamjosephmj.squishy.visual.plus

@Composable
fun PlaygroundScreen(onBack: () -> Unit, viewModel: PlaygroundViewModel = viewModel()) {
    val visuals: List<Pair<String, OverscrollVisual>> = listOf(
        "PushDown" to OverscrollVisuals.pushDown(),
        "Zoom" to OverscrollVisuals.zoom(),
        "Rotate" to OverscrollVisuals.rotate(),
        "Tilt" to OverscrollVisuals.tilt(),
        "Blur" to OverscrollVisuals.blur(),
        "Zoom+Fade" to (OverscrollVisuals.zoom() + OverscrollVisuals.fade()),
    )
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
                        selected = index == viewModel.selected,
                        onClick = { viewModel.select(index) },
                        text = visuals[index].first,
                    )
                }
            }
            SectionLabel(
                "visual · " + visuals[viewModel.selected].first.lowercase(),
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
                    DemoRow(
                        index,
                        Modifier.childOverScrollSupport(
                            state,
                            visual = visuals[viewModel.selected].second,
                        ),
                    )
                }
            }
        }
    }
}
