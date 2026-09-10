package io.iamjosephmj.squishy.chamber.playground

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class PlaygroundViewModel : ViewModel() {
    var selected by mutableIntStateOf(0)
        private set

    fun select(index: Int) {
        selected = index
    }
}
