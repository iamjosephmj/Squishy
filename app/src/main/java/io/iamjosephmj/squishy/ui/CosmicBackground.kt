package io.iamjosephmj.squishy.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import io.iamjosephmj.squishy.ui.theme.ApertureBg
import java.util.Random
import kotlin.math.min

private data class FieldStar(
    val center: Offset,
    val radius: Float,
    val alpha: Float,
)

@Composable
fun CosmicBackground(modifier: Modifier = Modifier) {
    val stars = remember {
        val random = Random(910)
        List(170) {
            FieldStar(
                center = Offset(random.nextFloat(), random.nextFloat()),
                radius = 0.6f + random.nextFloat() * random.nextFloat() * 2.0f,
                alpha = 0.2f + random.nextFloat() * 0.7f,
            )
        }
    }
    val drift = rememberInfiniteTransition(label = "nebula-drift")
    val phaseA by drift.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(52_000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "nebula-a",
    )
    val phaseB by drift.animateFloat(
        initialValue = 1f,
        targetValue = -1f,
        animationSpec = infiniteRepeatable(
            animation = tween(68_000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "nebula-b",
    )
    val phaseC by drift.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(84_000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "nebula-c",
    )

    Canvas(modifier) {
        drawRect(ApertureBg)

        fun nebula(color: Color, phase: Float, cx: Float, cy: Float, spread: Float) {
            val wobble = phase * 0.05f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(color, Color.Transparent),
                    center = Offset(size.width * (cx + wobble), size.height * (cy - wobble)),
                    radius = min(size.width, size.height) * spread,
                ),
                radius = min(size.width, size.height) * spread,
                center = Offset(size.width * (cx + wobble), size.height * (cy - wobble)),
            )
        }

        nebula(Color(0x52A06EFF), phaseA, cx = 0.1f, cy = 0.15f, spread = 0.75f)
        nebula(Color(0x2E46B4DC), phaseB, cx = 0.9f, cy = 0.35f, spread = 0.7f)
        nebula(Color(0x1AFFA882), phaseC, cx = 0.4f, cy = 1.05f, spread = 0.65f)

        rotate(degrees = 24f, pivot = center) {
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0x0BDCD7F0),
                        Color(0x0FF0F0DC),
                        Color(0x0BC8C8F0),
                        Color.Transparent,
                    ),
                    start = Offset.Zero,
                    end = Offset(size.width * 1.6f, 0f),
                ),
                size = Size(size.width * 1.8f, size.height),
                topLeft = Offset(-size.width * 0.4f, 0f),
            )
        }

        stars.forEach { star ->
            drawCircle(
                color = Color(0xFFE9EAF0).copy(alpha = star.alpha),
                radius = star.radius,
                center = star.center.scaledTo(size),
            )
        }

        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x00000000), Color(0x66000000)),
                center = Offset(size.width * 0.5f, size.height * 0.22f),
                radius = size.width * 1.6f,
            ),
        )
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x4D000000), Color(0x00000000)),
                center = Offset(size.width * 0.5f, size.height * 1.1f),
                radius = size.height * 0.8f,
            ),
        )
    }
}

private fun Offset.scaledTo(size: Size): Offset =
    Offset(x * size.width, y * size.height)
