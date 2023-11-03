package com.example.cool_time.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("exceptApp")
data class ExceptApp(
    @PrimaryKey
    val packageName : String,
    val checked : Boolean
)
