package io.iamjosephmj.squishy

import io.iamjosephmj.squishy.child.*
import io.iamjosephmj.squishy.physics.*
import io.iamjosephmj.squishy.scroll.*
import io.iamjosephmj.squishy.state.*
import io.iamjosephmj.squishy.visual.*

import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.Orientation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class OverScrollConfigTest {

    @Test
    fun linearCurve_appliesDeltaDirectly() {
        val curve = OverscrollCurve.Linear
        assertEquals(140f, curve.apply(rawDelta = 40f, current = 100f, max = 1000f))
        assertEquals(60f, curve.apply(rawDelta = -40f, current = 100f, max = 1000f))
    }

    @Test
    fun rubberBand_fullGainAtCenter() {
        val curve = OverscrollCurve.RubberBand()
        assertEquals(100f, curve.apply(rawDelta = 100f, current = 0f, max = 1000f), 0.001f)
    }

    @Test
    fun rubberBand_dampensWithDepth() {
        val curve = OverscrollCurve.RubberBand(stiffness = 3f)
        val from900 = curve.apply(rawDelta = 100f, current = 900f, max = 1000f)
        assertTrue("deep drag must gain less than full delta", from900 - 900f < 100f)
        assertTrue("deep drag must still move", from900 > 900f)
        val gain = (from900 - 900f) / 100f
        assertEquals(1f / (1f + 3f * 0.9f), gain, 0.001f)
    }

    @Test
    fun rubberBand_releaseAndCrossing_stayOneToOne() {
        val curve = OverscrollCurve.RubberBand(stiffness = 3f)
        assertEquals(800f, curve.apply(rawDelta = -100f, current = 900f, max = 1000f), 0.001f)
        assertEquals(-100f, curve.apply(rawDelta = -100f, current = 0f, max = 1000f), 0.001f)
        assertEquals(-138.46f, curve.apply(rawDelta = -50f, current = -100f, max = 1000f), 0.01f)
    }

    @Test
    fun rubberBand_monotonicAndBounded() {
        val curve = OverscrollCurve.RubberBand()
        var offset = 0f
        var previous = 0f
        repeat(200) {
            offset = curve.apply(rawDelta = 50f, current = offset, max = 1000f)
            assertTrue(offset >= previous)
            assertTrue(offset <= 1000f)
            previous = offset
        }
    }

    @Test
    fun rubberBand_zeroMax_isNoOp() {
        val curve = OverscrollCurve.RubberBand()
        assertEquals(0f, curve.apply(rawDelta = 40f, current = 0f, max = 0f), 0.001f)
    }

    @Test
    fun customCurve_usesProvidedLambda() {
        val curve = OverscrollCurve.Custom { rawDelta, current, _ -> current + rawDelta / 2f }
        assertEquals(20f, curve.apply(rawDelta = 40f, current = 0f, max = 1000f))
    }

    @Test
    fun config_defaultsReproduceV2Constants() {
        val config = OverScrollConfig()
        assertEquals(Orientation.Vertical, config.orientation)
        assertEquals(1000f, config.maxOverscroll)
        assertEquals(OverscrollCurve.Linear, config.curve)
        assertEquals(0.06f, config.absorbVelocityFactor)
        assertEquals(120, config.absorbDurationMillis)
        assertEquals(50f, config.minAbsorbVelocity)
        assertTrue(config.topEdge.enabled)
        assertTrue(config.bottomEdge.enabled)
        assertEquals(null, config.topEdge.maxOverscroll)
    }

    @Test
    fun edgeConfig_effectiveMax() {
        assertEquals(400f, EdgeConfig(enabled = true, maxOverscroll = 400f).effectiveMax(1000f))
        assertEquals(1000f, EdgeConfig(enabled = true, maxOverscroll = null).effectiveMax(1000f))
    }
}
