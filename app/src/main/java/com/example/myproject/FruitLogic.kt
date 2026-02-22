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

        // حذف میوه‌های خارج شده
        fruits.removeAll { it.y > 2600f }
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
    engine: GameEngine, // به جای لیست میوه‌ها، کل انجین را می‌فرستیم تا به بافر دسترسی داشته باشیم
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
                        // نکته طلایی: دیتای میوه‌ها همین الان در engine.byteBuffer (توسط updatePhysics) آپدیت شده است
                        // پس نیازی به هیچ For لوپی برای کپی کردن مجدد نیست!

                        val hitIndex = findHitFruitIndexDirect(
                            engine.getBuffer(), // بافر مستقیم انجین
                            engine.fruits.size,
                            touchPos.x,
                            touchPos.y,
                            120f // شعاع برخورد
                        )

                        if (hitIndex != -1 && hitIndex < engine.fruits.size) {
                            val f = engine.fruits[hitIndex]
                            if (!f.isHalf) {
                                onHit(f)
                                engine.fruits.removeAt(hitIndex)
                            }
                        }
                    }
                } else {
                    trailPoints.clear()
                }
            }
        }
    }
}

// ۳. تابع مدیریت دو نیم شدن میوه با افکت فیزیکی
fun handleFruitSplit(fruits: SnapshotStateList<FruitState>, f: FruitState) {
    // ارسال f.rotation به نیمه‌ها
    fruits.add(FruitState(f.x, f.y, f.velY, f.velX - 12f, f.leftImage!!, isHalf = true, initialRotation = f.rotation))
    fruits.add(FruitState(f.x, f.y, f.velY, f.velX + 12f, f.rightImage!!, isHalf = true, initialRotation = f.rotation))
}