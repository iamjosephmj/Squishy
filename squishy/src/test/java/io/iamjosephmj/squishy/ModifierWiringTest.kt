package io.iamjosephmj.squishy

import io.iamjosephmj.squishy.child.*
import io.iamjosephmj.squishy.physics.*
import io.iamjosephmj.squishy.scroll.*
import io.iamjosephmj.squishy.state.*
import io.iamjosephmj.squishy.visual.*

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.Alignment
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
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

/**
 * Pins the modifier wiring contracts — gating, orientation, fling plumbing,
 * area modes, role retargeting and mid-gesture plugin swaps — so the
 * Modifier.Node migration cannot silently change them.
 */
@OptIn(ExperimentalFoundationApi::class)
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w360dp-h640dp-420dpi")
class ModifierWiringTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun overScroll_enabledFalse_ignoresDrag() {
        lateinit var state: OverScrollState
        composeRule.setContent {
            state = rememberOverScrollState()
            Column(
                Modifier
                    .fillMaxSize()
                    .overScroll(state, enabled = false)
            ) {
                repeat(40) { index ->
                    BasicText("row $index", Modifier.fillMaxWidth().height(100.dp))
                }
            }
        }
        composeRule.onRoot().performTouchInput {
            down(center)
            repeat(8) { moveBy(Offset(0f, 30f), delayMillis = 40) }
        }
        composeRule.waitForIdle()
        composeRule.runOnIdle {
            assertEquals("disabled state must not overscroll", 0f, state.overscrollOffset)
            assertEquals("disabled state must not scroll", 0, state.scrollValue)
        }
        composeRule.onRoot().performTouchInput { up() }
    }

    @Test
    fun overScroll_horizontalOrientation_overscrollsAlongX() {
        lateinit var state: OverScrollState
        val peak = mutableFloatStateOf(0f)
        composeRule.setContent {
            state = rememberOverScrollState(orientation = Orientation.Horizontal)
            LaunchedPeak(state) { v -> if (v > peak.floatValue) peak.floatValue = v }
            Row(
                Modifier
                    .fillMaxSize()
                    .overScroll(state)
            ) {
                repeat(20) { index ->
                    BasicText("col $index", Modifier.width(200.dp))
                }
            }
        }
        composeRule.onRoot().performTouchInput {
            down(center)
            repeat(8) { moveBy(Offset(30f, 0f), delayMillis = 40) }
        }
        composeRule.waitForIdle()
        composeRule.runOnIdle {
            assertTrue("horizontal drag at the start edge must overscroll", peak.floatValue > 0f)
            assertTrue("state must report the horizontal axis", state.orientation == Orientation.Horizontal)
        }
        composeRule.onRoot().performTouchInput { up() }
        composeRule.waitUntil(5_000) { state.overscrollOffset == 0f }
    }

    @Test
    fun overScroll_customFlingBehavior_receivesFlingVelocity() {
        lateinit var state: OverScrollState
        var fling: RecordingFlingBehavior? = null
        val peak = mutableFloatStateOf(0f)
        composeRule.setContent {
            state = rememberOverScrollState()
            LaunchedPeak(state) { v -> if (v > peak.floatValue) peak.floatValue = v }
            val defaultFling = ScrollableDefaults.flingBehavior()
            val recording = remember(defaultFling) { RecordingFlingBehavior(defaultFling) }
            fling = recording
            LaunchedEdge(state)
            Column(
                Modifier
                    .fillMaxSize()
                    .overScroll(state, flingBehavior = recording)
            ) {
                repeat(40) { index ->
                    BasicText("row $index", Modifier.fillMaxWidth().height(100.dp))
                }
            }
        }
        composeRule.waitUntil(5_000) { state.maxScrollValue > 0 && state.scrollValue == state.maxScrollValue }
        composeRule.onRoot().performTouchInput {
            swipeWithVelocity(
                start = center,
                end = center - Offset(0f, 300f),
                endVelocity = 12_000f,
                durationMillis = 40,
            )
        }
        composeRule.waitUntil(5_000) { state.overscrollOffset == 0f }
        composeRule.runOnIdle {
            assertTrue(
                "fling momentum must flow through the custom FlingBehavior",
                fling!!.calls.isNotEmpty(),
            )
            assertTrue("unconsumed fling velocity must become a bounce", peak.floatValue > 0f)
        }
    }

    @Test
    fun overScrollArea_containerEffectFalse_stateStillReceivesLeftovers() {
        lateinit var state: OverScrollState
        val peak = mutableFloatStateOf(0f)
        composeRule.setContent {
            state = rememberOverScrollState(config = OverScrollConfig(maxOverscroll = 500f))
            LaunchedPeak(state) { v -> if (v > peak.floatValue) peak.floatValue = v }
            OverScrollArea(state, Modifier.fillMaxSize(), containerEffect = false) {
                LazyColumn(Modifier.fillMaxSize()) {
                    items(count = 20) { index ->
                        BasicText("lazy $index", Modifier.fillMaxWidth().height(200.dp))
                    }
                }
            }
        }
        composeRule.onRoot().performTouchInput {
            down(center)
            repeat(10) { moveBy(Offset(0f, 30f), delayMillis = 40) }
        }
        composeRule.waitForIdle()
        composeRule.runOnIdle {
            assertTrue(
                "area must feed edge leftovers to the state even when frozen",
                peak.floatValue > 0f,
            )
        }
        composeRule.onRoot().performTouchInput { up() }
        composeRule.waitUntil(5_000) { state.overscrollOffset == 0f }
    }

    @Test
    fun overscrollRole_nameChange_retargetsItemToNewRole() {
        lateinit var state: OverScrollState
        var selected by mutableIntStateOf(0)
        val roleA = CountingTransform()
        val roleB = CountingTransform()
        composeRule.setContent {
            state = rememberOverScrollState(config = OverScrollConfig(maxOverscroll = 500f))
            val roles = rememberOverScrollRoles {
                transform("a") { roleA.run(this) }
                transform("b") { roleB.run(this) }
            }
            Column(
                Modifier
                    .fillMaxSize()
                    .overScroll(state, containerEffect = false)
            ) {
                BasicText(
                    "item",
                    Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .overscrollRole(state, roles, if (selected == 0) "a" else "b"),
                )
            }
        }
        composeRule.onRoot().performTouchInput {
            down(center)
            repeat(6) { moveBy(Offset(0f, 30f), delayMillis = 40) }
        }
        composeRule.waitForIdle()
        composeRule.runOnIdle {
            assertTrue("initial role must run", roleA.count > 0)
            assertEquals(0, roleB.count)
        }
        composeRule.onRoot().performTouchInput { up() }
        composeRule.waitUntil(5_000) { state.overscrollOffset == 0f }
        composeRule.runOnIdle { selected = 1 }
        composeRule.onRoot().performTouchInput {
            down(center)
            repeat(6) { moveBy(Offset(0f, 30f), delayMillis = 40) }
        }
        composeRule.waitForIdle()
        composeRule.runOnIdle {
            assertTrue("retargeted role must take over", roleB.count > 0)
        }
        composeRule.onRoot().performTouchInput { up() }
        composeRule.waitUntil(5_000) { state.overscrollOffset == 0f }
    }

    @Test
    fun overscrollRoles_insideOverScrollAreaLazyColumn_allDepthsMove() {
        lateinit var state: OverScrollState
        val headerSeen = mutableListOf<Float>()
        val sectionSeen = mutableListOf<Float>()
        val rowSeen = mutableListOf<Float>()
        composeRule.setContent {
            state = rememberOverScrollState(
                config = OverScrollConfig(maxOverscroll = 520f, curve = OverscrollCurve.RubberBand()),
            )
            val roles = rememberOverScrollRoles {
                transform("header") { headerSeen.add(value) }
                transform("section") { sectionSeen.add(value) }
                transform("row") { rowSeen.add(value) }
            }
            OverScrollArea(state, Modifier.fillMaxSize(), containerEffect = false) {
                LazyColumn(Modifier.fillMaxSize()) {
                    item {
                        BasicText(
                            "header",
                            Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .overscrollRole(state, roles, "header"),
                        )
                    }
                    item {
                        BasicText(
                            "section",
                            Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .overscrollRole(state, roles, "section"),
                        )
                    }
                    items(count = 12) { index ->
                        BasicText(
                            "row $index",
                            Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .overscrollRole(state, roles, "row", index = index),
                        )
                    }
                }
            }
        }
        composeRule.onRoot().performTouchInput {
            down(center)
            repeat(10) { moveBy(Offset(0f, 30f), delayMillis = 40) }
        }
        composeRule.waitForIdle()
        composeRule.runOnIdle {
            assertTrue("header role must run inside a lazy area", headerSeen.any { it != 0f })
            assertTrue("section role must run inside a lazy area", sectionSeen.any { it != 0f })
            assertTrue("row role must run inside a lazy area", rowSeen.any { it != 0f })
        }
        composeRule.onRoot().performTouchInput { up() }
        composeRule.waitUntil(5_000) { state.overscrollOffset == 0f }
    }

    @Test
    fun homeLayout_everyTaggedElementMovesDuringPull() {
        lateinit var state: OverScrollState
        composeRule.setContent {
            state = rememberOverScrollState(
                config = OverScrollConfig(maxOverscroll = 520f, curve = OverscrollCurve.RubberBand()),
            )
            val roles = rememberOverScrollRoles {
                transform("header") { translationY = value * 0.10f }
                transform("section") { translationY = value * 0.16f }
                transform("row") { translationY = value * (0.22f + index * 0.012f) }
            }
            OverScrollArea(state, Modifier.fillMaxSize(), containerEffect = false) {
                LazyColumn(
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 36.dp, bottom = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.overscrollRole(state, roles, "header"),
                        ) {
                            BasicText("Squishy", Modifier.fillMaxWidth().height(40.dp))
                        }
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                    item {
                        BasicText(
                            "OVERSCROLL TOOLKIT",
                            Modifier
                                .fillMaxWidth()
                                .height(24.dp)
                                .overscrollRole(state, roles, "section"),
                        )
                    }
                    item {
                        Column(modifier = Modifier.overscrollRole(state, roles, "header")) {
                            BasicText("Overscroll,", Modifier.fillMaxWidth().height(40.dp))
                            BasicText("reimagined.", Modifier.fillMaxWidth().height(40.dp))
                        }
                    }
                    item {
                        BasicText(
                            "A plugin system for Jetpack Compose overscroll.",
                            Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .overscrollRole(state, roles, "section"),
                        )
                    }
                    repeat(6) { position ->
                        item(key = "demo$position") {
                            BasicText(
                                "card $position",
                                Modifier
                                    .fillMaxWidth()
                                    .height(90.dp)
                                    .overscrollRole(state, roles, "row", index = position),
                            )
                        }
                    }
                }
            }
        }
        composeRule.waitForIdle()
        fun topOf(text: String) = composeRule.onNodeWithText(text, substring = true)
            .fetchSemanticsNode().positionInRoot.y

        val wordmarkRest = topOf("Squishy")
        val heroRest = topOf("reimagined.")
        val paragraphRest = topOf("A plugin system")
        val cardRest = topOf("card 0")

        composeRule.onRoot().performTouchInput {
            down(center)
            repeat(10) { moveBy(Offset(0f, 30f), delayMillis = 40) }
        }
        composeRule.waitForIdle()
        composeRule.runOnIdle {
            assertTrue("pull must be holding overscroll", state.overscrollOffset > 0f)
            val wordmarkDelta = topOf("Squishy") - wordmarkRest
            val heroDelta = topOf("reimagined.") - heroRest
            val paragraphDelta = topOf("A plugin system") - paragraphRest
            val cardDelta = topOf("card 0") - cardRest
            assertTrue("wordmark (header) must move", wordmarkDelta > 0f)
            assertTrue(
                "hero subtitle must move with the pull (delta=$heroDelta)",
                heroDelta > 0f,
            )
            assertTrue(
                "hero must move like the wordmark at header depth " +
                    "(hero=$heroDelta wordmark=$wordmarkDelta)",
                abs(heroDelta - wordmarkDelta) < 1f,
            )
            assertTrue(
                "section copy must trail at mid depth (delta=$paragraphDelta)",
                paragraphDelta > heroDelta,
            )
            assertTrue(
                "cards must fan out ahead of the hero (delta=$cardDelta)",
                cardDelta > paragraphDelta,
            )
        }
        composeRule.onRoot().performTouchInput { up() }
        composeRule.waitUntil(5_000) { state.overscrollOffset == 0f }
    }

    @Test
    fun childOverScrollSupport_visualSwapMidGesture_rematerializes() {
        lateinit var state: OverScrollState
        var selected by mutableIntStateOf(0)
        val first = CountingVisual(0)
        val second = CountingVisual(1)
        composeRule.setContent {
            state = rememberOverScrollState(config = OverScrollConfig(maxOverscroll = 800f))
            Column(
                Modifier
                    .fillMaxSize()
                    .overScroll(state)
            ) {
                BasicText(
                    "item",
                    Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .childOverScrollSupport(
                            state,
                            visual = if (selected == 0) first else second,
                        ),
                )
                repeat(30) { index ->
                    BasicText("row $index", Modifier.fillMaxWidth().height(100.dp))
                }
            }
        }
        composeRule.onRoot().performTouchInput {
            down(center)
            repeat(8) { moveBy(Offset(0f, 30f), delayMillis = 40) }
        }
        composeRule.waitForIdle()
        composeRule.runOnIdle {
            assertTrue("must be holding overscroll mid-gesture", state.overscrollOffset != 0f)
            assertTrue("initial visual must be materialized", first.appliedIds.contains(0))
            selected = 1
        }
        composeRule.waitForIdle()
        composeRule.runOnIdle {
            assertTrue(
                "swapped visual must materialize while the gesture holds",
                second.appliedIds.contains(1),
            )
        }
        composeRule.onRoot().performTouchInput { up() }
        composeRule.waitUntil(5_000) { state.overscrollOffset == 0f }
    }
}

/** Tracks the peak of a flow of floats collected from [state]'s overscroll. */
@Composable
private fun LaunchedPeak(state: OverScrollState, onUpdate: (Float) -> Unit) {
    androidx.compose.runtime.LaunchedEffect(state) {
        snapshotFlow { state.overscrollOffset }.collect { onUpdate(abs(it)) }
    }
}

/** Scrolls the container to its trailing edge as soon as it is measurable. */
@Composable
private fun LaunchedEdge(state: OverScrollState) {
    androidx.compose.runtime.LaunchedEffect(state) {
        snapshotFlow { state.maxScrollValue }.first { it > 0 }
        state.scrollTo(state.maxScrollValue)
    }
}

/** Counts how many times its layer transform ran. */
private class CountingTransform {
    var count = 0
        private set

    fun run(scope: ChildOverscrollScope) {
        count++
        scope.translationY = scope.value * 0.2f
    }
}

/** Records every time the visual's modifier materializes. */
private class CountingVisual(private val id: Int) : OverscrollVisual {
    val appliedIds = mutableListOf<Int>()

    override fun visual(value: () -> Float, bounds: Float, orientation: Orientation): Modifier {
        appliedIds.add(id)
        return Modifier
    }
}

/** Records every fling it is handed, then behaves exactly like the platform default. */
private class RecordingFlingBehavior(private val delegate: FlingBehavior) : FlingBehavior {
    val calls = mutableListOf<Float>()

    override suspend fun ScrollScope.performFling(initialVelocity: Float): Float {
        calls.add(initialVelocity)
        return with(delegate) { performFling(initialVelocity) }
    }
}
