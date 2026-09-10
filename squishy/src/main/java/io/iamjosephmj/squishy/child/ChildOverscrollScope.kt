package io.iamjosephmj.squishy.child

import androidx.compose.ui.graphics.GraphicsLayerScope
import io.iamjosephmj.squishy.state.OverScrollState
import kotlin.math.abs

/** Which edge the current pull is engaged with. */
enum class OverscrollDirection {
    /** The leading edge — top for vertical, start for horizontal. */
    Top,
    /** The trailing edge — bottom for vertical, end for horizontal. */
    Bottom,
    /** No overscroll held. */
    None,
}

/**
 * The scope a per-item transform runs in. Extends [GraphicsLayerScope], so
 * `translationY`, `scaleX`, `alpha` and friends apply directly to the item's
 * layer — writes are frame-synced, no recomposition.
 *
 * All reads reflect the shared [io.iamjosephmj.squishy.state.OverScrollState]
 * of the container the item belongs to.
 */
interface ChildOverscrollScope : GraphicsLayerScope {
    /** Current overscroll in px, signed — positive at the leading edge. */
    val value: Float

    /** `abs(value) / maxOverscroll` clamped to 0..1 — pull depth. */
    val progress: Float

    /** Which edge the pull is on; see [OverscrollDirection]. */
    val direction: OverscrollDirection

    /** The item's position, as passed to the modifier. Enables stagger. */
    val index: Int
}

/**
 * [ChildOverscrollScope] bound to one item: delegates every layer property to
 * the live [GraphicsLayerScope] it is created inside, and derives the scope
 * reads from the shared [OverScrollState] on demand.
 */
internal class ChildOverscrollScopeImpl(
    private val state: OverScrollState,
    internal val layer: GraphicsLayerScope,
    override val index: Int = 0,
) : ChildOverscrollScope, GraphicsLayerScope by layer {

    override val value: Float
        get() = state.overscrollOffset

    override val progress: Float
        get() = if (state.maxOverscroll <= 0f) 0f
        else (abs(value) / state.maxOverscroll).coerceIn(0f, 1f)

    override val direction: OverscrollDirection
        get() = when {
            value > 0.5f -> OverscrollDirection.Top
            value < -0.5f -> OverscrollDirection.Bottom
            else -> OverscrollDirection.None
        }
}
