package io.iamjosephmj.squishy

import io.iamjosephmj.squishy.child.*
import io.iamjosephmj.squishy.physics.*
import io.iamjosephmj.squishy.scroll.*
import io.iamjosephmj.squishy.state.*
import io.iamjosephmj.squishy.visual.*

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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalFoundationApi::class, ExperimentalTestApi::class)
class BaseOverscrollEffectTest {

    private class TestEffect(
        orientation: Orientation = Orientation.Vertical,
        maxOverscroll: Float = 100f,
        animationSpec: AnimationSpec<Float> = tween(100),
    ) : BaseOverscrollEffect(orientation, maxOverscroll, animationSpec) {
        override val effectModifier: Modifier = Modifier
    }

    @Test
    fun applyToScroll_returnsSumOfAllConsumption() {
        val effect = TestEffect()
        val returned = effect.applyToScroll(
            delta = Offset(0f, 40f),
            source = NestedScrollSource.UserInput,
            performScroll = { Offset(0f, 10f) }
        )
        assertEquals(Offset(0f, 40f), returned)
        assertEquals(30f, effect.value)
    }

    @Test
    fun applyToScroll_clampsToMaxOverscroll_andReportsHonestConsumption() {
        val effect = TestEffect(maxOverscroll = 100f)
        effect.applyToScroll(Offset(0f, 80f), NestedScrollSource.UserInput) { Offset.Zero }
        assertEquals(80f, effect.value)
        val returned = effect.applyToScroll(Offset(0f, 50f), NestedScrollSource.UserInput) { Offset.Zero }
        assertEquals(Offset(0f, 20f), returned)
        assertEquals(100f, effect.value)
    }

    @Test
    fun applyToScroll_supportsNegativeDirection() {
        val effect = TestEffect()
        val returned = effect.applyToScroll(Offset(0f, -70f), NestedScrollSource.UserInput) { Offset.Zero }
        assertEquals(Offset(0f, -70f), returned)
        assertEquals(-70f, effect.value)
    }

    @Test
    fun applyToScroll_releasesTensionBeforePerformScroll_andCallsPerformScrollOnce() {
        val effect = TestEffect(maxOverscroll = 100f)
        effect.applyToScroll(Offset(0f, 50f), NestedScrollSource.UserInput) { Offset.Zero }
        var performScrollCalls = 0
        val returned = effect.applyToScroll(Offset(0f, -80f), NestedScrollSource.UserInput) { delta ->
            performScrollCalls++
            Offset.Zero
        }
        assertEquals(1, performScrollCalls)
        assertEquals(-30f, effect.value)
        assertEquals(Offset(0f, -80f), returned)
    }

    @Test
    fun applyToScroll_ignoresLeftoverForNonUserInputSources() {
        val effect = TestEffect()
        val returned = effect.applyToScroll(
            Offset(0f, 40f),
            NestedScrollSource.SideEffect
        ) { Offset.Zero }
        assertEquals(Offset.Zero, returned)
        assertEquals(0f, effect.value)
    }

    @Test
    fun applyToScroll_ignoresSubPixelLeftover() {
        val effect = TestEffect()
        val returned = effect.applyToScroll(Offset(0f, 0.3f), NestedScrollSource.UserInput) { Offset.Zero }
        assertEquals(Offset.Zero, returned)
        assertEquals(0f, effect.value)
    }

    @Test
    fun applyToScroll_horizontalOrientation_usesXAxis() {
        val effect = TestEffect(orientation = Orientation.Horizontal)
        val returned = effect.applyToScroll(Offset(40f, 999f), NestedScrollSource.UserInput) { Offset(40f, 999f) }
        assertEquals(Offset(40f, 999f), returned)
        assertEquals(0f, effect.value)
        val second = effect.applyToScroll(Offset(60f, 999f), NestedScrollSource.UserInput) { Offset.Zero }
        assertEquals(Offset(60f, 0f), second)
        assertEquals(60f, effect.value)
    }

    @Test
    fun dragOffsetIsSynchronous_noCoroutineAdvanceNeeded() = runTest {
        val effect = TestEffect()
        effect.applyToScroll(Offset(0f, 30f), NestedScrollSource.UserInput) { Offset(0f, 5f) }
        assertEquals(25f, effect.value)
    }

    @Test
    fun applyToFling_awaitsSettleUntilValueReachesZero() = runTest {
        withContext(TestMonotonicFrameClock(backgroundScope)) {
            val effect = TestEffect()
            effect.applyToScroll(Offset(0f, 50f), NestedScrollSource.UserInput) { Offset.Zero }
            var flingVelocitySeen: Velocity? = null
            effect.applyToFling(Velocity(0f, 1000f)) { velocity ->
                flingVelocitySeen = velocity
                velocity
            }
            assertEquals(Velocity(0f, 1000f), flingVelocitySeen)
            assertEquals(0f, effect.value)
            assertFalse(effect.isInProgress)
        }
    }

    @Test
    fun newScrollCancelsActiveSettle_andValueHolds() = runTest {
        withContext(TestMonotonicFrameClock(backgroundScope)) {
            val effect = TestEffect()
            effect.applyToScroll(Offset(0f, 100f), NestedScrollSource.UserInput) { Offset.Zero }
            val settle = launch { effect.applyToFling(Velocity.Zero) { it } }
            advanceTimeBy(50)
            runCurrent()
            val midSettleValue = effect.value
            assertTrue(midSettleValue in 1f..99f)
            effect.applyToScroll(Offset(0f, -10f), NestedScrollSource.UserInput) { Offset.Zero }
            runCurrent()
            assertTrue(settle.isCompleted)
            assertEquals(midSettleValue - 10f, effect.value)
        }
    }

    @Test
    fun applyToFling_absorbsLeftoverVelocity_thenSettlesToZero() = runTest {
        withContext(TestMonotonicFrameClock(backgroundScope)) {
            val effect = TestEffect()
            val fling = launch {
                effect.applyToFling(Velocity(0f, 2000f)) { Velocity.Zero }
            }
            advanceTimeBy(100)
            runCurrent()
            assertTrue(
                "leftover fling velocity should extend the offset",
                effect.value > 0f
            )
            assertTrue(effect.value <= 100f)
            fling.join()
            assertEquals(0f, effect.value)
        }
    }

    @Test
    fun applyToFling_smallLeftoverVelocity_doesNotExtend() = runTest {
        withContext(TestMonotonicFrameClock(backgroundScope)) {
            val effect = TestEffect()
            effect.applyToFling(Velocity(0f, 30f)) { Velocity.Zero }
            assertEquals(0f, effect.value)
            assertFalse(effect.isInProgress)
        }
    }

    @Test
    fun applyToFling_strongLeftoverVelocity_clampsAtMaxOverscroll() = runTest {
        withContext(TestMonotonicFrameClock(backgroundScope)) {
            val effect = TestEffect()
            val fling = launch {
                effect.applyToFling(Velocity(0f, 100_000f)) { Velocity.Zero }
            }
            advanceTimeBy(150)
            runCurrent()
            assertTrue(effect.value in 80f..100f)
            fling.join()
            assertEquals(0f, effect.value)
        }
    }
}
