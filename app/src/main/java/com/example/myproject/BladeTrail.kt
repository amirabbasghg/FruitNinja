package com.example.myproject

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas

fun DrawScope.drawBladeTrail(trailPoints: List<Offset>) {
    // ۱. بررسی تعداد نقاط: حداقل ۳ نقطه لازم داریم تا بتوانیم یک خط معنادار رسم کنیم
    if (trailPoints.size > 2) {

        // ۲. ساخت یک "مسیر" (Path): مسیری که از نقاط لمس شده توسط کاربر عبور می‌کند
        val trailPath = Path().apply {
            // رفتن به اولین نقطه‌ای که کاربر لمس کرده
            moveTo(trailPoints.first().x, trailPoints.first().y)

            // کشیدن خط از نقطه فعلی به نقاط بعدی به ترتیب (اتصال نقاط به هم)
            for (i in 1 until trailPoints.size) {
                lineTo(trailPoints[i].x, trailPoints[i].y)
            }
        }

        // ۳. ورود به لایه Native (اصلی) اندروید برای دسترسی به تنظیمات پیشرفته گرافیکی
        drawContext.canvas.nativeCanvas.apply {

            // ۴. ساخت یک قلم (Paint) برای لایه بیرونی و درخشانِ شمشیر
            val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.WHITE // رنگ اصلی خط سفید باشد
                style = android.graphics.Paint.Style.STROKE // فقط دور خط (ضخامت) رسم شود، نه داخل آن
                strokeWidth = 15f // ضخامت خط بیرونی (۱۵ پیکسل)
                strokeCap = android.graphics.Paint.Cap.ROUND // نوک خط‌ها گرد باشد تا نرم به نظر برسد

                // ایجاد افکت نئونی (Glow): یک سایه درخشان فیروزه‌ای دور خط سفید
                // (شعاع سایه ۲۰، جابجایی X و Y صفر، رنگ فیروزه‌ای)
                setShadowLayer(20f, 0f, 0f, android.graphics.Color.CYAN)

                isAntiAlias = true // فعال‌سازی لبه‌های نرم (برای جلوگیری از شطرنجی شدن خط)
            }

            // ۵. رسم مسیر اول: خط ضخیم با درخشش فیروزه‌ای (این لایه زیرین است)
            drawPath(trailPath.asAndroidPath(), paint)

            // ۶. تغییر تنظیمات قلم برای لایه داخلی و تیزِ شمشیر
            paint.apply {
                strokeWidth = 6f // ضخامت کمتر (۶ پیکسل) برای ایجاد مرکز تیز و براق
                setShadowLayer(0f, 0f, 0f, 0) // حذف سایه برای این لایه (تا مرکز خط کاملاً سفید و تخت باشد)
            }

            // ۷. رسم مسیر دوم: یک خط باریک‌تر و کاملاً سفید روی خط قبلی
            // این ترکیب (خط باریک روی خط پهن سایه‌دار) باعث ایجاد حس درخشش نئونی می‌شود
            drawPath(trailPath.asAndroidPath(), paint)
        }
    }
}