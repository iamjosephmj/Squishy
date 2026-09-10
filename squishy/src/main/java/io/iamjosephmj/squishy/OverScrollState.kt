package io.iamjosephmj.squishy

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.MutatorMutex
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.overscroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Modifier
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Velocity
import kotlin.math.abs
import kotlin.math.roundToInt

@Stable
class OverScrollState internal constructor(
    internal val effect: BaseOverscrollEffect,
    internal val scrollState: SquishyScrollState,
) {
    val orientation: Orientation
        get() = effect.orientation

    val overscrollOffset: Float
        get() = effect.value

    val isOverscrolling: Boolean
        get() = effect.isInProgress

    val scrollValue: Int
        get() = scrollState.position.roundToInt()

    val maxScrollValue: Int
        get() = scrollState.maxPosition.roundToInt()

    val maxOverscroll: Float
        get() = effect.maxOverscroll

    suspend fun scrollTo(value: Int) = scrollState.scrollTo(value.toFloat())

    suspend fun animateScrollTo(value: Int) = scrollState.animateScrollTo(value.toFloat())
}

internal class SquishyScrollState : ScrollableState {
    private val mutex = MutatorMutex()
    private val inProgressState = mutableStateOf(false)

    var position by mutableFloatStateOf(0f)
        internal set
    var maxPosition by mutableFloatStateOf(0f)

    override val isScrollInProgress: Boolean
        get() = inProgressState.value
    override val canScrollForward: Boolean
        get() = position < maxPosition
    override val canScrollBackward: Boolean
        get() = position > 0f

    override fun dispatchRawDelta(delta: Float): Float {
        val previous = position
        position = (position + delta).coerceIn(0f, maxPosition)
        return position - previous
    }

    private val scrollScope = object : ScrollScope {
        override fun scrollBy(pixels: Float): Float {
            val previous = position
            position = (position + pixels).coerceIn(0f, maxPosition)
            return position - previous
        }
    }

    override suspend fun scroll(
        scrollPriority: MutatePriority,
        block: suspend ScrollScope.() -> Unit
    ): Unit = mutex.mutate(scrollPriority) {
        inProgressState.value = true
        try {
            block(scrollScope)
        } finally {
            inProgressState.value = false
        }
    }

    suspend fun scrollTo(value: Float) = scroll { scrollBy(value - position) }

    suspend fun animateScrollTo(
        value: Float,
        animationSpec: AnimationSpec<Float> = spring()
    ) {
        scroll {
            var lastValue = position
            animate(
                initialValue = position,
                targetValue = value.coerceIn(0f, maxPosition),
                animationSpec = animationSpec
            ) { v, _ ->
                scrollBy(v - lastValue)
                lastValue = v
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
internal class ConfiguredOverscrollEffect(
    orientation: Orientation,
    config: OverScrollConfig,
    visual: OverscrollVisual,
) : BaseOverscrollEffect(
    orientation = orientation,
    maxOverscroll = config.maxOverscroll,
    animationSpec = config.settleSpec,
    curve = config.curve,
    topEdge = config.topEdge,
    bottomEdge = config.bottomEdge,
    absorbVelocityFactor = config.absorbVelocityFactor,
    absorbDurationMillis = config.absorbDurationMillis,
    minAbsorbVelocity = config.minAbsorbVelocity,
) {
    override val effectModifier: Modifier =
        visual.visual({ value }, maxOverscroll, orientation)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun rememberOverScrollState(
    orientation: Orientation = Orientation.Vertical,
    maxOverscroll: Float = 1000f,
    animationSpec: AnimationSpec<Float> = tween(500),
): OverScrollState {
    val effect = remember(orientation, maxOverscroll, animationSpec) {
        PushDownOverscrollEffect(orientation, maxOverscroll, animationSpec)
    }
    return remember(effect) { OverScrollState(effect, SquishyScrollState()) }
}

@Composable
fun rememberOverScrollState(
    effect: BaseOverscrollEffect,
): OverScrollState = remember(effect) {
    OverScrollState(effect, SquishyScrollState())
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun rememberOverScrollState(
    config: OverScrollConfig,
): OverScrollState = rememberOverScrollState(OverscrollVisuals.pushDown(), config)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun rememberOverScrollState(
    visual: OverscrollVisual,
    config: OverScrollConfig = OverScrollConfig(),
): OverScrollState {
    val effect = remember(visual, config) {
        ConfiguredOverscrollEffect(config.orientation, config, visual)
    }
    return remember(effect) { OverScrollState(effect, SquishyScrollState()) }
}

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

private fun Modifier.scrollingLayout(state: OverScrollState): Modifier =
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

fun Modifier.childOverScrollSupport(
    state: OverScrollState,
): Modifier = this.then(state.effect.effectModifier)

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

private class ChildOverscrollScopeImpl(
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
