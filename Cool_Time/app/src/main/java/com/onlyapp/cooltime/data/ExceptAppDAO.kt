package com.onlyapp.cooltime.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.onlyapp.cooltime.data.entity.ExceptApp

@Dao
interface ExceptAppDAO {
    @Insert
    fun insertApp(exceptApp : ExceptApp)

    @Query("DELETE FROM exceptApp WHERE packageName=:packageName")
    fun deleteApp(packageName : String)

    @Query("SELECT * FROM exceptApp WHERE packageName=:packageName")
    fun getApp(packageName : String) : ExceptApp

    @Query("SELECT * FROM exceptApp")
    fun getAll() : LiveData<List<ExceptApp>>

    @Query("SELECT * FROM exceptApp")
    fun getAllNotLive() : List<ExceptApp>
    @Update
    fun updateApp(exceptApp : ExceptApp)

}