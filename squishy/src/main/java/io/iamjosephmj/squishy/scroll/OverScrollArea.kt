package io.iamjosephmj.squishy.scroll

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import io.iamjosephmj.squishy.state.OverScrollState

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OverScrollArea(
    state: OverScrollState,
    modifier: Modifier = Modifier,
    containerEffect: Boolean = true,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalOverscrollConfiguration provides null) {
        Box(modifier = modifier.overScrollConnection(state, containerEffect)) {
            content()
        }
    }
}
