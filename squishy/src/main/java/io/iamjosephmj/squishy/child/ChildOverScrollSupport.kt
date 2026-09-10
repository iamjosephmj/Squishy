package io.iamjosephmj.squishy.child

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import io.iamjosephmj.squishy.state.OverScrollState
import io.iamjosephmj.squishy.visual.OverscrollVisual

fun Modifier.childOverScrollSupport(
    state: OverScrollState,
): Modifier = this.then(state.effect.effectModifier)

@OptIn(ExperimentalComposeUiApi::class)
fun Modifier.childOverScrollSupport(
    state: OverScrollState,
    visual: OverscrollVisual? = null,
    key: Any? = null,
    index: Int = 0,
    transform: ChildOverscrollScope.() -> Unit = {},
): Modifier = composed(
    fullyQualifiedName = "io.iamjosephmj.squishy.childOverScrollSupport",
    key1 = key,
) {
    this
        .then(
            visual?.visual(
                { state.overscrollOffset },
                state.maxOverscroll,
                state.orientation,
            ) ?: Modifier
        )
        .graphicsLayer {
            ChildOverscrollScopeImpl(state, this, index).transform()
        }
}
