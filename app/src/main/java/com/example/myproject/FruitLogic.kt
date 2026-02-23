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
    val fruits: SnapshotStateList<FruitState>,
    val screenWidth: Float,
    // ورودی تابع به ByteBuffer تغییر کرد
    val processPhysicsNeonDirect: (ByteBuffer, Int, Float, Float) -> Unit
) {
    var gameSpeed by mutableFloatStateOf(1.0f)

    // رزرو حافظه مستقیم (خارج از JVM). فرض می‌کنیم نهایتاً 100 میوه داریم.
    // 100 میوه * 5 پارامتر * 4 بایت (حجم هر Float) = 2000 بایت حافظه خام
    private val maxFruits = 100
    private val byteBuffer = ByteBuffer.allocateDirect(maxFruits * 5 * 4).order(ByteOrder.nativeOrder())
    private val floatBuffer = byteBuffer.asFloatBuffer()
    fun getBuffer(): ByteBuffer = byteBuffer
    fun updateBufferFromList() {
        // ۱. ابتدا نشانگر بافر را به نقطه شروع (صفر) برمی‌گردانیم
        byteBuffer.clear()

        // ۲. تمام میوه‌های باقی‌مانده در لیست را دوباره توی بافر می‌ریزیم
        fruits.forEach { fruit ->
            byteBuffer.putFloat(fruit.x)
            byteBuffer.putFloat(fruit.y)
            byteBuffer.putFloat(fruit.velX)
            byteBuffer.putFloat(fruit.velY)
            byteBuffer.putFloat(if (fruit.isHalf) 1f else 0f) // یا هر دیتای دیگری که داری
        }

        // ۳. موقعیت بافر را برای خواندن توسط اسمبلی آماده می‌کنیم
        byteBuffer.flip()
    }

    fun updatePhysics() {
        if (fruits.isEmpty()) return

        // ۱. ریست کردن نشانگر بافر
        floatBuffer.position(0)

        // ۲. نوشتن دیتای کاتلین در حافظه خام (بدون ساخت آرایه جدید)
        for (i in fruits.indices) {
            val f = fruits[i]
            floatBuffer.put(i * 5 + 0, f.x)
            floatBuffer.put(i * 5 + 1, f.y)
            floatBuffer.put(i * 5 + 2, f.velX)
            floatBuffer.put(i * 5 + 3, f.velY)
            floatBuffer.put(i * 5 + 4, f.rotation)
        }

        // ۳. شلیک کردن آدرس حافظه به C++ و Assembly
        processPhysicsNeonDirect(byteBuffer, fruits.size, gameSpeed, screenWidth)

        // ۴. خواندن مستقیم نتایج اسمبلی از همان حافظه
        for (i in fruits.indices) {
            val f = fruits[i]
            f.x = floatBuffer.get(i * 5 + 0)
            f.y = floatBuffer.get(i * 5 + 1)
            f.velX = floatBuffer.get(i * 5 + 2)
            f.velY = floatBuffer.get(i * 5 + 3)
            f.rotation = floatBuffer.get(i * 5 + 4)
        }

    }
}
//    private fun applyWallBounce(f: FruitState) {
//        val margin = 80f // حاشیه امنیت دیواره‌ها
//
//        if (f.x < margin) {
//            f.x = margin
//            f.velX = Math.abs(f.velX) * 0.7f // معکوس کردن سرعت به سمت راست
//        } else if (f.x > screenWidth - margin) {
//            f.x = screenWidth - margin
//            f.velX = -Math.abs(f.velX) * 0.7f // معکوس کردن سرعت به سمت چپ
//        }
//    }
//}

// ۱. تابع تولید میوه (اصلاح شده برای پرتاب عمودی‌تر)
// در FruitLogic.kt تابع spawnFruits را اینگونه اصلاح کن:
fun spawnFruits(fruits: SnapshotStateList<FruitState>, fruitTypes: List<FruitType>, screenWidth: Float) {
    repeat(if (Random.nextInt(100) < 70) 2 else 3) {
        val randomType = fruitTypes.random()
        fruits.add(FruitState(
            initialX = (screenWidth * 0.2f) + Random.nextFloat() * (screenWidth * 0.6f),
            initialY = 2300f,
            velY = -90f - (Random.nextFloat() * 25f),
            velX = (Random.nextFloat() - 0.5f) * 14f,
            image = randomType.whole,
            leftImage = randomType.left,
            rightImage = randomType.right,
            // شروع با یک زاویه تصادفی که بازی طبیعی‌تر شود
            initialRotation = Random.nextFloat() * 360f
        ))
    }
}

// ۲. مدیریت لمس و تشخیص برخورد
// در فایل FruitLogic.kt

suspend fun PointerInputScope.handleTouchInput(
    engine: GameEngine,
    findHitFruitIndexDirect: (ByteBuffer, Int, Float, Float, Float) -> Int,
    trailPoints: MutableList<Offset>,
    onHit: (FruitState) -> Unit
) {
    awaitPointerEventScope {
        while (true) {
            val event = awaitPointerEvent()
            event.changes.forEach { change ->
                if (change.pressed) {
                    val touchPos = change.position
                    trailPoints.add(touchPos)

                    if (engine.fruits.isNotEmpty()) {
                        // --- شروع حلقه پاکسازی (Multi-Hit Cleaning) ---
                        // این حلقه تا زمانی که اسمبلی میوه‌ای زیر انگشت پیدا کنه، تکرار میشه
                        while (true) {
                            val hitIndex = findHitFruitIndexDirect(
                                engine.getBuffer(), // بافر مستقیم که اسمبلی روش اسکن می‌کنه
                                engine.fruits.size,
                                touchPos.x,
                                touchPos.y,
                                120f // شعاع برخورد
                            )

                            // اگر اسمبلی -1 برگردونه، یعنی دیگه هیچ میوه‌ای زیر انگشت نیست
                            if (hitIndex != -1 && hitIndex < engine.fruits.size) {
                                val f = engine.fruits[hitIndex]

                                if (!f.isHalf) {
                                    onHit(f) // اجرای افکت انفجار
                                    engine.fruits.removeAt(hitIndex) // حذف از لیست کاتلین

                                    // نکته حیاتی:
                                    // بعد از حذف از لیست، باید بافر رو آپدیت کنی تا اسمبلی در دور بعدی
                                    // بدونه که میوه حذف شده و دوباره همون رو پیدا نکنه (جلوگیری از Loop بی‌پایان)
                                    engine.updateBufferFromList()
                                } else {
                                    // اگر به هر دلیلی میوه نصف شده بود و هنوز در لیست بود، بشکن که گیر نکنی
                                    break
                                }
                            } else {
                                // هیچ میوه دیگه‌ای پیدا نشد، از حلقه while خارج شو
                                break
                            }
                        }
                        // --- پایان حلقه پاکسازی ---
                    }
                } else {
                    trailPoints.clear()
                }
            }
        }
    }
}

// ۳. تابع مدیریت دو نیم شدن میوه با افکت فیزیکی
// در فایل FruitLogic.kt

fun handleFruitSplit(
    fruits: SnapshotStateList<FruitState>,
    f: FruitState,
    getSplitPhysics: (Float, Float) -> FloatArray // دریافت تابع اسمبلی
) {
    // ۱. فراخوانی تابع اسمبلی برای محاسبه سرعت‌های جدید
    // اسمبلی بر اساس vx و vy فعلی، سرعت‌های انفجاری محاسبه می‌کند
    val splitResults = getSplitPhysics(f.velX, f.velY)

    val newVelX_Left = splitResults[0]
    val newVelX_Right = splitResults[1]
    val newVelY = splitResults[2]

    // ۲. ایجاد نیمه چپ با سرعت محاسبه شده در اسمبلی
    fruits.add(FruitState(
        initialX = f.x - 10f,
        initialY = f.y,
        velY = newVelY,
        velX = newVelX_Left,
        image = f.leftImage!!,
        isHalf = true,
        initialRotation = f.rotation
    ))

    // ۳. ایجاد نیمه راست با سرعت محاسبه شده در اسمبلی
    fruits.add(FruitState(
        initialX = f.x + 10f,
        initialY = f.y,
        velY = newVelY,
        velX = newVelX_Right,
        image = f.rightImage!!,
        isHalf = true,
        initialRotation = f.rotation
    ))
}