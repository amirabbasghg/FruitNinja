package com.example.myproject

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputScope
import kotlin.random.Random

// این ایمپورت‌ها را بالای فایل FruitLogic.kt اضافه کن
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.text.clear

class GameEngine(
    val fruits: SnapshotStateList<FruitState>, // لیستی هوشمند که با تغییر هر میوه، UI خودش آپدیت می‌شود (مثل Observable در فلاتر)
    val screenWidth: Float,                    // عرض صفحه برای چک کردن برخورد میوه‌ها به دیواره‌ها
    val processPhysicsNeonDirect: (ByteBuffer, Int, Float, Float) -> Unit // رفرنس تابع سی‌‎پلاس‌پلاس (اسمبلی)
) {
    // سرعت بازی که به صورت هوشمند (State) تعریف شده؛ تغییر این مقدار، سرعت پرتاب را آنی عوض می‌کند
    var gameSpeed by mutableFloatStateOf(1.0f)

    // رزرو یک فضای اختصاصی در RAM خارج از مدیریت جاوا (برای اینکه C++ مستقیم به آن دسترسی داشته باشد)
    private val maxFruits = 100
    // هر میوه 5 ویژگی (X, Y, VX, VY, Rotation) دارد که هر کدام 4 بایت (Float) فضا می‌گیرند
    private val byteBuffer = ByteBuffer.allocateDirect(maxFruits * 5 * 4).order(ByteOrder.nativeOrder())

    // یک "لایه دید" روی بافر که اجازه می‌دهد به جای بایت، مستقیماً با اعداد اعشاری (Float) کار کنیم
    private val floatBuffer = byteBuffer.asFloatBuffer()

    // تابعی برای فرستادن کل آدرس حافظه به بخش‌های دیگر
    fun getBuffer(): ByteBuffer = byteBuffer

    // آپدیت کردن فیزیک (جایی که جادو اتفاق می‌افتد)
    fun updatePhysics() {
        if (fruits.isEmpty()) return // اگر میوه‌ای در صحنه نیست، وقت پردازنده را نگیر

        // ۱. نشانگرِ نوشتن در حافظه را به نقطه صفر (شروع) برمی‌گردانیم
        floatBuffer.position(0)

        // ۲. انتقال اطلاعات از دنیای کاتلین به دنیای حافظه خام (RAM)
        for (i in fruits.indices) {
            val f = fruits[i]
            // اطلاعات هر میوه را در ردیف مخصوص خودش (i * 5) می‌چینیم
            floatBuffer.put(i * 5 + 0, f.x)        // موقعیت افقی
            floatBuffer.put(i * 5 + 1, f.y)        // موقعیت عمودی
            floatBuffer.put(i * 5 + 2, f.velX)     // سرعت افقی
            floatBuffer.put(i * 5 + 3, f.velY)     // سرعت عمودی
            floatBuffer.put(i * 5 + 4, f.rotation) // زاویه چرخش
        }

        // ۳. شلیک! آدرس حافظه را به موتور اسمبلی (NEON) می‌دهیم تا با سرعت نور محاسبات را انجام دهد
        processPhysicsNeonDirect(byteBuffer, fruits.size, gameSpeed, screenWidth)

        // ۴. حالا اسمبلی مختصات جدید را در همان حافظه نوشته است؛ ما آن‌ها را پس می‌گیریم
        for (i in fruits.indices) {
            val f = fruits[i]
            f.x = floatBuffer.get(i * 5 + 0)
            f.y = floatBuffer.get(i * 5 + 1)
            f.velX = floatBuffer.get(i * 5 + 2)
            f.velY = floatBuffer.get(i * 5 + 3)
            f.rotation = floatBuffer.get(i * 5 + 4)
        }
        // با این کار، میوه‌ها در صفحه حرکت می‌کنند بدون اینکه میلی‌ثانیه‌ای تاخیر ایجاد شود
    }
    fun updateBufferFromList() {
        // ۱. پاکسازی نشانگر بافر
        // این دستور دیتای قبلی را پاک نمی‌کند، بلکه فقط "نشانگر" (Pointer) را به ابتدای حافظه می‌برد.
        // مثل این است که به ابتدای یک نوار کاست برگردی تا آماده ضبط کردن (نوشتن) شوی.
        byteBuffer.clear()

        // ۲. حلقه برای ریختن دیتای تمام میوه‌ها در بافر
        // به ازای هر میوه‌ای که در حال حاضر در بازی وجود دارد (مثلاً ۳ تا میوه):
        fruits.forEach { fruit ->
            // اطلاعات را با فرمت Float پشت سر هم در حافظه خام می‌چسبانیم
            byteBuffer.putFloat(fruit.x)        // اول مختصات X
            byteBuffer.putFloat(fruit.y)        // بعد مختصات Y
            byteBuffer.putFloat(fruit.velX)     // بعد سرعت افقی
            byteBuffer.putFloat(fruit.velY)     // بعد سرعت عمودی

            // در نهایت یک وضعیت (IsHalf) را می‌فرستیم.
            // چون اسمبلی فقط عدد می‌فهمد، True را به 1f و False را به 0f تبدیل می‌کنیم.
            byteBuffer.putFloat(if (fruit.isHalf) 1f else 0f)
        }

        // ۳. آماده‌سازی برای خواندن (Flip)
        // این خط بسیار حیاتی است! وقتی نوشتن تمام شد، flip نشانگر را دوباره به اول برمی‌گرداند
        // و محدودیت (Limit) را روی آخرین جایی که نوشتیم تنظیم می‌کند.
        // با این کار اسمبلی می‌فهمد دقیقاً تا کجا باید اطلاعات را بخواند و از مرز رد نشود.
        byteBuffer.flip()
    }
}

fun spawnFruits(fruits: SnapshotStateList<FruitState>, fruitTypes: List<FruitType>, screenWidth: Float) {
    // یک عدد تصادفی بین ۱ تا ۱۰۰ برای تعیین "شانس" (مثل قرعه‌کشی)
    val chance = Random.nextInt(1, 101)

    // تعیین تعداد میوه‌هایی که همزمان پرتاب می‌شوند (بر اساس شانس)
    val count = when {
        chance <= 15 -> 1      // ۱۵٪ احتمال تک میوه (آسان)
        chance <= 40 -> 2      // ۲۵٪ احتمال دو میوه
        chance <= 70 -> 3      // ۳۰٪ احتمال سه میوه
        else -> 4              // ۳۰٪ احتمال چهار میوه همزمان (سخت و هیجانی)
    }

    // به تعداد تعیین شده، میوه می‌سازیم و به لیست اضافه می‌کنیم
    repeat(count) {
        // یک نوع میوه (سیب، طالبی و...) را به صورت تصادفی انتخاب کن
        val randomType = fruitTypes.random()

        fruits.add(FruitState(
            // نقطه شروع X: میوه‌ها را وسط صفحه متمرکز می‌کنیم (۲۰٪ تا ۸۰٪ عرض صفحه) که به لبه‌ها نچسبند
            initialX = (screenWidth * 0.2f) + Random.nextFloat() * (screenWidth * 0.6f),

            // نقطه شروع Y: پایین‌تر از لبه پایین صفحه (خارج از دید کاربر)
            initialY = 2300f,

            // سرعت پرتاب به سمت بالا (منفی یعنی رو به بالا): عددی بین -90 تا -115
            velY = -90f - (Random.nextFloat() * 25f),

            // سرعت افقی تصادفی: برای اینکه میوه‌ها کمی به چپ یا راست منحرف شوند
            velX = (Random.nextFloat() - 0.5f) * 14f,

            // تصاویر مربوط به این نوع میوه (کامل، نیمه چپ، نیمه راست)
            image = randomType.whole,
            leftImage = randomType.left,
            rightImage = randomType.right,

            // زاویه چرخش اولیه تصادفی برای طبیعی‌تر شدن
            initialRotation = Random.nextFloat() * 360f
        ))
    }
}


suspend fun PointerInputScope.handleTouchInput(
    engine: GameEngine,
    findHitFruitIndexDirect: (ByteBuffer, Int, Float, Float, Float) -> Int, // تابع کمکی اسمبلی
    trailPoints: MutableList<Offset>, // لیستی برای ذخیره مسیر حرکت انگشت
    onHit: (FruitState) -> Unit // واکنشی که باید بعد از برخورد نشان دهیم
) {
    awaitPointerEventScope { // شروع گوش دادن به رویدادهای لمس صفحه
        while (true) {
            val event = awaitPointerEvent() // منتظر بمان تا کاربر صفحه را لمس کند
            event.changes.forEach { change ->
                if (change.pressed) { // اگر انگشت روی صفحه فشار داده شده (در حال کشیدن)
                    val touchPos = change.position // مختصات دقیق انگشت (X, Y) را بگیر
                    trailPoints.add(touchPos) // این نقطه را به لیست "رد شمشیر" اضافه کن

                    if (engine.fruits.isNotEmpty()) {
                        // --- شروع حلقه پاکسازی (اگر چند میوه روی هم بودند، همه را ببرد) ---
                        while (true) {
                            // از اسمبلی بپرس: "آیا در این مختصات لمس، میوه‌ای وجود دارد؟"
                            val hitIndex = findHitFruitIndexDirect(
                                engine.getBuffer(), // بافر مستقیم اطلاعات میوه‌ها
                                engine.fruits.size, // تعداد کل میوه‌ها
                                touchPos.x,
                                touchPos.y,
                                120f // شعاع (Radius) حساسیت شمشیر (هر چه بیشتر باشد، بریدن آسان‌تر است)
                            )

                            // اگر اسمبلی عددی غیر از -1 برگرداند، یعنی یک میوه پیدا شد!
                            if (hitIndex != -1 && hitIndex < engine.fruits.size) {
                                val f = engine.fruits[hitIndex]

                                if (!f.isHalf) { // فقط میوه‌های "کامل" را ببر (نیمه‌ها دوباره بریده نمی‌شوند)
                                    onHit(f) // صدای بریدن یا لرزش را اجرا کن
                                    engine.fruits.removeAt(hitIndex) // میوه کامل را از لیست حذف کن

                                    // !!! خیلی مهم: حالا که لیست عوض شد، باید بافر را سریع آپدیت کنیم
                                    // تا اسمبلی در دور بعدیِ همین حلقه، میوه حذف شده را دوباره نبیند
                                    engine.updateBufferFromList()
                                } else {
                                    break // اگر میوه از قبل نصف بود، بقیه حلقه را رها کن
                                }
                            } else {
                                break // اگر هیچ میوه‌ای پیدا نشد، از حلقه جستجو خارج شو
                            }
                        }
                    }
                } else {
                    // اگر کاربر انگشتش را از روی صفحه برداشت
                    trailPoints.clear() // رد شمشیر را پاک کن تا خط سفید غیب شود
                }
            }
        }
    }
}
fun handleFruitSplit(
    fruits: SnapshotStateList<FruitState>, // لیست اصلی میوه‌ها برای اضافه کردن نیمه‌ها
    f: FruitState, // میوه‌ای که همین الان بریده شد
    getSplitPhysics: (Float, Float) -> FloatArray // تابع اسمبلی برای محاسبه سرعت انفجار
) {
    // ۱. از اسمبلی می‌خواهیم سرعت‌های جدید را بر اساس فیزیک محاسباتی به ما بدهد
    // ما سرعت فعلی میوه (vx, vy) را می‌دهیم و او ۳ سرعت جدید (چپ، راست و پرتاب عمودی) برمی‌گرداند
    val splitResults = getSplitPhysics(f.velX, f.velY)

    val newVelX_Left = splitResults[0]  // سرعت پرتاب شدن نیمه چپ به سمت بیرون
    val newVelX_Right = splitResults[1] // سرعت پرتاب شدن نیمه راست به سمت بیرون
    val newVelY = splitResults[2]       // سرعت پرتاب رو به بالای هر دو نیمه

    // ۲. ایجاد و اضافه کردن نیمه چپ به بازی
    fruits.add(FruitState(
        initialX = f.x - 10f,    // کمی فاصله به چپ نسبت به مرکز میوه اصلی
        initialY = f.y,
        velY = newVelY,          // سرعتی که اسمبلی حساب کرده (معمولاً کمی رو به بالاست)
        velX = newVelX_Left,     // پرتاب به سمت چپ
        image = f.leftImage!!,   // تصویر نیمه چپ میوه
        isHalf = true,           // علامت‌گذاری به عنوان نیمه (که امتیاز دوباره ندهد)
        initialRotation = f.rotation // شروع چرخش از همان زاویه‌ای که میوه اصلی بود
    ))

    // ۳. ایجاد و اضافه کردن نیمه راست به بازی
    fruits.add(FruitState(
        initialX = f.x + 10f,    // کمی فاصله به راست نسبت به مرکز میوه اصلی
        initialY = f.y,
        velY = newVelY,
        velX = newVelX_Right,    // پرتاب به سمت راست
        image = f.rightImage!!,  // تصویر نیمه راست میوه
        isHalf = true,
        initialRotation = f.rotation
    ))
}