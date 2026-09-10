package io.iamjosephmj.squishy.visual

import android.os.Build
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntOffset
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sign
import kotlin.math.tan

/**
 * The built-in visual library. All factories are remembered composables —
 * call them inline, combine with `+`, and hand the result to
 * `rememberOverScrollState(visual = ...)`.
 */
object OverscrollVisuals {

    /**
     * Content follows the finger: the offset becomes a translation along the
     * scroll axis.
     */
    @Composable
    fun pushDown(): OverscrollVisual = remember {
        OverscrollVisual { value, _, orientation ->
            Modifier.offset {
                if (orientation == Orientation.Vertical) {
                    IntOffset(0, value().roundToInt())
                } else {
                    IntOffset(value().roundToInt(), 0)
                }
            }
        }
    }

    /**
     * The container shrinks toward the pull, like compressing a sponge.
     *
     * @param minScaleX horizontal scale at a full-pull
     * @param minScaleY vertical scale at a full-pull
     */
    @Composable
    fun zoom(
        minScaleX: Float = 0.85f,
        minScaleY: Float = 0.85f,
    ): OverscrollVisual = remember(minScaleX, minScaleY) {
        OverscrollVisual { value, bounds, _ ->
            Modifier.graphicsLayer {
                val progress = progressOf(value(), bounds)
                scaleX = 1f - (1f - minScaleX) * progress
                scaleY = 1f - (1f - minScaleY) * progress
            }
        }
    }

    /**
     * Rotates around the screen axis; direction follows the edge being pulled.
     *
     * @param maxDegrees rotation at a full-pull
     */
    @Composable
    fun rotate(maxDegrees: Float = 8f): OverscrollVisual = remember(maxDegrees) {
        OverscrollVisual { value, bounds, _ ->
            Modifier.graphicsLayer {
                val v = value()
                rotationZ = maxDegrees * progressOf(v, bounds) * sign(v)
            }
        }
    }

    /**
     * Shears the content in the draw phase; direction follows the pull.
     *
     * @param maxDegrees shear angle at a full-pull
     */
    @Composable
    fun skew(maxDegrees: Float = 8f): OverscrollVisual = remember(maxDegrees) {
        OverscrollVisual { value, bounds, _ ->
            Modifier.drawWithContent {
                val v = value()
                val progress = progressOf(v, bounds)
                if (progress <= 0f) {
                    drawContent()
                } else {
                    val kx = tan(maxDegrees * progress * sign(v) * PI.toFloat() / 180f)
                    val matrixValues = FloatArray(16)
                    matrixValues[Matrix.ScaleX] = 1f
                    matrixValues[Matrix.ScaleY] = 1f
                    matrixValues[Matrix.ScaleZ] = 1f
                    matrixValues[Matrix.Perspective2] = 1f
                    matrixValues[Matrix.SkewX] = kx
                    matrixValues[Matrix.TranslateX] = -kx * size.height / 2f
                    val matrix = Matrix(matrixValues)
                    drawContext.canvas.save()
                    drawContext.canvas.concat(matrix)
                    try {
                        drawContent()
                    } finally {
                        drawContext.canvas.restore()
                    }
                }
            }
        }
    }

    /**
     * Leans the content in 3D around the horizontal axis, pivoting at the
     * pulled edge — a card tipping toward you.
     *
     * @param maxDegrees tilt at a full-pull
     */
    @Composable
    fun tilt(maxDegrees: Float = 12f): OverscrollVisual = remember(maxDegrees) {
        OverscrollVisual { value, bounds, _ ->
            Modifier.graphicsLayer {
                val v = value()
                val s = sign(v)
                rotationX = maxDegrees * progressOf(v, bounds) * s
                transformOrigin = TransformOrigin(0.5f, if (s >= 0f) 0f else 1f)
            }
        }
    }

    /**
     * Depth-of-field at the edge: focus falls off as the pull deepens.
     * The real blur needs API 31+; below it only [minAlpha] applies.
     *
     * @param maxRadiusPx blur radius at a full-pull
     * @param minAlpha opacity floor at a full-pull; keep at 1f to disable fading
     */
    @Composable
    fun blur(
        maxRadiusPx: Float = 24f,
        minAlpha: Float = 1f,
    ): OverscrollVisual = remember(maxRadiusPx, minAlpha) {
        OverscrollVisual { value, bounds, _ ->
            Modifier.graphicsLayer {
                val progress = progressOf(value(), bounds)
                alpha = 1f - (1f - minAlpha) * progress
                if (Build.VERSION.SDK_INT >= 31 && progress > 0f) {
                    val radius = maxRadiusPx * progress
                    renderEffect = BlurEffect(radius, radius)
                } else {
                    renderEffect = null
                }
            }
        }
    }

    /**
     * The edge dissolves into the background.
     *
     * @param minAlpha opacity floor at a full-pull
     */
    @Composable
    fun fade(minAlpha: Float = 0.5f): OverscrollVisual = remember(minAlpha) {
        OverscrollVisual { value, bounds, _ ->
            Modifier.graphicsLayer {
                val progress = progressOf(value(), bounds)
                alpha = 1f - (1f - minAlpha) * progress
            }
        }
    }
}
