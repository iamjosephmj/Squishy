package io.iamjosephmj.squishy.visual

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.ui.Modifier
import kotlin.math.abs

/**
 * What the overscroll offset *looks like*: a pure function from the pull to a
 * [Modifier]. There are no contracts to implement and no way to break the
 * scroll pipeline — the engine owns all motion, the visual only renders it.
 *
 * The returned modifier must read the offset lazily (inside `graphicsLayer`,
 * `offset` or draw-phase lambdas) so the effect stays frame-synced instead of
 * recomposing. Built-ins live in [OverscrollVisuals].
 */
fun interface OverscrollVisual {

    /**
     * Builds the modifier for this visual.
     *
     * @param value deferred read of the current offset in px — call it inside
     *   lambda-based modifiers only
     * @param bounds the effective offset limit in px; handy for normalizing
     *   (`value() / bounds` gives a 0..1 progress)
     * @param orientation the scroll axis, for axis-aware transforms
     */
    fun visual(value: () -> Float, bounds: Float, orientation: Orientation): Modifier
}

/**
 * Stacks two visuals: both render on the same pull, composed left to right.
 * `zoom() + fade()` both shrinks and dims at the edge.
 */
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
