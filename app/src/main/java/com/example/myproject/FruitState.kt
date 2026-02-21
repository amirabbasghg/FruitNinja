package com.example.myproject

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.ImageBitmap
import kotlin.random.Random

class FruitState(
    initialX: Float,
    initialY: Float,
    var velY: Float,
    var velX: Float,
    val image: ImageBitmap,
    val leftImage: ImageBitmap? = null,
    val rightImage: ImageBitmap? = null,
    initialRotation: Float = 0f,
    val rotationSpeed: Float = (Random.nextFloat() - 0.5f) * 12f,
    val isHalf: Boolean = false
) {
    var x by mutableFloatStateOf(initialX)
    var y by mutableFloatStateOf(initialY)
    var rotation by mutableFloatStateOf(initialRotation)
}

data class FruitType(val whole: ImageBitmap, val left: ImageBitmap, val right: ImageBitmap)