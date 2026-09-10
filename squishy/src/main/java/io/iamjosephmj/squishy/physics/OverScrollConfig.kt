package io.iamjosephmj.squishy.physics

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.Orientation

/**
 * How an overscroll behaves: its orientation, its resistance curve, the settle
 * animation, the fling-to-bounce conversion, and per-edge overrides.
 *
 * Value equality matters — a remembered state is keyed on the config instance,
 * so changing any field recreates the state (and resets any in-flight offset).
 *
 * @param orientation the scroll axis this state drives.
 * @param maxOverscroll the offset limit in px when the edge does not override it.
 * @param curve drag resistance mapping; see [OverscrollCurve].
 * @param settleSpec animation used to spring the offset back to zero.
 * @param absorbVelocityFactor how much leftover fling velocity converts into a
 *   bounce peak: `peak = offset + velocity * factor`, clamped to the edge limit.
 * @param absorbDurationMillis duration of the rise to the bounce peak.
 * @param minAbsorbVelocity leftover velocity below this (px/s) is ignored.
 * @param topEdge behavior of the leading edge (top for vertical).
 * @param bottomEdge behavior of the trailing edge (bottom for vertical).
 */
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
