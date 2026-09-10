package io.iamjosephmj.squishy.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.iamjosephmj.squishy.ui.theme.ApertureLine2
import io.iamjosephmj.squishy.ui.theme.TextSoft

@Composable
fun Eyebrow(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = TextSoft,
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .border(1.dp, ApertureLine2, RoundedCornerShape(999.dp))
            .background(Color(0x08FFFFFF))
            .padding(horizontal = 12.dp, vertical = 5.dp),
    )
}
