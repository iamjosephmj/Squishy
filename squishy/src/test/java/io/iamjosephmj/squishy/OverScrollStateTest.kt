package io.iamjosephmj.squishy

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.Modifier
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalFoundationApi::class)
class OverScrollStateTest {

    @Test
    fun pushDownEffect_startsAtZero() {
        val effect = PushDownOverscrollEffect(Orientation.Vertical, 100f, tween(10))
        assertEquals(0f, effect.value)
        assertFalse(effect.isInProgress)
        assertNotEquals(Modifier, effect.effectModifier)
    }

    @Test
    fun state_exposesEffectValue_andDelegatesScroll() = runTest {
        val effect = PushDownOverscrollEffect(Orientation.Vertical, 100f, tween(10))
        val state = OverScrollState(effect, SquishyScrollState())
        assertEquals(Orientation.Vertical, state.orientation)
        assertEquals(0f, state.overscrollOffset)
        assertEquals(0, state.scrollValue)
        assertEquals(0, state.maxScrollValue)
        state.scrollTo(10)
        assertEquals("unmeasured state coerces scrolls to zero", 0, state.scrollValue)
        effect.applyToScroll(Offset(0f, 30f), NestedScrollSource.UserInput) { Offset.Zero }
        assertEquals(30f, state.overscrollOffset)
        assertTrue(state.isOverscrolling)
    }

    @Test
    fun squishyScrollState_scrollsWithinMeasuredBounds() = runTest {
        val scrollState = SquishyScrollState()
        assertFalse(scrollState.canScrollForward)
        assertFalse(scrollState.canScrollBackward)
        scrollState.maxPosition = 100f
        assertTrue(scrollState.canScrollForward)
        scrollState.scrollTo(40f)
        assertEquals(40f, scrollState.position)
        scrollState.scrollTo(120f)
        assertEquals(100f, scrollState.position)
        assertFalse(scrollState.canScrollForward)
        assertTrue(scrollState.canScrollBackward)
        scrollState.dispatchRawDelta(-50f)
        assertEquals(50f, scrollState.position)
        assertEquals(-50f, scrollState.dispatchRawDelta(-80f))
        assertEquals(0f, scrollState.position)
        assertFalse(scrollState.isScrollInProgress)
    }
}
