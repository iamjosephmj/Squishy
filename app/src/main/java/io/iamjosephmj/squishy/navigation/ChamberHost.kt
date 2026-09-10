package io.iamjosephmj.squishy.navigation

import androidx.compose.runtime.Composable
import io.iamjosephmj.squishy.chamber.childmode.ChildModeScreen
import io.iamjosephmj.squishy.chamber.container.ContainerScreen
import io.iamjosephmj.squishy.chamber.curves.CurvesScreen
import io.iamjosephmj.squishy.chamber.lazylist.LazyListScreen
import io.iamjosephmj.squishy.chamber.playground.PlaygroundScreen
import io.iamjosephmj.squishy.chamber.roles.RolesScreen

@Composable
fun ChamberContent(screen: DemoScreen, onBack: () -> Unit) {
    when (screen) {
        DemoScreen.Container -> ContainerScreen(onBack = onBack)
        DemoScreen.ChildMode -> ChildModeScreen(onBack = onBack)
        DemoScreen.LazyColumn -> LazyListScreen(onBack = onBack)
        DemoScreen.Playground -> PlaygroundScreen(onBack = onBack)
        DemoScreen.Curves -> CurvesScreen(onBack = onBack)
        DemoScreen.Roles -> RolesScreen(onBack = onBack)
    }
}
