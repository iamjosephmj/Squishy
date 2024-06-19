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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.CoroutineScope
import kotlin.math.roundToInt

class FadeOverscrollEffect(
    scope: CoroutineScope,
    maxOverscroll: Float = 1000f,
    orientation: Orientation = Orientation.Vertical
) : BaseOverscrollEffect(scope, maxOverscroll, orientation) {
    override val effectModifier: Modifier
        get() = Modifier.graphicsLayer {
            alpha = 1f - (getOffsetValue() / maxOverscroll).coerceIn(0f, 1f)
        }
}

@Composable
fun rememberFadeOverscrollEffect(
    maxOverscroll: Float = 1000f,
    orientation: Orientation = Orientation.Vertical
): FadeOverscrollEffect {
    val scope = rememberCoroutineScope()
    return remember { FadeOverscrollEffect(scope, maxOverscroll, orientation) }
}

class ScaleOverscrollEffect(
    scope: CoroutineScope,
    maxOverscroll: Float = 1000f,
    orientation: Orientation = Orientation.Vertical
) : BaseOverscrollEffect(scope, maxOverscroll, orientation) {
    override val effectModifier: Modifier
        get() = Modifier.graphicsLayer {
            scaleX = 1f + (getOffsetValue() / maxOverscroll).coerceIn(0f, 1f)
            scaleY = 1f + (getOffsetValue() / maxOverscroll).coerceIn(0f, 1f)
        }
}


@Composable
fun rememberScaleOverscrollEffect(
    maxOverscroll: Float = 1000f,
    orientation: Orientation = Orientation.Vertical
): BaseOverscrollEffect {
    val scope = rememberCoroutineScope()
    return remember { ScaleOverscrollEffect(scope, maxOverscroll, orientation) }
}

class PushDownOverscrollEffect(
    scope: CoroutineScope,
    maxOverscroll: Float = 1000f,
    orientation: Orientation = Orientation.Vertical,
    animatable: Animatable<Float, out AnimationVector> = Animatable(0f),
    animationSpec: AnimationSpec<Float> = tween(500)
) : BaseOverscrollEffect(
    scope,
    maxOverscroll,
    orientation,
    animatable,
    animationSpec
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