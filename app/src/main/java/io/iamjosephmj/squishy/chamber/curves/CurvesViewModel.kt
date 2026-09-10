package io.iamjosephmj.squishy.chamber.curves

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import io.iamjosephmj.squishy.physics.EdgeConfig
import io.iamjosephmj.squishy.physics.OverScrollConfig
import io.iamjosephmj.squishy.physics.OverscrollCurve

/**
 * Owns the curves lab: curve selection, edge toggles, the max slider and
 * the editing/collapsed flag; derives [config] and the summary line from them.
 */
class CurvesViewModel : ViewModel() {
    var curveIndex by mutableIntStateOf(0)
        private set
    var topEnabled by mutableStateOf(true)
        private set
    var bottomEnabled by mutableStateOf(true)
        private set
    var maxOverscroll by mutableFloatStateOf(800f)
        private set
    var editing by mutableStateOf(true)
        private set

    val curveNames = listOf("Linear", "Rubber band", "Heavy band", "Half gain")

    val config: OverScrollConfig
        get() = OverScrollConfig(
            maxOverscroll = maxOverscroll,
            curve = curve,
            topEdge = EdgeConfig(enabled = topEnabled),
            bottomEdge = EdgeConfig(enabled = bottomEnabled),
        )

    val summary: String
        get() = "${curveNames[curveIndex].uppercase()} · " +
            (if (topEnabled) "TOP ON" else "TOP OFF") + " · " +
            (if (bottomEnabled) "BOTTOM ON" else "BOTTOM OFF") + " · " +
            "${maxOverscroll.toInt()} PX"

    private val curve: OverscrollCurve
        get() = when (curveIndex) {
            0 -> OverscrollCurve.Linear
            1 -> OverscrollCurve.RubberBand()
            2 -> OverscrollCurve.RubberBand(stiffness = 9f)
            else -> OverscrollCurve.Custom { rawDelta, _, _ -> rawDelta * 0.5f }
        }

    fun selectCurve(index: Int) {
        curveIndex = index
    }

    fun toggleTop() {
        topEnabled = !topEnabled
    }

    fun toggleBottom() {
        bottomEnabled = !bottomEnabled
    }

    fun onMaxChange(value: Float) {
        maxOverscroll = value
    }

    fun apply() {
        editing = false
    }

    fun edit() {
        editing = true
    }
}
