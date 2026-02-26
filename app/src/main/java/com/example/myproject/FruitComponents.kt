import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import com.example.myproject.R
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.example.myproject.FruitState
// ===================================================================
// ۱. ویجت نوار بالای صفحه (نمایش امتیاز و جان‌ها)
// ===================================================================
@Composable
fun GameHud(score: Int, lives: Int, modifier: Modifier = Modifier) {
    // ایجاد یک ردیف افقی که کل عرض صفحه را می‌گیرد
    Row(
        // modifierها ویژگی‌های ظاهری ویجت را تعیین می‌کنند
        modifier = modifier
            .fillMaxWidth() //  (پر کردن کل عرض)
            .padding(20.dp), // ایجاد ۲۰ واحد فاصله امن از لبه‌های صفحه نمایش
        horizontalArrangement = Arrangement.SpaceBetween, // هل دادن باکس امتیاز به چپ و باکس جان به راست
        verticalAlignment = Alignment.CenterVertically // تراز کردن المان‌ها دقیقاً در وسط محور عمودی
    ) {

        // --- باکس امتیاز (سمت چپ) ---
        Surface(
            color = Color.Black.copy(alpha = 0.6f), // رنگ پس‌زمینه مشکی با ۶۰ درصد شفافیت (شیشه‌ای)
            shape = RoundedCornerShape(24.dp), // گرد کردن لبه‌های باکس به شعاع ۲۴
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700).copy(0.5f)) // یک خط نازک طلایی و نیمه‌شفاف دور باکس
        ) {
            // یک ردیف داخلی برای چیدن آیکون جام و عدد امتیاز کنار هم
            Row(
                verticalAlignment = Alignment.CenterVertically, // تراز عمودی در مرکز
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp) // فاصله دادن محتوا از لبه‌های خود باکس
            ) {
                // آیکون جام قهرمانی
                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFFFD700)) // tint رنگ آیکون را طلایی می‌کند
                Spacer(modifier = Modifier.width(8.dp)) // ایجاد ۸ واحد فضای خالی بین آیکون و عدد

                // نمایش عدد امتیاز
                Text(
                    text = "$score", // قرار دادن متغیر امتیاز داخل رشته متنی
                    color = Color.White, // رنگ متن سفید
                    fontSize = 24.sp, // اندازه فونت ۲۴ (حساس به تنظیمات گوشی کاربر)
                    fontWeight = FontWeight.Black // ضخیم‌ترین حالت ممکن برای فونت (Boldتر از Bold)
                )
            }
        }

        // --- باکس جان‌ها (سمت راست) ---
        Surface(
            color = Color.Black.copy(alpha = 0.6f), // پس‌زمینه مشکی شیشه‌ای
            shape = RoundedCornerShape(24.dp), // گرد کردن لبه‌ها
            modifier = Modifier.shadow(8.dp, CircleShape) // اضافه کردن یک سایه ملایم و محو به شکل دایره پشت باکس (جلوه سه‌بعدی)
        ) {
            // منطق  تکرار قلب‌ها بر اساس جان باقی‌مانده:
            Text(
                text = "❤️".repeat(lives) + "💔".repeat(3-lives), // تکرار رشته‌ها بر اساس فرمول
                fontSize = 22.sp, // فونت ایموجی‌ها (کمی کوچکتر از امتیاز)
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp) // فاصله دادن قلب‌ها از لبه‌های باکس
            )
        }
    }
}

// ===================================================================
// ۲. تابع نقاشی کردن میوه‌ها (که فقط درون Canvas قابل اجراست)
// ===================================================================

fun DrawScope.drawFruit(fruit: FruitState) {
    // تابع چرخش بوم (Canvas) حول یک نقطه خاص (Pivot)
    // fruit.rotation مقدار زاویه‌ای است که موتور فیزیک در هر فریم محاسبه کرده
    // Offset(fruit.x, fruit.y) یعنی مرکز چرخش دقیقاً وسط خود میوه باشد (نه گوشه بالا-چپ بوم)
    rotate(fruit.rotation, pivot = Offset(fruit.x, fruit.y)) {

        // رسم تصویر بیت‌مپ (Bitmap) مربوط به این میوه خاص (سیب، لیمو یا نیمه میوه)
        drawImage(
            image = fruit.image,

            // تعیین مختصات شروع رسم:
            // چون نقطه (x,y) باید دقیقاً در *مرکز* میوه باشد، و رسم تصویر از گوشه بالا-چپ انجام می‌شود،
            // پس ما باید نقطه شروع رسم را ۱۰۰ واحد به چپ و ۱۰۰ واحد به بالا ببریم (چون طول و عرض میوه ۲۰۰ است)
            dstOffset = IntOffset((fruit.x - 100).toInt(), (fruit.y - 100).toInt()),

            // ابعاد میوه روی صفحه نمایش (مربعی با عرض و ارتفاع ۲۰۰ پیکسل)
            dstSize = IntSize(200, 200)
        )
    }
}

// ===================================================================
// ۳. اسلایدر کنترل سرعت بازی
// ===================================================================

@Composable
fun SpeedControlSlider(
    currentSpeed: Float, // مقدار فعلی سرعت
    onSpeedChange: (Float) -> Unit // Callback: تابعی که هر وقت کاربر اسلایدر را کشید، صدای زده شود
) {
    // ویجت کارتی برای قرار دادن اسلایدر داخل آن با افکت شیشه‌ای
    Card(
        modifier = Modifier
            .fillMaxWidth() // کارت کل عرض موجود را بگیرد
            .padding(20.dp), // ۲۰ واحد فاصله از دیوارها
        shape = RoundedCornerShape(28.dp), // لبه‌های نرم و گرد کارت

        // رنگ پس‌زمینه کارت: سفیدِ بسیار بسیار شفاف (۸٪) که جلوه‌ی Glassmorphism (شیشه‌ای) می‌دهد
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),

        // خط دور کارت: سفید با ۱۰٪ شفافیت
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(0.1f))
    ) {
        // قرار دادن المان‌های داخلی کارت زیر هم
        Column(
            modifier = Modifier.padding(20.dp), // فاصله محتوای کارت از لبه‌های خود کارت
            horizontalAlignment = Alignment.CenterHorizontally // تراز کردن همه محتویات (آیکون، متن، اسلایدر) در مرکز محور افقی
        ) {

            // --- ردیف بالایی: آیکون و متن سرعت فعلی ---
            Row(verticalAlignment = Alignment.CenterVertically) { // کنار هم چیدن المان‌ها در یک خط

                // نمایش آیکون متناسب با سرعت بازی
                Icon(
                    // اگر سرعت بیشتر از ۲ بود (سخت)، آیکون صاعقه نشان بده، در غیر این‌صورت آیکون سرعت‌سنج
                    imageVector = if(currentSpeed > 2f) Icons.Default.Bolt else Icons.Default.Speed,
                    contentDescription = null, // بدون نیاز به توضیحات برای نابینایان

                    // تغییر رنگ آیکون: اگر سرعت بالا بود زرد-نارنجی، اگر پایین بود فیروزه‌ای-آبی
                    tint = if(currentSpeed > 1.8f) Color(0xFFFFD600) else Color(0xFF00E5FF)
                )
                Spacer(modifier = Modifier.width(8.dp)) // ۸ واحد فاصله

                // نمایش متن سرعت
                Text(
                    // فرمت‌بندی رشته متنی: عدد سرعت را فقط با ۱ رقم اعشار نشان بده (مثلاً 1.5x)
                    text = "CHALLENGE LEVEL: ${"%.1f".format(currentSpeed)}x",
                    color = Color.White, // رنگ متن سفید
                    letterSpacing = 1.sp, // فاصله بین حروف برای خواناتر شدن و زیباتر شدن ظاهر داشبورد
                    style = MaterialTheme.typography.labelLarge // استفاده از استایل آماده متریال دیزاین برای برچسب‌ها
                )
            }

            // --- اسلایدر تغییر سرعت ---
            Slider(
                value = currentSpeed, // مقدار فعلی متصل به اسلایدر (مقدار اولیه)
                onValueChange = onSpeedChange, // وقتی اسلایدر تغییر کرد، این مقدار جدید به تابع والد برگردانده شود
                valueRange = 0.5f..3.0f, // کمترین مقدار ممکن 0.5 (آسان) و بیشترین 3.0 (بسیار سخت)

                // شخصی‌سازی رنگ‌های اسلایدر
                colors = SliderDefaults.colors(
                    thumbColor = Color.White, // رنگ دکمه‌ای که کاربر می‌گیرد و می‌کشد (سفید)
                    activeTrackColor = Color(0xFF00E5FF), // رنگ نوار سمت چپ دکمه (پر شده، رنگ فیروزه‌ای نئونی)
                    inactiveTrackColor = Color.White.copy(alpha = 0.2f) // رنگ نوار سمت راست دکمه (خالی، سفید شفاف)
                )
            )

            // --- ردیف پایینی: متن‌های راهنما (آسان و سخت) ---
            Row(
                modifier = Modifier.fillMaxWidth(), // این ردیف به اندازه عرض کارت باز شود
                horizontalArrangement = Arrangement.SpaceBetween // یکی به چپ هل داده شود، یکی به راست
            ) {
                // لیبل سمت چپ زیر اسلایدر برای سرعت پایین (لاک‌پشت)
                Text("🐢 Relax", color = Color.White.copy(0.5f), fontSize = 10.sp)
                // لیبل سمت راست زیر اسلایدر برای سرعت بالا (صاعقه)
                Text("⚡ Insane", color = Color.White.copy(0.5f), fontSize = 10.sp)
            }
        }
    }
}

// ===================================================================
// ۴. صفحه پایان بازی (Game Over Screen)
// ===================================================================

@Composable
fun GameOverOverlay(score: Int, onRestart: () -> Unit) { // score: امتیاز نهایی، onRestart: تابع دکمه شروع مجدد
    // ۱. باکس نگهدارنده اصلی (لایه‌ای روی کل صفحه بازی)
    Box(
        modifier = Modifier
            .fillMaxSize() // پر کردن ۱۰۰٪ ارتفاع و عرض صفحه نمایش
            .background(
                // ساخت گرادینت عمودی برای پس‌زمینه تاریک شونده
                Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.Black.copy(0.9f)) // از رنگ شفاف (بالا) به رنگ مشکی با ۹۰٪ غلظت (پایین)
                )
            ),
        contentAlignment = Alignment.Center // چیدن تمام ویجت‌های داخل این باکس دقیقاً وسط صفحه
    ) {

        // قرار دادن المان‌های Game Over زیر هم به صورت ستونی
        Column(horizontalAlignment = Alignment.CenterHorizontally) { // مرکزچین کردن المان‌ها روی محور افقی

            // ۲. آیکون اسکلت برای نشان دادن باخت بازی (با استفاده از تصویر Resource)
            Icon(
                painter = painterResource(id = R.drawable.ic_skull), // دریافت تصویر اسکلت از فایل‌های drawable پروژه
                contentDescription = "Skull", // نام متنی برای قابلیت دسترس‌پذیری
                modifier = Modifier.size(80.dp), // تغییر سایز آیکون به ۸۰ در ۸۰ واحد
                tint = Color.White // رنگ‌آمیزی آیکون به رنگ سفید یک‌دست
            )

            // ۳. متن اصلی باخت با فونت بزرگ
            Text(
                text = "GAME OVER", // متن اصلی
                fontSize = 50.sp, // فونت بسیار بزرگ (۵۰)
                fontWeight = FontWeight.Black, // ضخیم‌ترین حالت فونت
                color = Color(0xFFFF3D00), // رنگ نارنجی متمایل به قرمز تند برای القای حس باخت و خطر
                style = MaterialTheme.typography.displayMedium // استفاده از استایل تیتر بزرگ متریال دیزاین
            )

            Spacer(modifier = Modifier.height(10.dp)) // فضای خالی ۱۰ واحدی

            // ۴. کادر نمایش امتیاز نهایی کاربر
            Surface(
                color = Color.White.copy(alpha = 0.1f), // پس‌زمینه کادر سفیدِ بسیار شفاف
                shape = RoundedCornerShape(16.dp) // لبه‌های گرد کادر (کمی کمتر از دکمه‌ها)
            ) {
                // متن داخل کادر امتیاز
                Text(
                    text = "FINAL SCORE: $score", // چسباندن عدد امتیاز به انتهای کلمه
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp), // پدینگ داخلی برای بزرگ شدن سایز کادر دور متن
                    color = Color.White, // رنگ متن
                    fontSize = 20.sp // سایز ۲۰ (متوسط و خوانا)
                )
            }

            Spacer(modifier = Modifier.height(40.dp)) // فاصله ۴۰ واحدی برای جدا کردن دکمه از متن بالا

            // ۵. دکمه بازگشت و شروع مجدد (Play Again)
            Button(
                onClick = onRestart, // اجرای تابع Callback که از والد فرستاده شده (ریست کردن امتیاز و میوه‌ها)
                modifier = Modifier
                    .height(60.dp) // ارتفاع ۶۰ واحدی دکمه برای راحت کلیک شدن
                    .width(220.dp), // عرض ثابت ۲۲۰ واحدی دکمه
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)), // تغییر رنگ بدنه دکمه به سبز پررنگِ متریال
                shape = RoundedCornerShape(30.dp), // کپسولی کردن کامل دکمه (گردی ۳۰ معمولاً دکمه را کاملاً بیضی می‌کند)
                // افکت سایه برای القای برجستگی و سه‌بعدی بودن دکمه
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 10.dp)
            ) {
                // محتوای داخل دکمه (آیکون کنار متن)
                Row(verticalAlignment = Alignment.CenterVertically) { // تراز عمودی مرکز
                    Icon(Icons.Default.Refresh, contentDescription = null) // آیکون پیش‌فرض "چرخش/رفرش" متریال
                    Spacer(modifier = Modifier.width(10.dp)) // فضای خالی بین آیکون و کلمه
                    Text("PLAY AGAIN", fontSize = 18.sp, fontWeight = FontWeight.Bold) // متن درشت و بولد دکمه
                }
            }
        }
    }
}