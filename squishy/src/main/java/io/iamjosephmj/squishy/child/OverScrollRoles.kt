package io.iamjosephmj.squishy.child

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.iamjosephmj.squishy.state.OverScrollState
import io.iamjosephmj.squishy.visual.OverscrollVisual

class OverScrollRoles {
    internal val transforms = mutableMapOf<String, ChildOverscrollScope.() -> Unit>()
    internal val visuals = mutableMapOf<String, OverscrollVisual>()

    fun transform(name: String, block: ChildOverscrollScope.() -> Unit) {
        transforms[name] = block
    }

    fun visual(name: String, visual: OverscrollVisual) {
        visuals[name] = visual
    }
}

@Composable
fun rememberOverScrollRoles(builder: OverScrollRoles.() -> Unit): OverScrollRoles =
    remember { OverScrollRoles().apply(builder) }

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
                    ChildOverscrollScopeImpl(state, this, index).transform()
                }
            } else {
                Modifier
            }
        )
}
