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
import java.nio.ByteBuffer

class MainActivity : ComponentActivity() {

    // --- بخش توابع Native (اتصال به لایه اسمبلی و C++) ---

    // چرا از Direct ByteBuffer استفاده می‌کنیم؟
    // 1. دسترسی مستقیم: بافر در حافظه Native (خارج از JVM) ساخته می‌شود.
    // 2. حذف کپی (Zero-Copy): اسمبلی و کاتلین هر دو روی یک آدرس واحد از RAM کار می‌کنند.
    // 3. سرعت بالا: انتقال داده‌ها بین کاتلین و اسمبلی با تاخیر صفر انجام می‌شود.
    // 4. ترتیب بایت‌ها: حتماً باید Little-Endian باشد تا CPU درست اعداد را بخواند.

    // تابع اصلی برای به‌روزرسانی فیزیک تمام میوه‌ها به صورت موازی (NEON)
    external fun processPhysicsNeonDirect(
        buffer: ByteBuffer, // بافر حاوی داده‌های X, Y, VX, VY و Rotation
        count: Int,         // تعداد کل میوه‌های فعال
        gameSpeed: Float,   // ضریب سرعت بازی (Multiplier)
        screenWidth: Float  // عرض صفحه برای محاسبه برخورد با دیوارها
    )

    // تابع برای تشخیص اینکه آیا کاربر یک میوه خاص را لمس (برش) کرده است یا خیر
    external fun findHitFruitIndexDirect(
        buffer: ByteBuffer, // بافر داده‌ها
        count: Int,         // تعداد میوه‌ها
        tx: Float,          // مختصات X لمس کاربر
        ty: Float,          // مختصات Y لمس کاربر
        r: Float            // شعاع برخورد (Radius)
    ): Int                  // خروجی: ایندکس میوه برخورد شده (یا 1- اگر برخورد نشد)

    // تابع کمکی برای محاسبه سرعت نیمه‌های میوه پس از برش
    external fun getSplitPhysics(vx: Float, vy: Float): FloatArray

    // تابع برای چک کردن اینکه کدام میوه از پایین صفحه سقوط کرده (برای کسر جان)
    external fun findFallenFruitIndexDirect(
        buffer: ByteBuffer, // بافر داده‌ها
        count: Int,         // تعداد میوه‌ها
        deathLine: Float    // خط مرگ (مختصات Y انتهای صفحه)
    ): Int

    // --- بارگذاری کتابخانه Native در هنگام شروع برنامه ---
    companion object {
        init {
            // بارگذاری فایل .so که شامل کدهای اسمبلی بهینه شده است
            System.loadLibrary("myproject")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // شروع تنظیمات رابط کاربری با Jetpack Compose
        setContent {

            // استفاده از remember برای لود کردن تصاویر فقط برای یک بار (بهبود پرفورمنس)
            val fruitTypes = remember {
                listOf(
                    // مدل سیب: شامل تصویر کامل و دو نیمه برای زمان برش
                    FruitType(
                        whole = BitmapFactory.decodeResource(resources, R.drawable.apple_whole)
                            .asImageBitmap(),
                        left = BitmapFactory.decodeResource(resources, R.drawable.apple_left)
                            .asImageBitmap(),
                        right = BitmapFactory.decodeResource(resources, R.drawable.apple_right)
                            .asImageBitmap()
                    ),
                    // مدل طالبی: تصاویر مربوط به طالبی
                    FruitType(
                        whole = BitmapFactory.decodeResource(resources, R.drawable.melon_whole)
                            .asImageBitmap(),
                        left = BitmapFactory.decodeResource(resources, R.drawable.melon_left)
                            .asImageBitmap(),
                        right = BitmapFactory.decodeResource(resources, R.drawable.melon_right)
                            .asImageBitmap()
                    ),
                    // مدل کیوی: تصاویر مربوط به کیوی
                    FruitType(
                        whole = BitmapFactory.decodeResource(resources, R.drawable.kiwi_whole)
                            .asImageBitmap(),
                        left = BitmapFactory.decodeResource(resources, R.drawable.kiwi_left)
                            .asImageBitmap(),
                        right = BitmapFactory.decodeResource(resources, R.drawable.kiwi_right)
                            .asImageBitmap()
                    ),
                    // مدل توت فرنگی: تصاویر مربوط به توت فرنگی
                    FruitType(
                        whole = BitmapFactory.decodeResource(resources, R.drawable.strawberry_whole)
                            .asImageBitmap(),
                        left = BitmapFactory.decodeResource(resources, R.drawable.strawberry_left)
                            .asImageBitmap(),
                        right = BitmapFactory.decodeResource(resources, R.drawable.strawberry_right)
                            .asImageBitmap()
                    ),
                )
            }

            // محاسبه عرض واقعی صفحه نمایش به پیکسل برای ارسال به موتور فیزیک
            val screenWidth = resources.displayMetrics.widthPixels.toFloat()

            // ایجاد لایه پس‌زمینه بازی
            Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF2B1B17)) {

                // فراخوانی کامپوننت اصلی بازی و تزریق توابع اسمبلی به آن
                FruitNinjaGame(
                    ::processPhysicsNeonDirect,   // پاس دادن رفرنس تابع فیزیک
                    ::findHitFruitIndexDirect,    // پاس دادن رفرنس تابع تشخیص برش
                    ::getSplitPhysics,             // پاس دادن رفرنس تابع تقسیم میوه
                    ::findFallenFruitIndexDirect, // پاس دادن رفرنس تابع سقوط
                    fruitTypes,                   // تصاویر بارگذاری شده
                    screenWidth                   // عرض صفحه
                )
            }
        }
    }
}