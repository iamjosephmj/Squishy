package io.iamjosephmj.squishy

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

fun interface OverscrollVisual {
    fun visual(value: () -> Float, bounds: Float, orientation: Orientation): Modifier
}

operator fun OverscrollVisual.plus(other: OverscrollVisual): OverscrollVisual =
    CombinedOverscrollVisual(this, other)

private data class CombinedOverscrollVisual(
    private val first: OverscrollVisual,
    private val second: OverscrollVisual,
) : OverscrollVisual {
    override fun visual(value: () -> Float, bounds: Float, orientation: Orientation): Modifier =
        first.visual(value, bounds, orientation)
            .then(second.visual(value, bounds, orientation))
}

internal fun progressOf(value: Float, bounds: Float): Float =
    if (bounds <= 0f) 0f else (abs(value) / bounds).coerceIn(0f, 1f)

object OverscrollVisuals {

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

    @Composable
    fun rotate(maxDegrees: Float = 8f): OverscrollVisual = remember(maxDegrees) {
        OverscrollVisual { value, bounds, _ ->
            Modifier.graphicsLayer {
                val v = value()
                rotationZ = maxDegrees * progressOf(v, bounds) * sign(v)
            }
        }
    }

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
                    renderEffect = BlurEffect(radiusX = radius, radiusY = radius)
                } else {
                    renderEffect = null
                }
            }
        }
    }

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
