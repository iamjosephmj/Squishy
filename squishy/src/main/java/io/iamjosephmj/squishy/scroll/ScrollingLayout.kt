package io.iamjosephmj.squishy.scroll

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.runtime.snapshots.Snapshot
import io.iamjosephmj.squishy.state.OverScrollState
import kotlin.math.roundToInt

internal fun Modifier.scrollingLayout(state: OverScrollState): Modifier =
    layout { measurable, constraints ->
        val vertical = state.orientation == Orientation.Vertical
        val loosened = if (vertical) {
            constraints.copy(maxHeight = Constraints.Infinity)
        } else {
            constraints.copy(maxWidth = Constraints.Infinity)
        }
        val placeable = measurable.measure(loosened)
        val width = placeable.width.coerceAtMost(constraints.maxWidth)
        val height = placeable.height.coerceAtMost(constraints.maxHeight)
        val contentMain = (if (vertical) placeable.height else placeable.width).toFloat()
        val containerMain = (if (vertical) height else width).toFloat()
        state.scrollState.maxPosition = (contentMain - containerMain).coerceAtLeast(0f)
        Snapshot.withoutReadObservation {
            state.scrollState.position =
                state.scrollState.position.coerceIn(0f, state.scrollState.maxPosition)
        }
        layout(width, height) {
            val offset = -state.scrollState.position.roundToInt()
            if (vertical) {
                placeable.placeRelative(0, offset)
            } else {
                placeable.placeRelative(offset, 0)
            }
        }
    }
