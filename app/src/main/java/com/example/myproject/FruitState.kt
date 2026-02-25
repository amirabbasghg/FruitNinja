package com.example.myproject

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.ImageBitmap
import kotlin.random.Random

class FruitState(
    initialX: Float,             // موقعیت افقی اولیه (لحظه تولد میوه)
    initialY: Float,             // موقعیت عمودی اولیه (معمولاً پایین صفحه)
    var velY: Float,             // سرعت حرکت در راستای Y (مثبت یعنی سقوط، منفی یعنی پرتاب به بالا)
    var velX: Float,             // سرعت حرکت در راستای X (برای حرکت مورب)
    val image: ImageBitmap,      // عکس کامل میوه برای نمایش روی بوم
    val leftImage: ImageBitmap? = null,  // عکس نیمه چپ (اگر میوه بریده شود - اختیاری)
    val rightImage: ImageBitmap? = null, // عکس نیمه راست (اگر میوه بریده شود - اختیاری)
    initialRotation: Float = 0f, // زاویه چرخش اولیه میوه (مثلاً ۴۵ درجه کج)

    // سرعت چرخش میوه به دور خودش؛ یک عدد تصادفی بین -6 تا +6 درجه در هر فریم
    val rotationSpeed: Float = (Random.nextFloat() - 0.5f) * 12f,

    val isHalf: Boolean = false  // آیا این یک میوه کامل است یا یک تکه بریده شده؟
) {
    // --- بخش متغیرهای هوشمند (State) ---
    // این سه متغیر با mutableFloatStateOf تعریف شده‌اند تا به محض اینکه
    // موتور فیزیک (اسمبلی) مقدار آن‌ها را تغییر داد، Compose بفهمد و میوه را در جای جدید رسم کند.

    var x by mutableFloatStateOf(initialX)      // مختصات X فعلی
    var y by mutableFloatStateOf(initialY)      // مختصات Y فعلی
    var rotation by mutableFloatStateOf(initialRotation) // زاویه چرخش فعلی
}
// این کلاس مثل یک "پوشه" عمل می‌کند که عکس‌های مربوط به یک نوع میوه (مثلاً هندوانه) را کنار هم نگه می‌دارد
data class FruitType(
    val whole: ImageBitmap, // تصویر هندوانه کامل
    val left: ImageBitmap,  // تصویر نیمه چپ هندوانه
    val right: ImageBitmap  // تصویر نیمه راست هندوانه
)