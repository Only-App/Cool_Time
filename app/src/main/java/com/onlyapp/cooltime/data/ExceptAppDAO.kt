package com.onlyapp.cooltime.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.onlyapp.cooltime.data.entity.ExceptApp
import kotlinx.coroutines.flow.Flow

@Dao
interface ExceptAppDAO {
    @Insert
    fun insertApp(exceptApp: ExceptApp)

    @Query("DELETE FROM except_app WHERE packageName=:packageName")
    fun deleteApp(packageName: String)

    @Query("SELECT * FROM except_app WHERE packageName=:packageName")
    fun getApp(packageName: String): ExceptApp

    @Query("SELECT * FROM except_app")
    fun getAllFlow(): Flow<List<ExceptApp>>

    @Update
    fun updateApp(exceptApp: ExceptApp)

}