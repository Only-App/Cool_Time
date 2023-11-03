package com.example.cool_time.model

import android.graphics.drawable.Drawable

data class ExceptAppItem(
    val appName : String,
    val packageName : String,
    val appIcon : Drawable,
    var checked : Boolean
)
