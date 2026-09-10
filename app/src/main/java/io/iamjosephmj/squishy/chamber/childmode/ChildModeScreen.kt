package io.iamjosephmj.squishy.chamber.childmode

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.iamjosephmj.flinger.behaviours.FlingPresets
import io.iamjosephmj.squishy.child.childOverScrollSupport
import io.iamjosephmj.squishy.navigation.DemoScreen
import io.iamjosephmj.squishy.scroll.overScroll
import io.iamjosephmj.squishy.state.rememberOverScrollState
import io.iamjosephmj.squishy.ui.DemoRow
import io.iamjosephmj.squishy.ui.ScreenScaffold
import io.iamjosephmj.squishy.ui.TelemetryRow

@Composable
fun ChildModeScreen(onBack: () -> Unit) {
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
                    .overScroll(state, containerEffect = false, flingBehavior = FlingPresets.iOSStyle()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                repeat(18) { index ->
                    DemoRow(index, Modifier.childOverScrollSupport(state))
                }
            }
        }
    }
}
