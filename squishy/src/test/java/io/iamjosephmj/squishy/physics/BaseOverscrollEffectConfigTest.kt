package io.iamjosephmj.squishy.physics


import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.TestMonotonicFrameClock
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalFoundationApi::class, ExperimentalTestApi::class)
class BaseOverscrollEffectConfigTest {

    private class TestEffect(
        orientation: Orientation = Orientation.Vertical,
        maxOverscroll: Float = 100f,
        animationSpec: AnimationSpec<Float> = tween(100),
        curve: OverscrollCurve = OverscrollCurve.Linear,
        topEdge: EdgeConfig = EdgeConfig(),
        bottomEdge: EdgeConfig = EdgeConfig(),
        absorbVelocityFactor: Float = 0.06f,
        absorbDurationMillis: Int = 120,
        minAbsorbVelocity: Float = 50f,
    ) : BaseOverscrollEffect(
        orientation,
        maxOverscroll,
        animationSpec,
        curve,
        topEdge,
        bottomEdge,
        absorbVelocityFactor,
        absorbDurationMillis,
        minAbsorbVelocity,
    ) {
        override val effectModifier: Modifier = Modifier
    }

    @Test
    fun engine_appliesCustomCurveToDrags() {
        val effect = TestEffect(
            curve = OverscrollCurve.Custom { rawDelta, _, _ -> rawDelta / 2f },
        )
        val returned = effect.applyToScroll(
            Offset(0f, 40f),
            NestedScrollSource.UserInput
        ) { Offset.Zero }
        assertEquals(20f, returned.y)
        assertEquals(20f, effect.value)
    }

    @Test
    fun engine_rubberBandDragMovesLessThanRawDelta_atDepth() {
        val effect = TestEffect(
            maxOverscroll = 1000f,
            curve = OverscrollCurve.RubberBand(stiffness = 3f),
        )
        effect.applyToScroll(Offset(0f, 900f), NestedScrollSource.UserInput) { Offset.Zero }
        assertEquals(900f, effect.value)
        effect.applyToScroll(Offset(0f, 100f), NestedScrollSource.UserInput) { Offset.Zero }
        assertTrue("offset must advance by less than raw delta at depth", effect.value < 1000f)
        assertTrue(effect.value > 900f)
    }

    @Test
    fun engine_absorbHonorsConfiguredVelocityFactor() = runTest {
        withContext(TestMonotonicFrameClock(backgroundScope)) {
            val effect = TestEffect(
                maxOverscroll = 1000f,
                absorbVelocityFactor = 0.5f,
                absorbDurationMillis = 60,
            )
            val fling = launch {
                effect.applyToFling(Velocity(0f, 4000f)) { Velocity.Zero }
            }
            advanceTimeBy(80)
            runCurrent()
            assertTrue(
                "peak must reflect configured factor (4000*0.5=2000 -> clamped 1000)",
                effect.value in 900f..1000f
            )
            fling.join()
            assertEquals(0f, effect.value)
        }
    }

    @Test
    fun engine_absorbHonorsConfiguredMinVelocity() = runTest {
        withContext(TestMonotonicFrameClock(backgroundScope)) {
            val effect = TestEffect(minAbsorbVelocity = 5000f)
            effect.applyToFling(Velocity(0f, 2000f)) { Velocity.Zero }
            assertEquals(0f, effect.value)
        }
    }

    @Test
    fun disabledTopEdge_leftoverUnconsumed_noOffset_bottomStillWorks() {
        val effect = TestEffect(topEdge = EdgeConfig(enabled = false))
        val returned = effect.applyToScroll(Offset(0f, 40f), NestedScrollSource.UserInput) {
            Offset.Zero
        }
        assertEquals(Offset.Zero, returned)
        assertEquals(0f, effect.value)
        val downward = effect.applyToScroll(Offset(0f, -40f), NestedScrollSource.UserInput) {
            Offset.Zero
        }
        assertEquals(Offset(0f, -40f), downward)
        assertEquals(-40f, effect.value)
    }

    @Test
    fun perEdgeMaxOverscroll_clampsIndependently() {
        val effect = TestEffect(
            maxOverscroll = 100f,
            topEdge = EdgeConfig(enabled = true, maxOverscroll = 50f),
        )
        effect.applyToScroll(Offset(0f, 80f), NestedScrollSource.UserInput) { Offset.Zero }
        assertEquals(50f, effect.value)
        val returned = effect.applyToScroll(Offset(0f, 30f), NestedScrollSource.UserInput) {
            Offset.Zero
        }
        assertEquals(Offset.Zero, returned)
        assertEquals(50f, effect.value)
        effect.applyToScroll(Offset(0f, -200f), NestedScrollSource.UserInput) { Offset.Zero }
        assertEquals(-100f, effect.value)
    }

    @Test
    fun disabledEdge_skipsAbsorb() = runTest {
        withContext(TestMonotonicFrameClock(backgroundScope)) {
            val effect = TestEffect(topEdge = EdgeConfig(enabled = false))
            effect.applyToFling(Velocity(0f, 4000f)) { Velocity.Zero }
            assertEquals(0f, effect.value)
        }
    }

    @Test
    fun disabledEdge_tensionReleaseStillConsumes() {
        val effect = TestEffect(bottomEdge = EdgeConfig(enabled = false))
        effect.applyToScroll(Offset(0f, 60f), NestedScrollSource.UserInput) { Offset.Zero }
        assertEquals(60f, effect.value)
        val returned = effect.applyToScroll(Offset(0f, -40f), NestedScrollSource.UserInput) {
            Offset.Zero
        }
        assertEquals(Offset(0f, -40f), returned)
        assertEquals(20f, effect.value)
    }
}
