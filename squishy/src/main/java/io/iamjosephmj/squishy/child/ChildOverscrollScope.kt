package io.iamjosephmj.squishy.child

import androidx.compose.ui.graphics.GraphicsLayerScope
import io.iamjosephmj.squishy.state.OverScrollState
import kotlin.math.abs

enum class OverscrollDirection {
    Top,
    Bottom,
    None,
}

interface ChildOverscrollScope : GraphicsLayerScope {
    val value: Float
    val progress: Float
    val direction: OverscrollDirection
    val index: Int
}

internal class ChildOverscrollScopeImpl(
    private val state: OverScrollState,
    layer: GraphicsLayerScope,
    override val index: Int = 0,
) : ChildOverscrollScope, GraphicsLayerScope by layer {

    override val value: Float
        get() = state.overscrollOffset

    override val progress: Float
        get() = if (state.maxOverscroll <= 0f) 0f
        else (abs(value) / state.maxOverscroll).coerceIn(0f, 1f)

    override val direction: OverscrollDirection
        get() = when {
            value > 0.5f -> OverscrollDirection.Top
            value < -0.5f -> OverscrollDirection.Bottom
            else -> OverscrollDirection.None
        }
}
