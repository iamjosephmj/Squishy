package io.iamjosephmj.squishy.scroll

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.overscroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalLayoutDirection
import io.iamjosephmj.squishy.state.OverScrollState

@OptIn(ExperimentalFoundationApi::class)
fun Modifier.overScroll(
    state: OverScrollState,
    enabled: Boolean = true,
    containerEffect: Boolean = true,
    flingBehavior: FlingBehavior? = null,
): Modifier = composed {
    this
        .clipToBounds()
        .then(if (containerEffect) Modifier.overscroll(state.effect) else Modifier)
        .scrollable(
            state = state.scrollState,
            orientation = state.orientation,
            enabled = enabled,
            reverseDirection = ScrollableDefaults.reverseDirection(
                LocalLayoutDirection.current,
                state.orientation,
                false
            ),
            flingBehavior = flingBehavior ?: ScrollableDefaults.flingBehavior(),
            overscrollEffect = state.effect,
        )
        .scrollingLayout(state)
}
