package com.example.criminalintent

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import android.os.Build
import android.text.Layout
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import kotlin.math.roundToInt


fun getScaledBitmap(path: String, destWidth: Int, destHeight: Int): Bitmap {
// Чтение размеров изображения на диске
    var options = BitmapFactory.Options()
    options.inJustDecodeBounds = true
    BitmapFactory.decodeFile(path, options)
    val srcWidth = options.outWidth.toFloat()
    val srcHeight = options.outHeight.toFloat()
// Выясняем, на сколько нужно уменьшить
    var inSampleSize = 1
    if (srcHeight > destHeight || srcWidth > destWidth) {
        val heightScale = srcHeight / destHeight
        val widthScale = srcWidth / destWidth
        val sampleScale = if (heightScale > widthScale) {
            heightScale
        } else {
            widthScale
        }
        inSampleSize = Math.round(sampleScale)
    }
    options = BitmapFactory.Options()
    options.inSampleSize = inSampleSize
// Чтение и создание окончательного растрового изображения
    return BitmapFactory.decodeFile(path, options)
}

fun getScaledBitmap(path: String, activity: Activity): Bitmap {
    val size = Point()
    activity.windowManager.defaultDisplay.getSize(size)

    return getScaledBitmap(path, size.x, size.y)
}


fun getScaled(path: String, layout: LinearLayout): Bitmap {

    val vto = layout.viewTreeObserver
    var width = 0
    var height = 0
    vto.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                layout.viewTreeObserver
                    .removeOnGlobalLayoutListener(this)
            } else {
                layout.viewTreeObserver
                    .removeGlobalOnLayoutListener(this)
            }
            width = layout.measuredWidth
            height = layout.measuredHeight
        }
    })

    return getScaledBitmap(path, width, height)
}
