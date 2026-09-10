package io.iamjosephmj.squishy.visual

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import io.iamjosephmj.squishy.physics.BaseOverscrollEffect
import kotlin.math.roundToInt

/**
 * The classic effect: content follows the finger — the offset becomes a layout
 * translation along the scroll axis, revealing what is behind the edge.
 *
 * Prefer [rememberOverScrollState]; extend this only to customize the motion.
 */
class PushDownOverscrollEffect(
    orientation: Orientation,
    maxOverscroll: Float,
    animationSpec: AnimationSpec<Float>,
) : BaseOverscrollEffect(orientation, maxOverscroll, animationSpec) {

    override val effectModifier: Modifier = Modifier.offset {
        if (orientation == Orientation.Vertical) {
            IntOffset(0, value.roundToInt())
        } else {
            IntOffset(value.roundToInt(), 0)
        }
    }
}

/** Creates a remembered [PushDownOverscrollEffect]; see that class. */
@Composable
fun rememberPushDownOverscrollEffect(
    orientation: Orientation = Orientation.Vertical,
    maxOverscroll: Float = 1000f,
    animationSpec: AnimationSpec<Float> = tween(500),
): PushDownOverscrollEffect = remember(orientation, maxOverscroll, animationSpec) {
    PushDownOverscrollEffect(orientation, maxOverscroll, animationSpec)
}
