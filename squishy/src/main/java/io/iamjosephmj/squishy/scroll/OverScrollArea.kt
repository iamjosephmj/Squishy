package io.iamjosephmj.squishy.scroll

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import io.iamjosephmj.squishy.state.OverScrollState

/**
 * Brings overscroll to scrollables you don't own — `LazyColumn`, `LazyRow`,
 * grids, pagers, anything on the nested-scroll channel. The area listens for
 * their edge leftovers, feeds them to [state], and suppresses the platform
 * stretch effect inside it.
 *
 * The child keeps its own engine (including its [androidx.compose.foundation.gestures.FlingBehavior]);
 * leftover fling velocity at an edge arrives on [state] as a bounce.
 *
 * @param state the screen's overscroll state
 * @param containerEffect whether the area renders the state's visual itself
 * @param content the scrollable, and anything else that should share the effect
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OverScrollArea(
    state: OverScrollState,
    modifier: Modifier = Modifier,
    containerEffect: Boolean = true,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalOverscrollConfiguration provides null) {
        Box(modifier = modifier.overScrollConnection(state, containerEffect)) {
            content()
        }
    }
}
