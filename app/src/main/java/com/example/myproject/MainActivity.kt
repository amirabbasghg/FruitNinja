package com.example.myproject

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight

class MainActivity : ComponentActivity() {

    external fun processPhysicsNeon(data: FloatArray, count: Int, gameSpeed: Float, screenWeight: Float)
    external fun findHitFruitIndex(data: FloatArray,count: Int ,tx: Float, ty: Float, r: Float): Int

    companion object {
        init { System.loadLibrary("myproject") }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // بارگذاری میوه‌های مختلف
            val fruitTypes = remember {
                listOf(
                    FruitType(
                        whole = BitmapFactory.decodeResource(resources, R.drawable.apple_whole).asImageBitmap(),
                        left = BitmapFactory.decodeResource(resources, R.drawable.apple_left).asImageBitmap(),
                        right = BitmapFactory.decodeResource(resources, R.drawable.apple_right).asImageBitmap()
                    ),
                    FruitType(
                        whole = BitmapFactory.decodeResource(resources, R.drawable.melon_whole).asImageBitmap(),
                        left = BitmapFactory.decodeResource(resources, R.drawable.melon_left).asImageBitmap(),
                        right = BitmapFactory.decodeResource(resources, R.drawable.melon_right).asImageBitmap()
                    ),
                    // هر میوه دیگری که داری را اینجا اضافه کن
                )
            }

            val screenWidth = resources.displayMetrics.widthPixels.toFloat()

            Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF2B1B17)) {
                // ارسال لیست کامل میوه‌ها به بازی
                FruitNinjaGame(::processPhysicsNeon, ::findHitFruitIndex, fruitTypes, screenWidth)
            }
        }
    }
}