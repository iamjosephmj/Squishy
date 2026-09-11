package io.iamjosephmj.squishy.child

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.GraphicsLayerScope
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
 * This is a plain modifier factory: any input change (including [key],
 * [index], [visual] or a new [transform] identity) rebuilds the modifier
 * chain on the caller's recomposition and re-materializes here — no
 * composition scope is created per item.
 *
 * @param visual plugin visual applied to this item (e.g. `OverscrollVisuals.tilt()`)
 * @param key call-site identity for the item; participates in chain rebuilds
 *   when the item's identity changes
 * @param index the item's position, exposed to [transform] as
 *   [ChildOverscrollScope.index] — one registered transform can stagger
 * @param transform layer writes in the [ChildOverscrollScope]
 */
fun Modifier.childOverScrollSupport(
    state: OverScrollState,
    visual: OverscrollVisual? = null,
    key: Any? = null,
    index: Int = 0,
    transform: ChildOverscrollScope.() -> Unit = {},
): Modifier {
    val runner = ChildTransformRunner(state, index, transform)
    return this
        .then(
            visual?.visual(
                { state.overscrollOffset },
                state.maxOverscroll,
                state.orientation,
            ) ?: Modifier
        )
        .graphicsLayer { runner.runIn(this) }
}

/**
 * Binds one [ChildOverscrollScopeImpl] to the live `GraphicsLayerScope` a
 * layer block runs in. The scope is rebuilt only when the layer instance or
 * the item's index changes — per-frame reads stay allocation-free.
 */
internal class ChildTransformRunner(
    private val state: OverScrollState,
    private val index: Int,
    private val transform: ChildOverscrollScope.() -> Unit,
) {
    private var scope: ChildOverscrollScopeImpl? = null

    fun runIn(layer: GraphicsLayerScope) {
        val cached = scope
        val impl = if (cached != null && cached.layer === layer && cached.index == index) {
            cached
        } else {
            ChildOverscrollScopeImpl(state, layer, index).also { scope = it }
        }
        impl.transform()
    }
}
