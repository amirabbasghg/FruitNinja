
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.*
import kotlinx.coroutines.delay
import kotlin.random.Random
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.example.myproject.FruitState

@Composable
fun ScoreDisplay(score: Int) {
    Text(
        text = "Score: $score",
        color = Color.Yellow,
        fontSize = 35.sp,
        modifier = Modifier.padding(25.dp)
    )
}

fun DrawScope.drawFruit(fruit: FruitState) {
    rotate(fruit.rotation, pivot = Offset(fruit.x, fruit.y)) {
        drawImage(
            image = fruit.image,
            dstOffset = IntOffset((fruit.x - 100).toInt(), (fruit.y - 100).toInt()),
            dstSize = IntSize(200, 200)
        )
    }
}
@Composable
fun SpeedControlSlider(
    currentSpeed: Float,
    onSpeedChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 40.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // نمایش متن سرعت با رنگ متغیر (اگر سرعت زیاد شد قرمز شود)
        Text(
            text = "Time Scale: ${"%.1f".format(currentSpeed)}x",
            color = if (currentSpeed > 1.5f) Color.Red else Color.White,
            fontSize = 18.sp,
            style = androidx.compose.ui.text.TextStyle(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Slider(
            value = currentSpeed,
            onValueChange = onSpeedChange,
            valueRange = 0.1f..3.0f, // محدوده سرعت از ۰.۱ تا ۳ برابر
            colors = SliderDefaults.colors(
                thumbColor = Color.Cyan,
                activeTrackColor = Color.Cyan,
                inactiveTrackColor = Color.Gray.copy(alpha = 0.5f)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // راهنمای کوچک زیر اسلایدر
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Slow", color = Color.Gray, fontSize = 12.sp)
            Text("Normal", color = Color.Gray, fontSize = 12.sp)
            Text("Fast", color = Color.Gray, fontSize = 12.sp)
        }
    }
}
fun DrawScope.drawBladeTrail(points: List<Offset>) {
    if (points.size > 1) {
        val path = Path().apply {
            moveTo(points.first().x, points.first().y)
            points.forEach { lineTo(it.x, it.y) }
        }
        drawPath(path, Color.White, style = Stroke(width = 10f))
    }
}