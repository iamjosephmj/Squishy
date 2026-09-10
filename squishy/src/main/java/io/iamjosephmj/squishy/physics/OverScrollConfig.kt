package io.iamjosephmj.squishy.physics

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.Orientation

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
