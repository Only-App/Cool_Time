package com.onlyapp.cooltime.common

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.WindowManager
import android.widget.Toast
import java.io.Serializable

//Serializable 객체를 Intent로 전달하기 위해 사용하는 함수
fun <T: Serializable> Intent.intentSerializable(key: String, clazz: Class<T>): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this.getSerializableExtra(key, clazz)
    } else {
        this.getSerializableExtra(key) as T?
    }
}
//다이얼로그 사이즈 설정 및 투명한 창으로 설정하는 함수
fun Context.dialogResize(dialog: Dialog, width: Float, height: Float){
    val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

    if (Build.VERSION.SDK_INT < 30){
        val display = windowManager.defaultDisplay
        val size = Point()

        display.getSize(size)

        val window = dialog.window

        val x = (size.x * width).toInt()
        val y = (size.y * height).toInt()

        window?.setLayout(x, y)

    }else{
        val rect = windowManager.currentWindowMetrics.bounds

        val window = dialog.window
        val x = (rect.width() * width).toInt()
        val y = (rect.height() * height).toInt()

        window?.setLayout(x, y)
    }
    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
}

//요일 정보에 오늘이 포함되는지
fun isExistMatchToday(today : Int, dayInfo : Int) : Boolean{
    return (1 shl (6 - (today + 5) % 7)) and dayInfo != 0
}

fun Context?.showShortToast(text : String){
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
}