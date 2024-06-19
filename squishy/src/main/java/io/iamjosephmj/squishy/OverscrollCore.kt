package io.iamjosephmj.squishy

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.OverscrollEffect
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.overscroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
abstract class BaseOverscrollEffect(
    private val scope: CoroutineScope,
    val maxOverscroll: Float = 1000f,
    val orientation: Orientation = Orientation.Vertical,
    private val animatable: Animatable<Float, out AnimationVector> = Animatable(0f),
    private val animationSpec: AnimationSpec<Float> = tween(500)
) : OverscrollEffect {

    override fun applyToScroll(
        delta: Offset,
        source: NestedScrollSource,
        performScroll: (Offset) -> Offset
    ): Offset {
        scope.launch {
            animatable.stop()
        }
        val consumed = performScroll(delta)
        val overscroll = delta - consumed
        if (overscroll != Offset.Zero) {
            scope.launch {
                val newOverscrollValue =
                    animatable.value + if (orientation == Orientation.Vertical) overscroll.y else overscroll.x
                animatable.snapTo(newOverscrollValue.coerceIn(-maxOverscroll, maxOverscroll))
            }
        }
        return consumed
    }

    override suspend fun applyToFling(
        velocity: Velocity,
        performFling: suspend (Velocity) -> Velocity
    ) {
        performFling(velocity)
        scope.launch {
            animatable.animateTo(
                0f,
                animationSpec = animationSpec
            )
        }
    }

    override val isInProgress: Boolean
        get() = animatable.value != 0f

    fun getOffsetValue(): Float {
        return animatable.value
    }

    override val effectModifier: Modifier
        get() = Modifier
}

@OptIn(ExperimentalFoundationApi::class)
fun Modifier.overScroll(
    isParentOverScrollEnabled: Boolean = true,
    overscrollEffect: BaseOverscrollEffect,
    orientation: Orientation = Orientation.Vertical,
    flingBehavior: FlingBehavior? = null
): Modifier = composed {
    val scrollStateRange = (-1f).rangeTo(1f)
    var offset by remember { mutableFloatStateOf(0f) }
    val scrollState = rememberScrollState()

    this
        .then(
            if (orientation == Orientation.Vertical) {
                this.verticalScroll(scrollState)
            } else {
                this.horizontalScroll(scrollState)
            }
        )
        .then(
            this.scrollable(
                orientation = orientation,
                state = rememberScrollableState { delta ->
                    val oldValue = offset
                    offset = (offset + delta).coerceIn(scrollStateRange)
                    offset - oldValue
                },
                overscrollEffect = object : OverscrollEffect {
                    override fun applyToScroll(
                        delta: Offset,
                        source: NestedScrollSource,
                        performScroll: (Offset) -> Offset
                    ): Offset {
                        return overscrollEffect.applyToScroll(delta, source, performScroll)
                    }

                    override suspend fun applyToFling(
                        velocity: Velocity,
                        performFling: suspend (Velocity) -> Velocity
                    ) {
                        return overscrollEffect.applyToFling(velocity, performFling)
                    }

                    override val isInProgress: Boolean
                        get() = overscrollEffect.isInProgress

                    override val effectModifier: Modifier
                        get() = if (!isParentOverScrollEnabled) Modifier else overscrollEffect.effectModifier
                },
                flingBehavior = flingBehavior
            )
        )
        .then(if (!isParentOverScrollEnabled) this else overscroll(overscrollEffect))

}

fun Modifier.childOverScrollSupport(
    overscrollEffect: BaseOverscrollEffect
): Modifier = composed {
    this
        .then(overscrollEffect.effectModifier)
}
