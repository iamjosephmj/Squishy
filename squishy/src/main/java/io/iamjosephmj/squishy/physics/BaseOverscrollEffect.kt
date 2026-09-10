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

/**
 * The overscroll engine. Owns every nested-scroll contract — consumption
 * accounting, tension release, drag gating, fling absorption, and the awaited
 * settle — and exposes the current offset as [value].
 *
 * Subclassing is the full-custom escape hatch; most effects only need an
 * [io.iamjosephmj.squishy.visual.OverscrollVisual] passed to
 * `rememberOverScrollState(visual, config)`. Extend this directly when you need
 * state beyond the offset (custom drawing loops, gesture side effects).
 *
 * The offset is backed by snapshot state: reads from `graphicsLayer`/`offset`
 * lambdas are frame-synced and never recompose. [applyToScroll] is fully
 * synchronous — no coroutine dispatch between input and [value] changing.
 *
 * @param orientation the axis this effect works on.
 * @param maxOverscroll default offset limit in px before per-edge overrides.
 * @param animationSpec the settle animation back to zero (see
 *   [OverScrollConfig.settleSpec]).
 */
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

    /** The current overscroll offset in px — positive at the leading edge. */
    val value: Float
        get() = offsetState.floatValue

    /** True while an offset is held or settling back to zero. */
    override val isInProgress: Boolean
        get() = value != 0f

    /** Projects a delta onto this effect's axis (y for vertical, x for horizontal). */
    protected fun axisValue(delta: Offset): Float =
        if (orientation == Orientation.Vertical) delta.y else delta.x

    /** Expands an axis value back to a 2D delta on this effect's axis. */
    protected fun axisOffset(axisDelta: Float): Offset =
        if (orientation == Orientation.Vertical) Offset(0f, axisDelta) else Offset(axisDelta, 0f)

    /** Effective positive-side limit: 0 when the leading edge is disabled. */
    internal fun topMax(): Float = if (topEdge.enabled) topEdge.effectiveMax(maxOverscroll) else 0f

    /** Effective negative-side limit: 0 when the trailing edge is disabled. */
    internal fun bottomMax(): Float =
        if (bottomEdge.enabled) bottomEdge.effectiveMax(maxOverscroll) else 0f

    /**
     * Decorates a scroll with overscroll. Order of operations, per the
     * `OverscrollEffect` contract:
     *
     * 1. Cancel any in-flight settle — a new gesture wins.
     * 2. Release existing tension toward zero (any source), consuming linearly.
     * 3. `performScroll` with the remainder — called exactly once.
     * 4. Apply the leftover (drag sources above a 0.5 px noise floor, only)
     *    through the configured curve, clamped per edge, and count what was
     *    absorbed as consumed.
     *
     * Returns the total consumption — release + scroll + absorbed — so nothing
     * the effect handled leaks to ancestor scrollables.
     */
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

    /**
     * Decorates a fling: runs [performFling] exactly once, then converts the
     * leftover velocity into a bounce peak (`offset + velocity * factor`,
     * edge-gated and clamped) and — crucially — *awaits* the full absorb plus
     * settle inside this call. Callers (scrollable, connections) treat return
     * as the gesture's true end; a new drag cancels via the tracked job.
     */
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
