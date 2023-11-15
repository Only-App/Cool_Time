package com.onlyapp.cooltime.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.onlyapp.cooltime.data.entity.Alarm

@Dao
interface AlarmDAO {
    @Insert
    fun insertAlarm(alarm  : Alarm)

    @Delete
    fun deleteAlarm(alarm : Alarm)
    @Update
    fun updateAlarm(alarm : Alarm) : Int

    @Query("SELECT * FROM alarm")
    fun getAll() : LiveData<List<Alarm>>

    @Query("SELECT * FROM alarm WHERE id= :id")
    fun getAlarm(id : Int) : Alarm
}