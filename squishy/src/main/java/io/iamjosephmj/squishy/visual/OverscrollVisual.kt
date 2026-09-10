package io.iamjosephmj.squishy.visual

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.ui.Modifier
import kotlin.math.abs

fun interface OverscrollVisual {
    fun visual(value: () -> Float, bounds: Float, orientation: Orientation): Modifier
}

operator fun OverscrollVisual.plus(other: OverscrollVisual): OverscrollVisual =
    CombinedOverscrollVisual(this, other)

internal fun progressOf(value: Float, bounds: Float): Float =
    if (bounds <= 0f) 0f else (abs(value) / bounds).coerceIn(0f, 1f)

private class CombinedOverscrollVisual(
    private val first: OverscrollVisual,
    private val second: OverscrollVisual,
) : OverscrollVisual {
    override fun visual(value: () -> Float, bounds: Float, orientation: Orientation): Modifier =
        first.visual(value, bounds, orientation)
            .then(second.visual(value, bounds, orientation))
}
