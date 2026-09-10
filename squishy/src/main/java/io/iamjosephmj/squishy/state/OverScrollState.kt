package io.iamjosephmj.squishy.state

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.runtime.Stable
import io.iamjosephmj.squishy.physics.BaseOverscrollEffect
import io.iamjosephmj.squishy.scroll.SquishyScrollState
import kotlin.math.roundToInt

@Stable
class OverScrollState internal constructor(
    internal val effect: BaseOverscrollEffect,
    internal val scrollState: SquishyScrollState,
) {
    val orientation: Orientation
        get() = effect.orientation

    val overscrollOffset: Float
        get() = effect.value

    val isOverscrolling: Boolean
        get() = effect.isInProgress

    val scrollValue: Int
        get() = scrollState.position.roundToInt()

    val maxScrollValue: Int
        get() = scrollState.maxPosition.roundToInt()

    val maxOverscroll: Float
        get() = effect.maxOverscroll

    suspend fun scrollTo(value: Int) = scrollState.scrollTo(value.toFloat())

    suspend fun animateScrollTo(value: Int) = scrollState.animateScrollTo(value.toFloat())
}
