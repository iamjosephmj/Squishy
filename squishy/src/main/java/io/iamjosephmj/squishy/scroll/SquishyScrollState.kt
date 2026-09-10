package io.iamjosephmj.squishy.scroll

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.MutatorMutex
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * The container's own scroll engine: position, range and the
 * [ScrollableState] machinery (mutual exclusion, honest canScroll flags) that
 * the `scrollable` modifier drives. Overscroll gating keys off those flags —
 * fitting content produces no overscroll, matching platform behavior.
 */
internal class SquishyScrollState : ScrollableState {
    private val mutex = MutatorMutex()
    private val inProgressState = mutableStateOf(false)

    /** Content scroll position in px, clamped to `0..maxPosition`. */
    var position by mutableFloatStateOf(0f)
        internal set

    /** Scroll range in px; written by [scrollingLayout] during measurement. */
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
