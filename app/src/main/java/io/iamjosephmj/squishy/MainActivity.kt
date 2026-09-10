package io.iamjosephmj.squishy

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import io.iamjosephmj.squishy.screens.ChildModeDemoScreen
import io.iamjosephmj.squishy.screens.ContainerDemoScreen
import io.iamjosephmj.squishy.screens.CurvesLabScreen
import io.iamjosephmj.squishy.screens.DemoScreen
import io.iamjosephmj.squishy.screens.HomeScreen
import io.iamjosephmj.squishy.screens.LazyDemoScreen
import io.iamjosephmj.squishy.screens.PlaygroundScreen
import io.iamjosephmj.squishy.screens.RolesScreen
import io.iamjosephmj.squishy.ui.theme.SquishyTheme
import kotlinx.serialization.Serializable

@Serializable
private data object HomeKey : NavKey

@Serializable
private data class ChamberKey(val screen: DemoScreen) : NavKey

private val EmphasizedEasing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
private const val EnterDuration = 420
private const val ExitDuration = 240

private fun AxisEnterTransition(offsetFraction: Float): EnterTransition =
    slideInHorizontally(
        animationSpec = tween(EnterDuration, easing = EmphasizedEasing),
        initialOffsetX = { (it * offsetFraction).toInt() },
    ) + fadeIn(
        animationSpec = tween(EnterDuration, delayMillis = 40, easing = EmphasizedEasing),
    ) + scaleIn(
        animationSpec = tween(EnterDuration, easing = EmphasizedEasing),
        initialScale = 0.94f,
    )

private fun AxisExitTransition(offsetFraction: Float): ExitTransition =
    slideOutHorizontally(
        animationSpec = tween(ExitDuration, easing = EmphasizedEasing),
        targetOffsetX = { (it * offsetFraction).toInt() },
    ) + fadeOut(
        animationSpec = tween(ExitDuration, easing = EmphasizedEasing),
    ) + scaleOut(
        animationSpec = tween(ExitDuration, easing = EmphasizedEasing),
        targetScale = 0.96f,
    )

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )

        setContent {
            SquishyTheme(darkTheme = true) {
                val backStack = rememberNavBackStack(HomeKey)
                NavDisplay(
                    backStack = backStack,
                    onBack = { backStack.removeLastOrNull() },
                    modifier = Modifier.fillMaxSize(),
                    entryProvider = entryProvider {
                        entry<HomeKey> {
                            HomeScreen(onOpen = { backStack.add(ChamberKey(it)) })
                        }
                        entry<ChamberKey> { key ->
                            ChamberContent(
                                screen = key.screen,
                                onBack = { backStack.removeLastOrNull() },
                            )
                        }
                    },
                    transitionSpec = {
                        AxisEnterTransition(0.28f) togetherWith AxisExitTransition(-0.10f)
                    },
                    popTransitionSpec = {
                        AxisEnterTransition(-0.28f) togetherWith AxisExitTransition(0.10f)
                    },
                    predictivePopTransitionSpec = {
                        AxisEnterTransition(-0.28f) togetherWith AxisExitTransition(0.10f)
                    },
                )
            }
        }
    }
}

@Composable
private fun ChamberContent(screen: DemoScreen, onBack: () -> Unit) {
    when (screen) {
        DemoScreen.Container -> ContainerDemoScreen(onBack = onBack)
        DemoScreen.ChildMode -> ChildModeDemoScreen(onBack = onBack)
        DemoScreen.LazyColumn -> LazyDemoScreen(onBack = onBack)
        DemoScreen.Playground -> PlaygroundScreen(onBack = onBack)
        DemoScreen.Curves -> CurvesLabScreen(onBack = onBack)
        DemoScreen.Roles -> RolesScreen(onBack = onBack)
    }
}
