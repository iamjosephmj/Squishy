package io.iamjosephmj.squishy.child

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.iamjosephmj.squishy.state.OverScrollState
import io.iamjosephmj.squishy.visual.OverscrollVisual

/**
 * A named registry of per-item animations — the `setTag()` pattern. Register
 * behaviors once per screen, then tag items with [Modifier.overscrollRole].
 * Items sharing a name share the animation; each still receives its own
 * [ChildOverscrollScope] with its own [ChildOverscrollScope.index].
 */
class OverScrollRoles {
    internal val transforms = mutableMapOf<String, ChildOverscrollScope.() -> Unit>()
    internal val visuals = mutableMapOf<String, OverscrollVisual>()

    /**
     * Registers a layer transform under [name], replacing any previous one.
     *
     * @param block runs inside each tagged item's graphics layer
     */
    fun transform(name: String, block: ChildOverscrollScope.() -> Unit) {
        transforms[name] = block
    }

    /** Registers a plugin [visual] under [name] for items tagged with it. */
    fun visual(name: String, visual: OverscrollVisual) {
        visuals[name] = visual
    }
}

/**
 * Remembers an [OverScrollRoles] registry built by [builder]. The builder runs
 * once; registering later updates are fine (the lookup happens per item).
 */
@Composable
fun rememberOverScrollRoles(builder: OverScrollRoles.() -> Unit): OverScrollRoles =
    remember { OverScrollRoles().apply(builder) }

/**
 * Tags this item with a role [name] — the item runs whatever `transform` and
 * `visual` the registry holds under that name, driven by [state]. Unknown
 * names render nothing, so dynamic lists can't crash on stale tags.
 *
 * @param roles the screen's registry; see [rememberOverScrollRoles]
 * @param name the role to look up
 * @param index the item's position, surfaced to the transform for stagger
 */
@OptIn(ExperimentalComposeUiApi::class)
fun Modifier.overscrollRole(
    state: OverScrollState,
    roles: OverScrollRoles,
    name: String,
    index: Int = 0,
): Modifier = composed(
    fullyQualifiedName = "io.iamjosephmj.squishy.overscrollRole",
    key1 = name to index,
) {
    val transform = roles.transforms[name]
    val visual = roles.visuals[name]
    var scope: ChildOverscrollScopeImpl? = null
    this
        .then(
            visual?.visual(
                { state.overscrollOffset },
                state.maxOverscroll,
                state.orientation,
            ) ?: Modifier
        )
        .then(
            if (transform != null) {
                Modifier.graphicsLayer {
                    val cached = scope
                    val impl = if (cached != null && cached.layer === this) {
                        cached
                    } else {
                        ChildOverscrollScopeImpl(state, this, index).also { scope = it }
                    }
                    impl.transform()
                }
            } else {
                Modifier
            }
        )
}
