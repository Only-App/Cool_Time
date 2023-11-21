package com.onlyapp.cooltime.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "except_app")
data class ExceptApp(
    @PrimaryKey
    @SerializedName("package_name") val packageName : String,
    @SerializedName("checked") val checked : Boolean
)
