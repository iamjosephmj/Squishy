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

/**
 * Turns a plain composable into a scroll container that produces overscroll on
 * [state]. One scrollable owns both the scrolling and the effect — the platform
 * stretch never engages and no delta leaks to parent scrollables.
 *
 * The content scrolls only when it overflows the container; fitting content
 * produces no overscroll. Requires bounded constraints along [OverScrollState.orientation]
 * (like `verticalScroll` does).
 *
 * @param state the screen's overscroll state
 * @param enabled when false, gestures are ignored without unwiring anything
 * @param containerEffect whether the container itself renders the state's
 *   visual. `false` freezes the box and leaves rendering to the items
 * @param flingBehavior any [FlingBehavior] (e.g. Flinger presets). Leftover
 *   velocity it can't consume at an edge becomes a bounce on [state]
 */
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
