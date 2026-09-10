package io.iamjosephmj.squishy.chamber.curves

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.iamjosephmj.flinger.behaviours.FlingPresets
import io.iamjosephmj.squishy.navigation.DemoScreen
import io.iamjosephmj.squishy.scroll.overScroll
import io.iamjosephmj.squishy.state.rememberOverScrollState
import io.iamjosephmj.squishy.ui.ApertureCard
import io.iamjosephmj.squishy.ui.ApertureChip
import io.iamjosephmj.squishy.ui.DemoRow
import io.iamjosephmj.squishy.ui.ScreenScaffold
import io.iamjosephmj.squishy.ui.SectionLabel
import io.iamjosephmj.squishy.ui.TelemetryRow
import io.iamjosephmj.squishy.ui.theme.TextBright

/** Chamber 05 — tune physics in the edit card; APPLY collapses to a summary. */
@Composable
fun CurvesScreen(onBack: () -> Unit, viewModel: CurvesViewModel = viewModel()) {
    val state = rememberOverScrollState(config = viewModel.config)
    ScreenScaffold(DemoScreen.Curves.title, onBack, intro = DemoScreen.Curves.detail) {
        Column(Modifier.fillMaxSize()) {
            AnimatedVisibility(
                visible = viewModel.editing,
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
                            viewModel.curveNames.forEachIndexed { index, name ->
                                ApertureChip(
                                    selected = index == viewModel.curveIndex,
                                    onClick = { viewModel.selectCurve(index) },
                                    text = name,
                                )
                            }
                        }
                        SectionLabel("edges")
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            ApertureChip(
                                selected = viewModel.topEnabled,
                                onClick = viewModel::toggleTop,
                                text = "Top on",
                            )
                            ApertureChip(
                                selected = viewModel.bottomEnabled,
                                onClick = viewModel::toggleBottom,
                                text = "Bottom on",
                            )
                        }
                        SectionLabel("max overscroll · ${viewModel.maxOverscroll.toInt()} px")
                        Slider(
                            value = viewModel.maxOverscroll,
                            onValueChange = viewModel::onMaxChange,
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
                                .clickable { viewModel.apply() },
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
                visible = !viewModel.editing,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SectionLabel(viewModel.summary, Modifier.weight(1f))
                    ApertureChip(selected = false, onClick = viewModel::edit, text = "Edit")
                }
            }
            Box(Modifier.padding(horizontal = 20.dp)) {
                TelemetryRow(state)
            }
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
                    .overScroll(state, flingBehavior = FlingPresets.iOSStyle()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                repeat(14) { index ->
                    DemoRow(index)
                }
            }
        }
    }
}
