package io.iamjosephmj.squishy.physics

import kotlin.math.abs

/**
 * Maps a raw drag delta onto a new overscroll offset, defining how the pull
 * *resists* as it deepens. Applied only to drag leftovers; the release path
 * (moving back toward zero) always consumes linearly.
 *
 * Implementations must be pure functions — they run on every drag frame.
 */
sealed interface OverscrollCurve {

    /**
     * Applies [rawDelta] to [current] given the effective limit [max].
     *
     * @param rawDelta the leftover drag delta in px, signed along the scroll axis
     * @param current the current overscroll offset in px
     * @param max the effective maximum for the edge being pulled (always >= 0)
     * @return the new offset; callers clamp the result to `[-max, max]`
     */
    fun apply(rawDelta: Float, current: Float, max: Float): Float

    /**
     * No resistance: every pixel of finger travel converts to a pixel of
     * offset until the limit is reached.
     */
    object Linear : OverscrollCurve {
        override fun apply(rawDelta: Float, current: Float, max: Float): Float = current + rawDelta
    }

    data class RubberBand(val stiffness: Float = 3f) : OverscrollCurve {
        override fun apply(rawDelta: Float, current: Float, max: Float): Float {
            if (max <= 0f || rawDelta == 0f) return current
            val sameSide = current * rawDelta > 0f || current == 0f
            if (!sameSide) return current + rawDelta
            val depth = abs(current) / max
            val gain = 1f / (1f + stiffness * depth)
            val increment = (abs(rawDelta) * gain).coerceAtMost(max - abs(current))
            return current + kotlin.math.sign(rawDelta) * increment
        }
    }

    /**
     * Your own mapping. The lambda runs every drag frame — keep it cheap and
     * side-effect free.
     *
     * If the mapping is monotonic the release path stays exact; amplifying
     * curves can report consuming more than the raw delta, which will starve
     * parent scrollables — prefer gain curves in `[0, 1]`.
     *
     * @param map same contract as [apply]
     */
    class Custom(
        val map: (rawDelta: Float, current: Float, max: Float) -> Float,
    ) : OverscrollCurve {
        override fun apply(rawDelta: Float, current: Float, max: Float): Float =
            map(rawDelta, current, max)
    }
}
