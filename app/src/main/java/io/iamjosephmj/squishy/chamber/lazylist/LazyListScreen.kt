package io.iamjosephmj.squishy.chamber.lazylist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.iamjosephmj.flinger.behaviours.FlingPresets
import io.iamjosephmj.squishy.navigation.DemoScreen
import io.iamjosephmj.squishy.physics.OverScrollConfig
import io.iamjosephmj.squishy.scroll.OverScrollArea
import io.iamjosephmj.squishy.state.rememberOverScrollState
import io.iamjosephmj.squishy.ui.DemoRow
import io.iamjosephmj.squishy.ui.ScreenScaffold
import io.iamjosephmj.squishy.ui.TelemetryRow

@Composable
fun LazyListScreen(onBack: () -> Unit) {
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
                    flingBehavior = FlingPresets.iOSStyle(),
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
