package io.iamjosephmj.squishy

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.CoroutineScope
import kotlin.math.roundToInt


class PushDownOverscrollEffect(
    scope: CoroutineScope,
    maxOverscroll: Float,
    orientation: Orientation,
    animatable: Animatable<Float, out AnimationVector>,
    animationSpec: AnimationSpec<Float>
) : BaseOverscrollEffect(
    scope = scope,
    maxOverscroll = maxOverscroll,
    orientation = orientation,
    animatable = animatable,
    animationSpec = animationSpec
) {
    override val effectModifier: Modifier
        get() = if (orientation == Orientation.Vertical) {
            super.effectModifier
                .offset { IntOffset(0, getOffsetValue().roundToInt()) }
        } else {
            super.effectModifier.offset { IntOffset(getOffsetValue().roundToInt(), 0) }
        }
}

@Composable
fun rememberPushDownOverscrollEffect(
    maxOverscroll: Float = 1000f,
    orientation: Orientation = Orientation.Vertical,
    animationSpec: AnimationSpec<Float> = tween(500),
    animatable: Animatable<Float, out AnimationVector> = Animatable(0f),
): BaseOverscrollEffect {
    val scope = rememberCoroutineScope()
    return remember {
        PushDownOverscrollEffect(
            scope,
            maxOverscroll,
            orientation,
            animatable,
            animationSpec
        )
    }
}