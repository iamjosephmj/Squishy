package io.iamjosephmj.squishy.physics

import kotlin.math.abs

sealed interface OverscrollCurve {
    fun apply(rawDelta: Float, current: Float, max: Float): Float

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

    class Custom(
        val map: (rawDelta: Float, current: Float, max: Float) -> Float,
    ) : OverscrollCurve {
        override fun apply(rawDelta: Float, current: Float, max: Float): Float =
            map(rawDelta, current, max)
    }
}
