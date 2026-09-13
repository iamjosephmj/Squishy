package io.iamjosephmj.squishy.scroll

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.overscroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Velocity
import io.iamjosephmj.squishy.state.OverScrollState

/**
 * The decorator half of [OverScrollArea], exposed for custom layouts: adds a
 * nested-scroll connection that feeds this node's children's edge leftovers
 * into [state] and renders the container visual. Does not add scrolling of its
 * own — pair it with any scrollable child.
 */
@OptIn(ExperimentalFoundationApi::class)
fun Modifier.overScrollConnection(
    state: OverScrollState,
    containerEffect: Boolean = true,
): Modifier = this
    .clipToBounds()
    .then(if (containerEffect) Modifier.overscroll(state.effect) else Modifier)
    .nestedScroll(
        object : NestedScrollConnection {
            /**
             * Unwinds held tension from the pre-dispatch delta, consuming
             * only the released portion so the child scrolls the remainder.
             * Without this, a recovery drag after a bounce can never reach
             * the container: the child at the edge consumes every pixel
             * scrolling back into range, and the pushed offset survives
             * until the finger lifts.
             */
            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource
            ): Offset = state.effect.releaseFrom(available)

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset = state.effect.applyToScroll(available, source) { Offset.Zero }

            override suspend fun onPostFling(
                consumed: Velocity,
                available: Velocity
            ): Velocity {
                state.effect.applyToFling(available) { Velocity.Zero }
                // Claim only this effect's axis. Returning the full vector
                // swallows every descendant's cross-axis fling leftover — a
                // horizontal row's bounce under a vertical area died here
                // before it ever reached its own effect.
                return when (state.orientation) {
                    Orientation.Vertical -> Velocity(0f, available.y)
                    Orientation.Horizontal -> Velocity(available.x, 0f)
                }
            }
        }
    )
