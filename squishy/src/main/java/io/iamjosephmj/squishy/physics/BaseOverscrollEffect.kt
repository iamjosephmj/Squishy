package io.iamjosephmj.squishy.physics

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.OverscrollEffect
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.Job
import kotlin.coroutines.coroutineContext
import kotlin.math.abs

@OptIn(ExperimentalFoundationApi::class)
abstract class BaseOverscrollEffect(
    val orientation: Orientation,
    val maxOverscroll: Float,
    val animationSpec: AnimationSpec<Float>,
    internal val curve: OverscrollCurve = OverscrollCurve.Linear,
    internal val topEdge: EdgeConfig = EdgeConfig(),
    internal val bottomEdge: EdgeConfig = EdgeConfig(),
    internal val absorbVelocityFactor: Float = 0.06f,
    internal val absorbDurationMillis: Int = 120,
    internal val minAbsorbVelocity: Float = 50f,
) : OverscrollEffect {

    private val offsetState = mutableFloatStateOf(0f)
    private val settleAnimatable = Animatable(0f)
    private var settleJob: Job? = null

    val value: Float
        get() = offsetState.floatValue

    override val isInProgress: Boolean
        get() = value != 0f

    protected fun axisValue(delta: Offset): Float =
        if (orientation == Orientation.Vertical) delta.y else delta.x

    protected fun axisOffset(axisDelta: Float): Offset =
        if (orientation == Orientation.Vertical) Offset(0f, axisDelta) else Offset(axisDelta, 0f)

    internal fun topMax(): Float = if (topEdge.enabled) topEdge.effectiveMax(maxOverscroll) else 0f

    internal fun bottomMax(): Float =
        if (bottomEdge.enabled) bottomEdge.effectiveMax(maxOverscroll) else 0f

    override fun applyToScroll(
        delta: Offset,
        source: NestedScrollSource,
        performScroll: (Offset) -> Offset
    ): Offset {
        settleJob?.cancel()
        val axisDelta = axisValue(delta)

        val release = when {
            value > 0f -> axisDelta.coerceIn(-value, 0f)
            value < 0f -> axisDelta.coerceIn(0f, -value)
            else -> 0f
        }
        if (release != 0f) offsetState.floatValue = value + release

        val remainder = axisDelta - release
        val consumedByScrollOffset = performScroll(axisOffset(remainder))
        val consumedByScroll = axisValue(consumedByScrollOffset)
        val offAxisConsumed = consumedByScrollOffset - axisOffset(consumedByScroll)

        val leftover = remainder - consumedByScroll
        val absorbed = if (source == NestedScrollSource.UserInput && abs(leftover) > 0.5f) {
            val upward = leftover > 0f
            val edge = if (upward) topEdge else bottomEdge
            if (edge.enabled) {
                val positiveMax = topMax()
                val negativeMax = bottomMax()
                val target = curve.apply(leftover, value, edge.effectiveMax(maxOverscroll))
                    .coerceIn(-negativeMax, positiveMax)
                val applied = target - value
                offsetState.floatValue = target
                applied
            } else {
                0f
            }
        } else {
            0f
        }

        return axisOffset(release + consumedByScroll + absorbed) + offAxisConsumed
    }

    override suspend fun applyToFling(
        velocity: Velocity,
        performFling: suspend (Velocity) -> Velocity
    ) {
        settleJob?.cancel()
        val consumed = performFling(velocity)
        val leftoverVelocity = velocityAxisValue(velocity) - velocityAxisValue(consumed)
        val job = requireNotNull(coroutineContext[Job]) {
            "applyToFling must be called from a coroutine scope"
        }
        settleJob = job
        try {
            if (abs(leftoverVelocity) > minAbsorbVelocity) {
                val upward = leftoverVelocity > 0f
                val edge = if (upward) topEdge else bottomEdge
                if (edge.enabled) {
                    val peak = (value + leftoverVelocity * absorbVelocityFactor)
                        .coerceIn(-bottomMax(), topMax())
                    if (peak != value) {
                        settleAnimatable.snapTo(value)
                        settleAnimatable.animateTo(peak, tween(absorbDurationMillis)) {
                            offsetState.floatValue = value
                        }
                    }
                }
            }
            settleAnimatable.snapTo(value)
            settleAnimatable.animateTo(0f, animationSpec) {
                offsetState.floatValue = value
            }
        } finally {
            if (settleJob === job) settleJob = null
        }
    }

    private fun velocityAxisValue(velocity: Velocity): Float =
        if (orientation == Orientation.Vertical) velocity.y else velocity.x

    abstract override val effectModifier: Modifier
}
