package com.example.myproject

import ScoreDisplay
import SpeedControlSlider
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.unit.dp
import drawFruit
import java.nio.ByteBuffer

// فرض بر این است که این توابع در فایل‌های FruitComponents.kt و FruitLogic.kt هستند
// اگر در همان پکیج باشند نیازی به Import دستی نیست، در غیر این صورت Import کنید.

@Composable
fun FruitNinjaGame(
    processPhysicsNeonDirect: (ByteBuffer, Int, Float, Float) -> Unit, // تغییر FloatArray به ByteBuffer
    findHitFruitIndexDirect: (ByteBuffer, Int, Float, Float, Float) -> Int, // (این یکی را بعداً درست می‌کنیم)
    getSplitPhysics: (Float, Float) -> FloatArray,
    fruitTypes: List<FruitType>,
    screenWidth: Float
) {
    // تعریف Stateها
    val fruits = remember { mutableStateListOf<FruitState>() }
    val trailPoints = remember { mutableStateListOf<Offset>() }
    var score by remember { mutableIntStateOf(0) }

    // مقداردهی Engine برای مدیریت فیزیک اسمبلی
    val engine = remember { GameEngine(fruits, screenWidth, processPhysicsNeonDirect) }

    // ۱. Spawner: تولید میوه‌ها با استفاده از تابع کمکی در FruitLogic
    LaunchedEffect(Unit) {
        while (true) {
            spawnFruits(fruits, fruitTypes, screenWidth)
            val baseDelay = Random.nextLong(1200, 2000)


            delay(baseDelay)
        }
    }

    // ۲. Game Loop: اجرای فیزیک NEON در هر فریم
    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos {
                engine.updatePhysics()
            }
        }
    }

    // ۳. لایه نمایش و تعامل
    Box(modifier = Modifier.fillMaxSize()) {
        // نمایش امتیاز (تعریف شده در FruitComponents)
        ScoreDisplay(score)

        Canvas(modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                // مدیریت لمس و تشخیص برخورد اسمبلی (تعریف شده در FruitLogic)
                handleTouchInput(engine, findHitFruitIndexDirect, trailPoints) { hitFruit: FruitState ->
                    score += 10
                    handleFruitSplit(fruits, hitFruit, getSplitPhysics)
                }
            }
        ) {
            // رسم تک تک میوه‌ها (تعریف شده در FruitComponents)
            fruits.forEach { fruit ->
                drawFruit(fruit)
            }

            // رسم رد شمشیر (تعریف شده در FruitComponents)
            drawBladeTrail(trailPoints)
        }
        SpeedControlSlider(
            currentSpeed = engine.gameSpeed,
            onSpeedChange = { newSpeed -> engine.gameSpeed = newSpeed }
        )
    }
}