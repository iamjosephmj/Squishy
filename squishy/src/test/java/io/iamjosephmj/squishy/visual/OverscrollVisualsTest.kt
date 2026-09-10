package io.iamjosephmj.squishy.visual


import androidx.compose.foundation.gestures.Orientation
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class OverscrollVisualsTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun progressOf_clampsToBounds_andGuardsZeroBounds() {
        assertEquals(0.5f, progressOf(value = 500f, bounds = 1000f))
        assertEquals(1f, progressOf(value = 5000f, bounds = 1000f))
        assertEquals(0f, progressOf(value = 500f, bounds = 0f))
    }

    @Test
    fun everyBuiltIn_producesNonIdentityModifier() {
        lateinit var visuals: List<OverscrollVisual>
        composeRule.setContent {
            visuals = listOf(
                OverscrollVisuals.pushDown(),
                OverscrollVisuals.zoom(),
                OverscrollVisuals.rotate(),
                OverscrollVisuals.skew(),
                OverscrollVisuals.tilt(),
                OverscrollVisuals.blur(),
                OverscrollVisuals.fade(),
            )
        }
        composeRule.runOnIdle {
            visuals.forEach { visual ->
                val produced = visual.visual({ 250f }, 1000f, Orientation.Vertical)
                assertNotEquals(Modifier, produced)
            }
        }
    }

    @Test
    fun plus_combinesVisuals_withoutLosingEither() {
        lateinit var combined: OverscrollVisual
        lateinit var zoom: OverscrollVisual
        composeRule.setContent {
            zoom = OverscrollVisuals.zoom()
            combined = zoom + OverscrollVisuals.fade()
        }
        composeRule.runOnIdle {
            val produced = combined.visual({ 250f }, 1000f, Orientation.Vertical)
            assertNotEquals(Modifier, produced)
            assertNotEquals(
                zoom.visual({ 250f }, 1000f, Orientation.Vertical),
                produced
            )
        }
    }
}
