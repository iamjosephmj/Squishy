package io.iamjosephmj.squishy.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith

/** Material emphasized easing, the curve all chamber transitions run on. */
val EmphasizedEasing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
private const val EnterDuration = 420
private const val ExitDuration = 240

/** Forward enter: slide from [offsetFraction] of width + fade + scale-up. */
fun axisEnterTransition(offsetFraction: Float): EnterTransition =
    slideInHorizontally(
        animationSpec = tween(EnterDuration, easing = EmphasizedEasing),
        initialOffsetX = { (it * offsetFraction).toInt() },
    ) + fadeIn(
        animationSpec = tween(EnterDuration, delayMillis = 40, easing = EmphasizedEasing),
    ) + scaleIn(
        animationSpec = tween(EnterDuration, easing = EmphasizedEasing),
        initialScale = 0.94f,
    )

/** Exit: slide to [offsetFraction] of width + fade + scale-down; shorter than enter. */
fun axisExitTransition(offsetFraction: Float): ExitTransition =
    slideOutHorizontally(
        animationSpec = tween(ExitDuration, easing = EmphasizedEasing),
        targetOffsetX = { (it * offsetFraction).toInt() },
    ) + fadeOut(
        animationSpec = tween(ExitDuration, easing = EmphasizedEasing),
    ) + scaleOut(
        animationSpec = tween(ExitDuration, easing = EmphasizedEasing),
        targetScale = 0.96f,
    )
