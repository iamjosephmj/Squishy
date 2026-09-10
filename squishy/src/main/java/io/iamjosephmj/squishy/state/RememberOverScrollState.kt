package io.iamjosephmj.squishy.state

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import io.iamjosephmj.squishy.physics.BaseOverscrollEffect
import io.iamjosephmj.squishy.physics.OverScrollConfig
import io.iamjosephmj.squishy.scroll.SquishyScrollState
import io.iamjosephmj.squishy.visual.OverscrollVisual
import io.iamjosephmj.squishy.visual.OverscrollVisuals
import io.iamjosephmj.squishy.visual.PushDownOverscrollEffect

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
