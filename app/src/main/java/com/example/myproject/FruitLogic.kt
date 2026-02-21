package com.example.myproject

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputScope
import kotlin.random.Random

class GameEngine(
    val fruits: SnapshotStateList<FruitState>,
    val screenWidth: Float,
    val processPhysicsNeon: (FloatArray, Int, Float, Float) -> Unit // اضافه شدن پارامتر چهارم
) {
    var gameSpeed by mutableFloatStateOf(1.0f)

    fun updatePhysics() {
        if (fruits.isEmpty()) return

        val data = FloatArray(fruits.size * 5)
        fruits.forEachIndexed { i, f ->
            data[i * 5 + 0] = f.x
            data[i * 5 + 1] = f.y
            data[i * 5 + 2] = f.velX
            data[i * 5 + 3] = f.velY
            data[i * 5 + 4] = f.rotation
        }

        // فراخوانی موتور تمام‌اسمبلی
        processPhysicsNeon(data, fruits.size, gameSpeed, screenWidth)

        for (i in fruits.indices) {
            val f = fruits[i]
            f.x = data[i * 5 + 0]
            f.y = data[i * 5 + 1]
            f.velX = data[i * 5 + 2]
            f.velY = data[i * 5 + 3]
            f.rotation = data[i * 5 + 4]
        }

        // تنها کاری که کاتلین انجام می‌دهد: مدیریت لیست
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
suspend fun PointerInputScope.handleTouchInput(
    fruits: SnapshotStateList<FruitState>,
    findHitFruitIndex: (FloatArray, Int, Float, Float, Float) -> Int,
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
                    if (trailPoints.size > 15) trailPoints.removeAt(0)

                    if (fruits.isNotEmpty()) {
                        val data = FloatArray(fruits.size * 5)
                        fruits.forEachIndexed { i, f ->
                            data[i * 5 + 0] = f.x
                            data[i * 5 + 1] = f.y
                        }

                        val hitIndex = findHitFruitIndex(data, fruits.size, touchPos.x, touchPos.y, 120f)

                        if (hitIndex != -1 && hitIndex < fruits.size) {
                            val f = fruits[hitIndex]
                            if (!f.isHalf) {
                                onHit(f)
                                // حذف میوه سالم بلافاصله بعد از برخورد
                                fruits.removeAt(hitIndex)
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