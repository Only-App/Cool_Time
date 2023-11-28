package com.onlyapp.cooltime.model

import android.graphics.drawable.Drawable

data class ExceptAppModel(
    val appName : String,
    val packageName : String,
    val appIcon : Drawable? = null,
    val checked : Boolean
)
