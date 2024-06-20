package io.iamjosephmj.squishy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.iamjosephmj.squishy.ui.theme.SquishyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val overScroll = rememberPushDownOverscrollEffect()

            SquishyTheme {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(40.dp)
                        .overScroll(
                            overscrollEffect = overScroll,
                            isParentOverScrollEnabled = false
                        ),
                ) {
                    for (item in 1..50) {
                        Button(
                            onClick = {},
                            modifier = Modifier
                                .fillMaxWidth()
                                .childOverScrollSupport(overScroll)
                                .height(50.dp)
                                .padding(1.dp)
                        ) {
                            Text(
                                text = "item$item", textAlign = TextAlign.Center
                            )
                        }

                    }
                }
            }
        }
    }
}
