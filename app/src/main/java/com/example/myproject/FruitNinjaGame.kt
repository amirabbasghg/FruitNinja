package com.example.myproject

import ScoreDisplay
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
import drawFruit
import java.nio.ByteBuffer

// فرض بر این است که این توابع در فایل‌های FruitComponents.kt و FruitLogic.kt هستند
// اگر در همان پکیج باشند نیازی به Import دستی نیست، در غیر این صورت Import کنید.

// در فایل FruitNinjaGame.kt

@Composable
fun FruitNinjaGame(
    processPhysicsNeonDirect: (ByteBuffer, Int, Float, Float) -> Unit,
    findHitFruitIndexDirect: (ByteBuffer, Int, Float, Float, Float) -> Int,
    getSplitPhysics: (Float, Float) -> FloatArray,
    fruitTypes: List<FruitType>,
    screenWidth: Float
) {
    val fruits = remember { mutableStateListOf<FruitState>() }
    val trailPoints = remember { mutableStateListOf<Offset>() }
    var score by remember { mutableIntStateOf(0) }

    // --- متغیرهای جدید ---
    var lives by remember { mutableIntStateOf(3) }
    var isGameOver by remember { mutableStateOf(false) }

    val engine = remember { GameEngine(fruits, screenWidth, processPhysicsNeonDirect) }

    // ۱. Spawner (فقط اگر بازی تمام نشده باشد تولید کند)
    LaunchedEffect(isGameOver, engine.gameSpeed) {
        while (!isGameOver) {
            spawnFruits(fruits, fruitTypes, screenWidth)
            val baseDelay = Random.nextLong(1200, 2000)
            delay((baseDelay / engine.gameSpeed).toLong())
        }
    }

    // ۲. Game Loop و چک کردن سقوط میوه
    LaunchedEffect(isGameOver) {
        while (!isGameOver) {
            withFrameNanos {
                engine.updatePhysics()

                // چک کردن میوه‌هایی که از پایین صفحه خارج شده‌اند
                val iterator = fruits.iterator()
                while (iterator.hasNext()) {
                    val fruit = iterator.next()
                    // اگر میوه از ارتفاع ۲۵۰۰ (پایین صفحه) رد شد
                    if (fruit.y > 2500f) {
                        // فقط اگر میوه سالم باشد (دونیم نشده باشد) جان کم می‌شود
                        if (!fruit.isHalf) {
                            lives -= 1
                            if (lives <= 0) isGameOver = true
                        }
                        iterator.remove() // حذف میوه از لیست
                    }
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // نمایش امتیاز و جان
        Column(modifier = Modifier.padding(25.dp)) {
            ScoreDisplay(score)
            Text("Lives: ${"❤️".repeat(lives)}", fontSize = 24.sp, color = Color.Red)
        }

        Canvas(modifier = Modifier
            .fillMaxSize()
            .pointerInput(isGameOver) { // اگر بازی تمام شد ورودی لمس غیرفعال شود
                if (isGameOver) return@pointerInput
                handleTouchInput(engine, findHitFruitIndexDirect, trailPoints) { hitFruit ->
                    score += 10
                    handleFruitSplit(fruits, hitFruit, getSplitPhysics)
                }
            }
        ) {
            fruits.forEach { drawFruit(it) }
            drawBladeTrail(trailPoints)
        }

        // --- صفحه باخت (Overlay) ---
        if (isGameOver) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("GAME OVER", fontSize = 48.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                    Text("Score: $score", fontSize = 32.sp, color = Color.White)
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(onClick = {
                        // ریست کردن بازی
                        lives = 3
                        score = 0
                        fruits.clear()
                        isGameOver = false
                    }) {
                        Text("Try Again")
                    }
                }
            }
        }

        if (!isGameOver) {
            SpeedControlSlider(
                currentSpeed = engine.gameSpeed,
                onSpeedChange = { engine.gameSpeed = it }
            )
        }
    }
}