package io.iamjosephmj.squishy

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.Orientation
import kotlin.math.abs
import kotlin.math.sign

data class OverScrollConfig(
    val orientation: Orientation = Orientation.Vertical,
    val maxOverscroll: Float = 1000f,
    val curve: OverscrollCurve = OverscrollCurve.Linear,
    val settleSpec: AnimationSpec<Float> = tween(500),
    val absorbVelocityFactor: Float = 0.06f,
    val absorbDurationMillis: Int = 120,
    val minAbsorbVelocity: Float = 50f,
    val topEdge: EdgeConfig = EdgeConfig(),
    val bottomEdge: EdgeConfig = EdgeConfig(),
)

data class EdgeConfig(
    val enabled: Boolean = true,
    val maxOverscroll: Float? = null,
) {
    internal fun effectiveMax(fallback: Float): Float = maxOverscroll ?: fallback
}

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
            return current + sign(rawDelta) * increment
        }
    }

    class Custom(
        val map: (rawDelta: Float, current: Float, max: Float) -> Float,
    ) : OverscrollCurve {
        override fun apply(rawDelta: Float, current: Float, max: Float): Float =
            map(rawDelta, current, max)
    }
}
