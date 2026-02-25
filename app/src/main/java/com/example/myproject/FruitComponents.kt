import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.example.myproject.FruitState
@Composable
fun GameHud(score: Int, lives: Int, modifier: Modifier = Modifier) {
    // ایجاد یک ردیف افقی که کل عرض صفحه را می‌گیرد
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(20.dp), // فاصله از لبه‌های گوشی
        horizontalArrangement = Arrangement.SpaceBetween, // امتیاز چپ، جان‌ها راست
        verticalAlignment = Alignment.CenterVertically // تراز کردن عمودی در مرکز
    ) {
        // --- باکس امتیاز ---
        Surface(
            color = Color.Black.copy(alpha = 0.6f), // پس‌زمینه مشکی نیمه‌شفاف
            shape = RoundedCornerShape(24.dp), // لبه‌های کاملاً گرد
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700).copy(0.5f)) // خط دور طلایی
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // آیکون جام قهرمانی طلایی 🏆
                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFFFD700))
                Spacer(modifier = Modifier.width(8.dp)) // فاصله بین آیکون و عدد
                Text(
                    text = "$score", // نمایش عدد امتیاز
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black // متن خیلی ضخیم
                )
            }
        }

        // --- باکس جان‌ها (❤️) ---
        Surface(
            color = Color.Black.copy(alpha = 0.6f),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.shadow(8.dp, CircleShape) // اضافه کردن سایه برای درخشش بیشتر
        ) {
            // منطق باحال: به تعداد جان‌های باقی‌مانده ❤️ و برای بقیه 💔 چاپ کن
            // مثلا اگر ۲ جان داشته باشی خروجی می‌شود: ❤️❤️💔
            Text(
                text = "❤️".repeat(lives) + "💔".repeat(3-lives),
                fontSize = 22.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}
fun DrawScope.drawFruit(fruit: FruitState) {
    // چرخاندن بوم حول مرکز میوه
    // fruit.rotation زاویه‌ای است که اسمبلی در هر لحظه حساب می‌کند
    rotate(fruit.rotation, pivot = Offset(fruit.x, fruit.y)) {
        // رسم تصویر میوه (سیب، لیمو و...)
        drawImage(
            image = fruit.image,
            // تعیین موقعیت: چون می‌خواهیم مرکز تصویر روی (x,y) باشد، نصف اندازه (100) را کم می‌کنیم
            dstOffset = IntOffset((fruit.x - 100).toInt(), (fruit.y - 100).toInt()),
            // اندازه میوه در صفحه (۲۰۰ در ۲۰۰ پیکسل)
            dstSize = IntSize(200, 200)
        )
    }
}

@Composable
fun SpeedControlSlider(
    currentSpeed: Float, // سرعت فعلی (مثلاً 1.0)
    onSpeedChange: (Float) -> Unit // تابعی که وقتی اسلایدر تغییر کرد صدا زده می‌شود
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        shape = RoundedCornerShape(28.dp),
        // رنگ سفید بسیار شفاف که حالتی شیشه‌ای (Glassmorphism) ایجاد می‌کند
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(0.1f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ردیف بالای اسلایدر (آیکون و متن)
            Row(verticalAlignment = Alignment.CenterVertically) {
                // اگر سرعت خیلی زیاد شد آیکون رعد (Bolt) و اگر معمولی بود آیکون سرعت‌سنج (Speed) نشان بده
                Icon(
                    imageVector = if(currentSpeed > 2f) Icons.Default.Bolt else Icons.Default.Speed,
                    contentDescription = null,
                    // تغییر رنگ آیکون بر اساس سرعت (زرد برای سرعت بالا، آبی برای سرعت پایین)
                    tint = if(currentSpeed > 1.8f) Color(0xFFFFD600) else Color(0xFF00E5FF)
                )
                Spacer(modifier = Modifier.width(8.dp))
                // نمایش عدد سرعت با یک رقم اعشار (مثلاً CHALLENGE LEVEL: 1.5x)
                Text(
                    text = "CHALLENGE LEVEL: ${"%.1f".format(currentSpeed)}x",
                    color = Color.White,
                    letterSpacing = 1.sp,
                    style = MaterialTheme.typography.labelLarge
                )
            }

            // خودِ اسلایدر
            Slider(
                value = currentSpeed, // مقدار فعلی اسلایدر
                onValueChange = onSpeedChange, // وقتی کاربر انگشتش را می‌کشد
                valueRange = 0.5f..3.0f, // محدوده سرعت از نیم برابر تا ۳ برابر
                colors = SliderDefaults.colors(
                    thumbColor = Color.White, // دایره‌ای که کاربر می‌گیرد
                    activeTrackColor = Color(0xFF00E5FF), // رنگ نوار سمت چپ (پر شده)
                    inactiveTrackColor = Color.White.copy(alpha = 0.2f) // رنگ نوار سمت راست (خالی)
                )
            )

            // متون راهنما زیر اسلایدر
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("🐢 Relax", color = Color.White.copy(0.5f), fontSize = 10.sp)
                Text("⚡ Insane", color = Color.White.copy(0.5f), fontSize = 10.sp)
            }
        }
    }
}
@Composable
fun GameOverOverlay(score: Int, onRestart: () -> Unit) {
    // ۱. باکس اصلی که کل صفحه را می‌پوشاند
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                // ایجاد یک گرادینت عمودی: از بالا (شفاف) به پایین (مشکی غلیظ)
                // این کار باعث می‌شود بازی در پس‌زمینه کمی دیده شود اما تمرکز روی متن باخت باشد
                Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.Black.copy(0.9f))
                )
            ),
        contentAlignment = Alignment.Center // تمام محتویات را دقیقاً وسط صفحه قرار بده
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            // ۲. نمایش ایموجی مرگ/باخت با اندازه بزرگ
            Text("💀", fontSize = 80.sp)

            // ۳. متن قرمز و ضخیم GAME OVER
            Text(
                text = "GAME OVER",
                fontSize = 50.sp,
                fontWeight = FontWeight.Black, // بیشترین ضخامت ممکن برای فونت
                color = Color(0xFFFF3D00), // رنگ نارنجی-قرمز تند
                style = MaterialTheme.typography.displayMedium
            )

            Spacer(modifier = Modifier.height(10.dp)) // فاصله کوچک

            // ۴. باکس نمایش امتیاز نهایی
            Surface(
                color = Color.White.copy(alpha = 0.1f), // پس‌زمینه بسیار شفاف سفید
                shape = RoundedCornerShape(16.dp) // لبه‌های نرم
            ) {
                Text(
                    text = "FINAL SCORE: $score", // نمایش امتیازی که در طول بازی جمع شده
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    color = Color.White,
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(40.dp)) // فاصله بزرگ قبل از دکمه

            // ۵. دکمه ریستارت (شروع مجدد)
            Button(
                onClick = onRestart, // وقتی کلیک شد، تابعی که از والد آمده را اجرا کن
                modifier = Modifier
                    .height(60.dp)
                    .width(220.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)), // رنگ سبز (نشانه شروع دوباره)
                shape = RoundedCornerShape(30.dp), // دکمه کاملاً کپسولی شکل
                // ایجاد سایه زیر دکمه برای حس برجستگی (۳ بعدی بودن)
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 10.dp)
            ) {
                // محتویات داخل دکمه (آیکون + متن)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Refresh, contentDescription = null) // آیکون بازنشانی
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("PLAY AGAIN", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}