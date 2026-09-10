package io.iamjosephmj.squishy.physics

data /**
 * Behavior of a single edge (top or bottom) of an overscroll container.
 *
 * A disabled edge does not absorb: its drag delta and fling velocity pass
 * through to ancestor scrollables, and no offset accumulates on the state.
 */
class EdgeConfig(
    /** When `false`, this edge is inert — see class docs. */
    val enabled: Boolean = true,
    /** Per-edge limit in px; `null` inherits [OverScrollConfig.maxOverscroll]. */
    val maxOverscroll: Float? = null,
) {
    internal fun effectiveMax(fallback: Float): Float = maxOverscroll ?: fallback
}
