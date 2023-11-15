package com.onlyapp.cooltime.model

import android.graphics.drawable.Drawable

data class ExceptAppItem(
    val appName : String,
    val packageName : String,
    val appIcon : Drawable,
    val checked : Boolean
)
