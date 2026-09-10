package io.iamjosephmj.squishy.state

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.runtime.Stable
import io.iamjosephmj.squishy.physics.BaseOverscrollEffect
import io.iamjosephmj.squishy.scroll.SquishyScrollState
import kotlin.math.roundToInt

/**
 * The single source of truth for one scrolling screen: the overscroll offset
 * plus the content scroll position. Create one per screen with
 * [rememberOverScrollState] and hand it to `Modifier.overScroll` or
 * `OverScrollArea`.
 *
 * All reads are snapshot state — safe to read from `graphicsLayer`/`offset`
 * lambdas for frame-synced animation without recomposition.
 */
@Stable
class OverScrollState internal constructor(
    internal val effect: BaseOverscrollEffect,
    internal val scrollState: SquishyScrollState,
) {
    /** The axis this state drives. */
    val orientation: Orientation
        get() = effect.orientation

    /** Current overscroll in px — positive at the leading (top) edge, negative at the trailing. */
    val overscrollOffset: Float
        get() = effect.value

    /** True while an overscroll is held or settling. */
    val isOverscrolling: Boolean
        get() = effect.isInProgress

    /** Content scroll position in px. Only meaningful once the container has been measured. */
    val scrollValue: Int
        get() = scrollState.position.roundToInt()

    /** Content scroll range in px (content minus viewport). Zero while unmeasured or non-scrolling. */
    val maxScrollValue: Int
        get() = scrollState.maxPosition.roundToInt()

    /** The configured default offset limit in px (per-edge limits may differ). */
    val maxOverscroll: Float
        get() = effect.maxOverscroll

    /**
     * Jumps the content scroll to [value] (px) without animation, through the
     * same pipeline gestures use.
     */
    suspend fun scrollTo(value: Int) = scrollState.scrollTo(value.toFloat())

    /**
     * Animates the content scroll to [value] (px). Uses a spring by default;
     * any in-flight overscroll settles normally.
     */
    suspend fun animateScrollTo(value: Int) = scrollState.animateScrollTo(value.toFloat())
}
