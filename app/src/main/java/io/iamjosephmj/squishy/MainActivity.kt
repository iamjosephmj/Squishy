package io.iamjosephmj.squishy

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import io.iamjosephmj.squishy.home.HomeScreen
import io.iamjosephmj.squishy.navigation.ChamberContent
import io.iamjosephmj.squishy.navigation.DemoScreen
import io.iamjosephmj.squishy.navigation.axisEnterTransition
import io.iamjosephmj.squishy.navigation.axisExitTransition
import io.iamjosephmj.squishy.ui.theme.SquishyTheme
import kotlinx.serialization.Serializable

@Serializable
private data object HomeKey : NavKey

@Serializable
private data class ChamberKey(val screen: DemoScreen) : NavKey

/**
 * The single activity: hosts the Navigation 3 back stack with shared-axis
 * transitions and the predictive-back spec.
 */
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
                                onBack = dropUnlessResumed { backStack.removeLastOrNull() },
                            )
                        }
                    },
                    transitionSpec = {
                        axisEnterTransition(0.28f) togetherWith axisExitTransition(-0.10f)
                    },
                    popTransitionSpec = {
                        axisEnterTransition(-0.28f) togetherWith axisExitTransition(0.10f)
                    },
                    predictivePopTransitionSpec = {
                        axisEnterTransition(-0.28f) togetherWith axisExitTransition(0.10f)
                    },
                )
            }
        }
    }
}
