package io.iamjosephmj.squishy

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.iamjosephmj.squishy.physics.OverScrollConfig
import io.iamjosephmj.squishy.scroll.OverScrollArea
import io.iamjosephmj.squishy.scroll.overScroll
import io.iamjosephmj.squishy.state.OverScrollState
import io.iamjosephmj.squishy.state.rememberOverScrollState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.math.abs

@OptIn(ExperimentalFoundationApi::class)
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w360dp-h640dp-420dpi")
class NestedAxesScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val vPeak = mutableFloatStateOf(0f)
    private val hPeak = mutableFloatStateOf(0f)
    private lateinit var vState: OverScrollState
    private lateinit var hState: OverScrollState

    /** Stops the fling early and hands back residual velocity, like iOS-style. */
    private val earlyStopFling =
        object : androidx.compose.foundation.gestures.FlingBehavior {
            override suspend fun androidx.compose.foundation.gestures.ScrollScope.performFling(
                initialVelocity: Float,
            ): Float = initialVelocity
        }

    @Composable
    private fun ScreenUnderTest(flingBehavior: androidx.compose.foundation.gestures.FlingBehavior? = null) {
        vState = rememberOverScrollState(
            config = remember { OverScrollConfig(maxOverscroll = 700f) },
        )
        hState = rememberOverScrollState(
            config = remember {
                OverScrollConfig(orientation = Orientation.Horizontal, maxOverscroll = 500f)
            },
        )
        LaunchedEffect(vState) {
            snapshotFlow { vState.overscrollOffset }.collect {
                if (abs(it) > vPeak.floatValue) vPeak.floatValue = abs(it)
            }
        }
        LaunchedEffect(hState) {
            snapshotFlow { hState.overscrollOffset }.collect {
                if (abs(it) > hPeak.floatValue) hPeak.floatValue = abs(it)
            }
        }
        Column(Modifier.fillMaxSize()) {
            Box(Modifier.height(60.dp))
            OverScrollArea(vState, Modifier.fillMaxSize()) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    repeat(3) { index ->
                        BasicText("row $index", Modifier.fillMaxWidth().height(80.dp))
                    }
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .onGloballyPositioned {
                                println("DIAG row bounds " + it.boundsInRoot())
                            }
                            .overScroll(hState, flingBehavior = flingBehavior),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        repeat(10) { index ->
                            Box(Modifier.width(160.dp).fillMaxHeight())
                        }
                    }
                    repeat(4) { index ->
                        BasicText("row ${index + 3}", Modifier.fillMaxWidth().height(80.dp))
                    }
                }
            }
        }
    }

    @Test
    fun horizontalRow_bouncesAtItsEdge_whileVerticalStaysQuiet() {
        composeRule.setContent { ScreenUnderTest() }
        composeRule.waitForIdle()

        // Drag the row left until it pins at its end edge (range ~3644 px).
        repeat(5) {
            composeRule.onRoot().performTouchInput {
                down(Offset(500f, 1041f))
                repeat(20) { moveBy(Offset(-40f, 0f), delayMillis = 25) }
                up()
            }
        }
        composeRule.waitForIdle()
        // One more drag at the pinned edge: the row should bounce.
        composeRule.onRoot().performTouchInput {
            down(Offset(500f, 1041f))
            repeat(20) { moveBy(Offset(-40f, 0f), delayMillis = 25) }
            up()
        }
        composeRule.waitUntil(5_000) { hState.overscrollOffset == 0f }
        composeRule.runOnIdle {
            println(
                "REPRO nested-axes: hPeak=${hPeak.floatValue} vPeak=${vPeak.floatValue} " +
                    "hScroll=${hState.scrollValue} hMax=${hState.maxScrollValue}"
            )
            assertTrue(
                "horizontal row should bounce at its edge; peak was ${hPeak.floatValue}",
                hPeak.floatValue > 100f
            )
            assertEquals(
                "horizontal gestures must not move the vertical state",
                0f,
                vPeak.floatValue
            )
        }
    }

    @Test
    fun horizontalRowFlick_atEdge_bounces() {
        composeRule.setContent { ScreenUnderTest() }
        composeRule.waitForIdle()

        // Pin the row at its end edge.
        repeat(5) {
            composeRule.onRoot().performTouchInput {
                down(Offset(500f, 1041f))
                repeat(20) { moveBy(Offset(-40f, 0f), delayMillis = 25) }
                up()
            }
        }
        composeRule.waitForIdle()
        // Flick it: real velocity into the pinned edge should bounce.
        composeRule.onRoot().performTouchInput {
            swipe(Offset(700f, 1041f), Offset(100f, 1041f))
        }
        composeRule.waitUntil(5_000) { hState.overscrollOffset == 0f }
        composeRule.runOnIdle {
            println("REPRO flick: hPeak=" + hPeak.floatValue)
            assertTrue(
                "a flick into the pinned edge should bounce; peak was ${hPeak.floatValue}",
                hPeak.floatValue > 100f
            )
        }
    }

    @Test
    fun horizontalRowFling_leftoverVelocity_pullsOverscroll() {
        composeRule.setContent { ScreenUnderTest(flingBehavior = earlyStopFling) }
        composeRule.waitForIdle()

        // Scroll the row into its range but NOT to the edge (3 of ~4.5 drags).
        repeat(3) {
            composeRule.onRoot().performTouchInput {
                down(Offset(500f, 1041f))
                repeat(20) { moveBy(Offset(-40f, 0f), delayMillis = 25) }
                up()
            }
        }
        composeRule.waitForIdle()
        composeRule.runOnIdle {
            println(
                "REPRO fling-pull: hScroll=${hState.scrollValue}/${hState.maxScrollValue} " +
                    "pre-flick peak=${hPeak.floatValue}"
            )
        }

        // Flick toward the end edge. The drag stays inside the range (600 px
        // of ~1200 remaining), so any overscroll must come from the fling's
        // leftover velocity pulling the row past the edge.
        composeRule.onRoot().performTouchInput {
            swipe(Offset(700f, 1041f), Offset(100f, 1041f))
        }
        composeRule.waitUntil(5_000) { hState.overscrollOffset == 0f }
        composeRule.runOnIdle {
            println("REPRO fling-pull: hPeak=" + hPeak.floatValue + " hScroll=" + hState.scrollValue)
            assertTrue(
                "leftover fling velocity should pull the row into overscroll; " +
                    "peak was ${hPeak.floatValue}",
                hPeak.floatValue > 100f
            )
            assertTrue(
                "drag phase must stay inside the range for this test to be valid",
                hState.scrollValue <= hState.maxScrollValue
            )
        }
    }
}
