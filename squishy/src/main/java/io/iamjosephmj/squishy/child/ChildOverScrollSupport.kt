package io.iamjosephmj.squishy.child

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import io.iamjosephmj.squishy.state.OverScrollState
import io.iamjosephmj.squishy.visual.OverscrollVisual

/**
 * The item inherits the container's visual — whatever the state's effect
 * renders (push-down offset, zoom, ...), applied to this item's layer.
 * Pair with `overScroll(state, containerEffect = false)` so the box holds
 * still while the items carry the effect.
 */
fun Modifier.childOverScrollSupport(
    state: OverScrollState,
): Modifier = this.then(state.effect.effectModifier)

/**
 * Per-item overscroll: an optional plugin [visual] plus an inline [transform],
 * both driven by the container's [state].
 *
 * Runs inside the item's graphics layer — reads of `value`/`progress` are
 * frame-synced and never recompose. Let the visual drive the item's response;
 * add a [transform] only for effects the visual doesn't already provide.
 *
 * @param visual plugin visual applied to this item (e.g. `OverscrollVisuals.tilt()`)
 * @param key invalidates the modifier when the item's identity changes;
 *   the [visual], [index] and [transform] are tracked automatically, so
 *   swapping them re-applies the modifier even across skippable callers
 * @param index the item's position, exposed to [transform] as
 *   [ChildOverscrollScope.index] — one registered transform can stagger
 * @param transform layer writes in the [ChildOverscrollScope]
 */
@OptIn(ExperimentalComposeUiApi::class)
fun Modifier.childOverScrollSupport(
    state: OverScrollState,
    visual: OverscrollVisual? = null,
    key: Any? = null,
    index: Int = 0,
    transform: ChildOverscrollScope.() -> Unit = {},
): Modifier = composed(
    fullyQualifiedName = "io.iamjosephmj.squishy.childOverScrollSupport",
    key, visual, index, transform,
) {
    var scope: ChildOverscrollScopeImpl? = null
    this
        .then(
            visual?.visual(
                { state.overscrollOffset },
                state.maxOverscroll,
                state.orientation,
            ) ?: Modifier
        )
        .graphicsLayer {
            val cached = scope
            val impl = if (cached != null && cached.layer === this) {
                cached
            } else {
                ChildOverscrollScopeImpl(state, this, index).also { scope = it }
            }
            impl.transform()
        }
}
