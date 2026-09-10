package io.iamjosephmj.squishy.physics

data class EdgeConfig(
    val enabled: Boolean = true,
    val maxOverscroll: Float? = null,
) {
    internal fun effectiveMax(fallback: Float): Float = maxOverscroll ?: fallback
}
