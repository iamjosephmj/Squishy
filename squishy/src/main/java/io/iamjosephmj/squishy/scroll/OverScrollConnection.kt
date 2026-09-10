package io.iamjosephmj.squishy.scroll

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.overscroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Velocity
import io.iamjosephmj.squishy.state.OverScrollState

@OptIn(ExperimentalFoundationApi::class)
fun Modifier.overScrollConnection(
    state: OverScrollState,
    containerEffect: Boolean = true,
): Modifier = composed {
    this
        .clipToBounds()
        .then(if (containerEffect) Modifier.overscroll(state.effect) else Modifier)
        .nestedScroll(
            object : NestedScrollConnection {
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
                    return available
                }
            }
        )
}
