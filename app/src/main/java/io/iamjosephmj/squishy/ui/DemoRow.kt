package io.iamjosephmj.squishy.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.iamjosephmj.squishy.ui.theme.TextBright
import io.iamjosephmj.squishy.ui.theme.TextSoft

@Composable
fun DemoRow(index: Int, modifier: Modifier = Modifier) {
    ApertureCard(modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = "Row %02d".format(index + 1),
                style = MaterialTheme.typography.titleMedium,
                color = TextBright,
            )
            Text(
                text = "Drag past the edge and feel the plugin system respond.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSoft,
            )
        }
    }
}
