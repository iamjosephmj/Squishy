package io.iamjosephmj.squishy

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.test.swipeWithVelocity
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
class OverScrollIntegrationTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun dragPastInnerEdge_overscrolls_outerParentDoesNotScroll() {
        lateinit var state: OverScrollState
        val parentScroll = mutableIntStateOf(0)
        val peakOverscroll = mutableFloatStateOf(0f)
        composeRule.setContent {
            state = rememberOverScrollState()
            val parentState = rememberScrollState()
            SideEffect { parentScroll.intValue = parentState.value }
            LaunchedEffect(state) {
                snapshotFlow { state.overscrollOffset }.collect {
                    if (abs(it) > peakOverscroll.floatValue) peakOverscroll.floatValue = abs(it)
                }
            }
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(parentState)
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .height(2000.dp)
                        .overScroll(state)
                ) {
                    repeat(80) { index ->
                        BasicText(
                            text = "row $index",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        )
                    }
                }
            }
        }

        composeRule.onRoot().performTouchInput {
            down(center)
            repeat(10) {
                moveBy(Offset(0f, 30f), delayMillis = 50)
            }
            up()
        }
        composeRule.waitUntil(5_000) { state.overscrollOffset == 0f }
        composeRule.runOnIdle {
            assertTrue(
                "squish should have engaged at the edge",
                peakOverscroll.floatValue > 0f
            )
            assertEquals(
                "overscroll delta must not leak to the parent scrollable",
                0,
                parentScroll.intValue
            )
        }
    }

    @Test
    fun lazyColumn_insideOverScrollArea_overscrollsAtEdge() {
        lateinit var state: OverScrollState
        val peakOverscroll = mutableFloatStateOf(0f)
        composeRule.setContent {
            state = rememberOverScrollState()
            LaunchedEffect(state) {
                snapshotFlow { state.overscrollOffset }.collect {
                    if (abs(it) > peakOverscroll.floatValue) peakOverscroll.floatValue = abs(it)
                }
            }
            OverScrollArea(state, Modifier.fillMaxSize()) {
                LazyColumn(Modifier.fillMaxSize()) {
                    items(count = 20) { index ->
                        BasicText(
                            text = "lazy $index",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
                    }
                }
            }
        }

        composeRule.onRoot().performTouchInput { swipeUp() }
        composeRule.onRoot().performTouchInput { swipeUp() }
        composeRule.waitForIdle()
        composeRule.onRoot().performTouchInput {
            down(center)
            repeat(10) {
                moveBy(Offset(0f, -30f), delayMillis = 50)
            }
            up()
        }
        composeRule.waitUntil(5_000) { state.overscrollOffset == 0f }
        composeRule.runOnIdle {
            assertTrue(
                "LazyColumn edge drag should feed the overscroll effect",
                peakOverscroll.floatValue > 0f
            )
        }
    }

    @Test
    fun rememberOverScrollState_survivesRecomposition_withoutResettingScroll() {
        lateinit var state: OverScrollState
        var recompose by mutableStateOf(0)
        composeRule.setContent {
            recompose
            state = rememberOverScrollState()
            Column(Modifier.fillMaxSize().overScroll(state)) {
                repeat(40) { index ->
                    BasicText("row $index", Modifier.fillMaxWidth().height(100.dp))
                }
            }
            LaunchedEffect(Unit) {
                snapshotFlow { state.maxScrollValue }.first { it > 0 }
                state.scrollTo(42)
            }
        }
        composeRule.runOnIdle {
            assertEquals(42, state.scrollValue)
            recompose++
        }
        composeRule.runOnIdle {
            assertEquals("state must survive recomposition", 42, state.scrollValue)
        }
    }

    @Test
    fun overScroll_scrollsContent_thenSquishesAtEdge() {
        lateinit var state: OverScrollState
        val peakOverscroll = mutableFloatStateOf(0f)
        var maxScrolled = 0
        composeRule.setContent {
            state = rememberOverScrollState()
            LaunchedEffect(state) {
                snapshotFlow { state.overscrollOffset }.collect {
                    if (abs(it) > peakOverscroll.floatValue) peakOverscroll.floatValue = abs(it)
                }
            }
            LaunchedEffect(state) {
                snapshotFlow { state.scrollValue }.collect { if (it > maxScrolled) maxScrolled = it }
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
        repeat(4) {
            composeRule.onRoot().performTouchInput { swipeUp() }
            composeRule.waitForIdle()
        }
        composeRule.onRoot().performTouchInput {
            down(center)
            repeat(10) {
                moveBy(Offset(0f, -30f), delayMillis = 50)
            }
            up()
        }
        composeRule.waitUntil(5_000) { state.overscrollOffset == 0f }
        composeRule.runOnIdle {
            assertTrue("content must scroll inside overScroll", maxScrolled > 0)
            assertTrue("edge drag must squish", peakOverscroll.floatValue > 0f)
        }
    }

    @Test
    fun flingIntoEdge_triggersOverscrollAutomatically() {
        lateinit var state: OverScrollState
        val peakOverscroll = mutableFloatStateOf(0f)
        composeRule.setContent {
            state = rememberOverScrollState()
            LaunchedEffect(state) {
                snapshotFlow { state.overscrollOffset }.collect {
                    if (abs(it) > peakOverscroll.floatValue) peakOverscroll.floatValue = abs(it)
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
        repeat(4) {
            composeRule.onRoot().performTouchInput { swipeUp() }
            composeRule.waitForIdle()
        }
        composeRule.waitUntil(5_000) { state.overscrollOffset == 0f }
        composeRule.runOnIdle { peakOverscroll.floatValue = 0f }
        composeRule.onRoot().performTouchInput {
            swipeWithVelocity(
                start = center,
                end = center - Offset(0f, 300f),
                endVelocity = 12000f,
                durationMillis = 40
            )
        }
        composeRule.waitUntil(5_000) { state.overscrollOffset == 0f }
        composeRule.runOnIdle {
            assertTrue(
                "fling into the edge must trigger the overscroll automatically",
                peakOverscroll.floatValue > 500f
            )
        }
    }
}
