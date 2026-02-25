package com.example.myproject

import GameHud
import GameOverOverlay
import SpeedControlSlider
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.delay
import kotlin.random.Random
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import drawFruit
import java.nio.ByteBuffer


@Composable
fun FruitNinjaGame(
    // پوینتر به تابع اسمبلی برای محاسبات فیزیکی (حرکت و برخورد با دیوار)
    processPhysicsNeonDirect: (ByteBuffer, Int, Float, Float) -> Unit,
    // پوینتر به تابع اسمبلی برای تشخیص برخورد لمس کاربر با میوه
    findHitFruitIndexDirect: (ByteBuffer, Int, Float, Float, Float) -> Int,
    // تابع کمکی برای محاسبه سرعت نیمه‌های میوه پس از برش
    getSplitPhysics: (Float, Float) -> FloatArray,
    // پوینتر به تابع اسمبلی برای تشخیص میوه‌هایی که از پایین صفحه خارج شده‌اند
    findFallenFruitIndexDirect: (ByteBuffer, Int, Float) -> Int,
    // لیست تصاویر میوه‌ها (سیب، طالبی و غیره)
    fruitTypes: List<FruitType>,
    // عرض صفحه گوشی برای تنظیم مرزهای بازی
    screenWidth: Float
) {
    // --- مدیریت وضعیت (State Management) ---

    // لیست میوه‌های فعال در صحنه (استفاده از StateListOf برای به‌روزرسانی خودکار UI)
    val fruits = remember { mutableStateListOf<FruitState>() }
    // لیست نقاط مسیر حرکت انگشت کاربر برای رسم افکت تریل (Line Trail)
    val trailPoints = remember { mutableStateListOf<Offset>() }
    // امتیاز فعلی بازیکن
    var score by remember { mutableIntStateOf(0) }

    // تعداد جان‌های باقی‌مانده (شروع با ۳ جان)
    var lives by remember { mutableIntStateOf(3) }
    // وضعیت اتمام بازی (اگر True شود، همه چیز متوقف می‌گردد)
    var isGameOver by remember { mutableStateOf(false) }

    // ایجاد موتور بازی برای مدیریت بافر مستقیم و هماهنگی با اسمبلی
    val engine = remember { GameEngine(fruits, screenWidth, processPhysicsNeonDirect) }

    // --- ۱. واحد تولید میوه (Spawner) ---

    // این بلاک تا زمانی که بازی تمام نشده، به صورت موازی میوه تولید می‌کند
    LaunchedEffect(isGameOver) {
        while (!isGameOver) {
            // اضافه کردن میوه‌های جدید به لیست با ویژگی‌های رندوم
            spawnFruits(fruits, fruitTypes, screenWidth)

            // زمان انتظار برای پرتاب موج بعدی میوه‌ها
            val baseDelay = Random.nextLong(1200, 2000)
            // هرچه سرعت بازی (gameSpeed) بیشتر شود، فاصله زمانی پرتاب‌ها کمتر می‌شود
            delay((baseDelay / engine.gameSpeed).toLong())
        }
    }

    // --- ۲. حلقه اصلی بازی (Game Loop) ---

    // مدیریت فیزیک و چک کردن قوانین باخت در هر فریم
    LaunchedEffect(isGameOver) {
        while (!isGameOver) {
            // هماهنگ‌سازی با نرخ نوسازی صفحه (مثلاً ۶۰ یا ۱۲۰ بار در ثانیه)
            withFrameNanos {
                // ارسال داده‌ها به اسمبلی برای حرکت دادن میوه‌ها
                engine.updatePhysics()

                // الف) از موتور اسمبلی می‌پرسیم: "آیا میوه‌ای از خط مرگ (y=2500) رد شده؟"
                // خروجی: ایندکس آن میوه در بافر، یا 1- اگر هیچ میوه‌ای نیفتاده باشد.
                val fallenIndex = findFallenFruitIndexDirect(engine.getBuffer(), fruits.size, 2500f)

                // ب) اگر اسمبلی تشخیص داد که میوه‌ای سقوط کرده:
                if (fallenIndex != -1 && fallenIndex < fruits.size) {
                    val fallenFruit = fruits[fallenIndex]

                    // اگر میوه هنوز سالم بود و کاربر آن را نبریده بود، یک جان کم شود
                    if (!fallenFruit.isHalf) {
                        lives -= 1
                        // اگر جان‌ها تمام شد، وضعیت بازی به "پایان" تغییر کند
                        if (lives <= 0) isGameOver = true
                    }

                    // حذف میوه از لیست (چه بریده شده باشد چه سالم) برای آزاد کردن حافظه
                    fruits.removeAt(fallenIndex)
                }
            }
        }
    }

// استفاده از Box برای قرار دادن لایه‌های مختلف (Canvas، متن و دکمه‌ها) روی یکدیگر
    Box(modifier = Modifier.fillMaxSize()) {

        // نمایش گرافیکی امتیاز (Score) و تعداد جان‌های باقی‌مانده (Lives) در بالای صفحه
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .zIndex(1f) // بالاتر از Canvas
        ) {
            GameHud(score, lives)
        }

        // اصلی‌ترین بخش بصری بازی: بوم نقاشی (Canvas) برای رسم میوه‌ها و افکت‌ها
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                // مدیریت ورودی‌های لمسی کاربر
                .pointerInput(isGameOver) {
                    // اگر وضعیت بازی "پایان" (GameOver) باشد، لمس کردن صفحه هیچ واکنشی نخواهد داشت
                    if (isGameOver) return@pointerInput

                    // تشخیص برخورد لمس با میوه (با کمک موتور فیزیک و تابع اسمبلی)
                    handleTouchInput(engine, findHitFruitIndexDirect, trailPoints) { hitFruit ->
                        // در صورت اصابت شمشیر به میوه: ۱۰ امتیاز اضافه شود
                        score += 10
                        // میوه سالم حذف و دو نیمه با فیزیک جدید جایگزین شوند
                        handleFruitSplit(fruits, hitFruit, getSplitPhysics)
                    }
                }) {

            // رسم تک‌تک میوه‌های موجود در لیست بر اساس مختصاتی که اسمبلی محاسبه کرده است
            fruits.forEach { drawFruit(it) }

            // رسم خط سفید یا افکت شمشیر (Blade Trail) که به دنبال انگشت کاربر حرکت می‌کند
            drawBladeTrail(trailPoints)
        }

        // --- مدیریت لایه‌های کنترلی و وضعیت پایان بازی ---

        // بررسی وضعیت بازی: اگر بازی تمام نشده باشد (!isGameOver)، اسلایدر کنترل سرعت نمایش داده شود
        if (!isGameOver) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()           // اشغال کل عرض پایین صفحه
                    .align(Alignment.BottomCenter) // قرارگیری دقیق در پایین و وسط
                    .zIndex(1f)               // اولویت نمایش ۱ (بالاتر از بوم نقاشی یا Canvas)
                    .padding(bottom = 16.dp)  // ایجاد فاصله از لبه پایینی گوشی
            ) {
                // کامپوننت اسلایدر برای تغییر دینامیک سرعت موتور فیزیک (NEON)
                SpeedControlSlider(
                    currentSpeed = engine.gameSpeed,
                    onSpeedChange = { engine.gameSpeed = it } // به‌روزرسانی ضریب سرعت در لحظه
                )
            }
        }

        // --- لایه بالایی: صفحه Game Over (وسط صفحه) ---
        // این بلاک فقط زمانی اجرا می‌شود که بازیکن تمام جان‌های خود را از دست داده باشد
        if (isGameOver) {
            Box(
                modifier = Modifier
                    .fillMaxSize()            // پوشاندن کل صفحه برای جلوگیری از تعامل با زیر لایه‌ها
                    .align(Alignment.Center)  // مرکزیت بخشیدن به محتوای پایان بازی
                    .zIndex(2f)               // اولویت نمایش ۲ (بالاترین لایه ممکن، حتی روی اسلایدر)
            ) {
                // فراخوانی کامپوننت لایه‌ی باخت که در FruitComponents تعریف کردیم
                GameOverOverlay(
                    score = score,            // نمایش امتیاز نهایی کسب شده
                    onRestart = {
                        // عملیات ریست کلی (Hard Reset) برای شروع یک راند جدید:
                        lives = 3            // بازگرداندن جان‌ها به مقدار اولیه
                        score = 0            // صفر کردن امتیاز
                        fruits.clear()       // حذف تمام میوه‌های موجود در حافظه و بافر
                        trailPoints.clear()  // پاک کردن ردِ شمشیر باقی‌مانده روی صفحه
                        isGameOver = false   // تغییر وضعیت به "درحال بازی" و فعال شدن دوباره حلقه‌ها
                    }
                )
            }
        }
    } // پایان Box اصلی
} // پایان تابع FruitNinjaGame