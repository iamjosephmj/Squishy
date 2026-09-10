package io.iamjosephmj.squishy.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.iamjosephmj.squishy.OverScrollArea
import io.iamjosephmj.squishy.OverScrollConfig
import io.iamjosephmj.squishy.childOverScrollSupport
import io.iamjosephmj.squishy.overScroll
import io.iamjosephmj.squishy.rememberOverScrollState
import io.iamjosephmj.squishy.ui.ApertureCard
import io.iamjosephmj.squishy.ui.TelemetryRow
import io.iamjosephmj.squishy.ui.theme.TextBright
import io.iamjosephmj.squishy.ui.theme.TextSoft

@Composable
fun ContainerDemoScreen(onBack: () -> Unit) {
    val state = rememberOverScrollState()
    ScreenScaffold(DemoScreen.Container.title, onBack, intro = DemoScreen.Container.detail) {
        Column(Modifier.fillMaxSize()) {
            Box(Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
                TelemetryRow(state)
            }
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .overScroll(state),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                repeat(18) { index ->
                    DemoRow(index)
                }
            }
        }
    }
}

@Composable
fun ChildModeDemoScreen(onBack: () -> Unit) {
    val state = rememberOverScrollState()
    ScreenScaffold(DemoScreen.ChildMode.title, onBack, intro = DemoScreen.ChildMode.detail) {
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
                repeat(18) { index ->
                    DemoRow(index, Modifier.childOverScrollSupport(state))
                }
            }
        }
    }
}

@Composable
fun LazyDemoScreen(onBack: () -> Unit) {
    val state = rememberOverScrollState(
        config = remember { OverScrollConfig(maxOverscroll = 700f) },
    )
    ScreenScaffold(DemoScreen.LazyColumn.title, onBack, intro = DemoScreen.LazyColumn.detail) {
        Column(Modifier.fillMaxSize()) {
            Box(Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
                TelemetryRow(state)
            }
            OverScrollArea(state, Modifier.fillMaxSize()) {
                LazyColumn(
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(24, key = { it }) { index ->
                        DemoRow(index)
                    }
                }
            }
        }
    }
}

@Composable
fun DemoRow(index: Int, modifier: Modifier = Modifier) {
    ApertureCard(modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = "Row %02d".format(index + 1),
                style = MaterialTheme.typography.titleMedium,
                color = TextBright,
            )
            Text(
                text = "Drag past the edge and feel the plugin system respond.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSoft,
            )
        }
    }
}
