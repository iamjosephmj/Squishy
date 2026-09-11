package io.iamjosephmj.squishy

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.iamjosephmj.squishy.ui.ScreenScaffold
import io.iamjosephmj.squishy.ui.theme.SquishyTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/**
 * Renders the chamber header and captures it to a PNG so the back control's
 * shape can be reviewed: a circular disc with a backward arrow beside the
 * uppercase title.
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w1200dp-h2608dp-480dpi")
class ScreenScaffoldHeaderTest {

    @get:Rule
    val composeRule = createAndroidComposeRule(ComponentActivity::class.java)

    @Test
    fun header_showsCircularBackButtonWithTitle() {
        composeRule.setContent {
            SquishyTheme(darkTheme = true) {
                Surface(Modifier.fillMaxSize()) {
                    ScreenScaffold(
                        title = "container",
                        onBack = {},
                        intro = "Path 1 basics — a plain column that squishes past its edges.",
                    ) {
                        Text("content")
                    }
                }
            }
        }
        composeRule.onNodeWithContentDescription("Back").assertExists()
        composeRule.onNodeWithText("CONTAINER").assertExists()

        composeRule.activity.window.decorView.let { view ->
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            view.draw(Canvas(bitmap))
            File(System.getProperty("java.io.tmpdir"), "squishy_header_render.png").outputStream().use { stream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            }
        }
    }
}
