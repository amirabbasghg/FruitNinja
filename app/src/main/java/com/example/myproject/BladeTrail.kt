package com.example.myproject

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas

fun DrawScope.drawBladeTrail(trailPoints: List<Offset>) {
    if (trailPoints.size > 2) {
        val trailPath = Path().apply {
            moveTo(trailPoints.first().x, trailPoints.first().y)
            for (i in 1 until trailPoints.size) {
                lineTo(trailPoints[i].x, trailPoints[i].y)
            }
        }

        drawContext.canvas.nativeCanvas.apply {
            val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.WHITE
                style = android.graphics.Paint.Style.STROKE
                strokeWidth = 15f
                strokeCap = android.graphics.Paint.Cap.ROUND
                setShadowLayer(20f, 0f, 0f, android.graphics.Color.CYAN)
                isAntiAlias = true
            }
            drawPath(trailPath.asAndroidPath(), paint)

            paint.apply {
                strokeWidth = 6f
                setShadowLayer(0f, 0f, 0f, 0)
            }
            drawPath(trailPath.asAndroidPath(), paint)
        }
    }
}