package io.iamjosephmj.squishy

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
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
class PluginIntegrationTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun zoomVisual_engagesAtEdge_andRenders() {
        lateinit var state: OverScrollState
        val peak = mutableFloatStateOf(0f)
        composeRule.setContent {
            state = rememberOverScrollState(
                visual = OverscrollVisuals.zoom(),
                config = OverScrollConfig(maxOverscroll = 800f),
            )
            LaunchedEffect(state) {
                snapshotFlow { state.overscrollOffset }.collect {
                    if (abs(it) > peak.floatValue) peak.floatValue = abs(it)
                }
            }
            Column(
                Modifier
                    .fillMaxSize()
                    .overScroll(state)
            ) {
                repeat(40) { index ->
                    BasicText("row $index", Modifier.fillMaxWidth().height(100.dp))
                }
            }
        }
        composeRule.onRoot().performTouchInput {
            down(center)
            repeat(10) { moveBy(Offset(0f, 30f), delayMillis = 50) }
            up()
        }
        composeRule.waitUntil(5_000) { state.overscrollOffset == 0f }
        composeRule.runOnIdle { assertTrue(peak.floatValue > 0f) }
    }

    @Test
    fun combinedVisual_engagesAtEdge() {
        lateinit var state: OverScrollState
        val peak = mutableFloatStateOf(0f)
        composeRule.setContent {
            state = rememberOverScrollState(
                visual = OverscrollVisuals.zoom() + OverscrollVisuals.fade(),
            )
            LaunchedEffect(state) {
                snapshotFlow { state.overscrollOffset }.collect {
                    if (abs(it) > peak.floatValue) peak.floatValue = abs(it)
                }
            }
            Column(
                Modifier
                    .fillMaxSize()
                    .overScroll(state)
            ) {
                repeat(40) { index ->
                    BasicText("row $index", Modifier.fillMaxWidth().height(100.dp))
                }
            }
        }
        composeRule.onRoot().performTouchInput {
            down(center)
            repeat(10) { moveBy(Offset(0f, 30f), delayMillis = 50) }
            up()
        }
        composeRule.waitUntil(5_000) { state.overscrollOffset == 0f }
        composeRule.runOnIdle { assertTrue(peak.floatValue > 0f) }
    }

    @Test
    fun configOnlyOverload_usesPushDown_andSurvivesRecomposition() {
        lateinit var state: OverScrollState
        var recompose by mutableStateOf(0)
        composeRule.setContent {
            recompose
            state = rememberOverScrollState(
                config = OverScrollConfig(maxOverscroll = 600f, curve = OverscrollCurve.RubberBand()),
            )
            LaunchedEffect(Unit) {
                snapshotFlow { state.maxScrollValue }.first { it > 0 }
                state.scrollTo(42)
            }
            Column(
                Modifier
                    .fillMaxSize()
                    .overScroll(state)
            ) {
                repeat(40) { index ->
                    BasicText("row $index", Modifier.fillMaxWidth().height(100.dp))
                }
            }
        }
        composeRule.runOnIdle {
            assertEquals(600f, state.maxOverscroll)
            assertEquals(42, state.scrollValue)
            recompose++
        }
        composeRule.runOnIdle {
            assertEquals("state must survive recomposition", 42, state.scrollValue)
        }
    }

    @Test
    fun visualOverload_survivesRecomposition() {
        lateinit var state: OverScrollState
        var recompose by mutableStateOf(0)
        composeRule.setContent {
            recompose
            state = rememberOverScrollState(visual = OverscrollVisuals.rotate())
            LaunchedEffect(Unit) {
                snapshotFlow { state.maxScrollValue }.first { it > 0 }
                state.scrollTo(7)
            }
            Column(
                Modifier
                    .fillMaxSize()
                    .overScroll(state)
            ) {
                repeat(40) { index ->
                    BasicText("row $index", Modifier.fillMaxWidth().height(100.dp))
                }
            }
        }
        composeRule.runOnIdle { recompose++ }
        composeRule.runOnIdle { assertEquals(7, state.scrollValue) }
    }

    @Test
    fun childDsl_transformSeesValueProgressAndDirection() {
        lateinit var state: OverScrollState
        val seen = mutableSetOf<OverscrollDirection>()
        var maxProgress = 0f
        var sawNonZeroValue = false
        composeRule.setContent {
            state = rememberOverScrollState()
            Column(
                Modifier
                    .fillMaxSize()
                    .overScroll(state)
            ) {
                repeat(40) { index ->
                    BasicText(
                        "row $index",
                        Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .childOverScrollSupport(state) {
                                seen.add(direction)
                                if (progress > maxProgress) maxProgress = progress
                                if (value != 0f) sawNonZeroValue = true
                                translationY = value
                            },
                    )
                }
            }
        }
        composeRule.onRoot().performTouchInput {
            down(center)
            repeat(10) { moveBy(Offset(0f, 30f), delayMillis = 50) }
            up()
        }
        composeRule.waitUntil(5_000) { state.overscrollOffset == 0f }
        composeRule.runOnIdle {
            assertTrue("transform must observe non-zero value", sawNonZeroValue)
            assertTrue("progress must reach > 0", maxProgress > 0f)
            assertTrue("direction must include Top for top-edge drag", seen.contains(OverscrollDirection.Top))
            assertTrue(seen.contains(OverscrollDirection.None))
        }
    }

    @Test
    fun childVisual_movesChildrenWhileContainerStaysStatic() {
        lateinit var state: OverScrollState
        composeRule.setContent {
            state = rememberOverScrollState(
                config = OverScrollConfig(maxOverscroll = 500f),
            )
            Column(
                Modifier
                    .fillMaxSize()
                    .overScroll(state, containerEffect = false)
            ) {
                repeat(40) { index ->
                    BasicText(
                        "row $index",
                        Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .childOverScrollSupport(state, visual = OverscrollVisuals.pushDown()),
                    )
                }
            }
        }
        composeRule.waitForIdle()
        val yBefore = composeRule.onNodeWithText("row 0")
            .fetchSemanticsNode().positionInRoot.y
        composeRule.onRoot().performTouchInput {
            down(center)
            repeat(8) { moveBy(Offset(0f, 30f), delayMillis = 50) }
        }
        composeRule.waitForIdle()
        val yDuring = composeRule.onNodeWithText("row 0")
            .fetchSemanticsNode().positionInRoot.y
        composeRule.onRoot().performTouchInput { up() }
        composeRule.waitUntil(5_000) { state.overscrollOffset == 0f }
        assertTrue(
            "child with per-child visual must move during the drag",
            yDuring - yBefore > 100f
        )
    }

    @Test
    fun overscrollRoles_routeTaggedAnimationsPerName() {
        lateinit var state: OverScrollState
        val headerSeen = mutableListOf<Pair<Float, Float>>()
        val cardSeen = mutableListOf<Float>()
        composeRule.setContent {
            state = rememberOverScrollState(
                config = OverScrollConfig(maxOverscroll = 500f),
            )
            val roles = rememberOverScrollRoles {
                transform("header") {
                    headerSeen.add(value to progress)
                    scaleX = 1f + progress
                }
                transform("card") {
                    cardSeen.add(value)
                    translationY = value * 0.1f
                }
            }
            Column(
                Modifier
                    .fillMaxSize()
                    .overScroll(state, containerEffect = false)
            ) {
                BasicText(
                    "header",
                    Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .overscrollRole(state, roles, "header"),
                )
                repeat(6) { index ->
                    BasicText(
                        "row $index",
                        Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .overscrollRole(state, roles, "card"),
                    )
                }
                BasicText(
                    "unknown",
                    Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .overscrollRole(state, roles, "missing"),
                )
            }
        }
        composeRule.onRoot().performTouchInput {
            down(center)
            repeat(10) { moveBy(Offset(0f, 30f), delayMillis = 40) }
            up()
        }
        composeRule.waitUntil(5_000) { state.overscrollOffset == 0f }
        composeRule.runOnIdle {
            assertTrue("header role must run", headerSeen.isNotEmpty())
            assertTrue("card role must run", cardSeen.isNotEmpty())
            assertTrue(
                "header role must observe progress",
                headerSeen.any { it.second > 0f },
            )
            assertTrue(
                "card role must observe the overscroll value",
                cardSeen.any { it != 0f },
            )
        }
    }

    @Test
    fun overscrollRoles_exposeItemIndexToTransforms() {
        lateinit var state: OverScrollState
        val seenIndices = mutableSetOf<Int>()
        composeRule.setContent {
            state = rememberOverScrollState(
                config = OverScrollConfig(maxOverscroll = 500f),
            )
            val roles = rememberOverScrollRoles {
                transform("row") {
                    seenIndices.add(index)
                    translationY = value * (0.2f + index * 0.1f)
                }
            }
            Column(
                Modifier
                    .fillMaxSize()
                    .overScroll(state, containerEffect = false)
            ) {
                repeat(4) { i ->
                    BasicText(
                        "row $i",
                        Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .overscrollRole(state, roles, "row", index = i),
                    )
                }
            }
        }
        composeRule.onRoot().performTouchInput {
            down(center)
            repeat(10) { moveBy(Offset(0f, 30f), delayMillis = 40) }
            up()
        }
        composeRule.waitUntil(5_000) { state.overscrollOffset == 0f }
        composeRule.runOnIdle {
            assertTrue(
                "transform must see each item's index",
                seenIndices.containsAll(listOf(0, 1, 2, 3)),
            )
        }
    }
}
