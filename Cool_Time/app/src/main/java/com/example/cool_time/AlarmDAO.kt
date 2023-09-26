package com.example.cool_time

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.cool_time.model.Alarm

@Dao
interface AlarmDAO {
    @Insert
    fun insertAlarm(alarm  : Alarm)

    @Delete
    fun deleteAlarm(alarm : Alarm)
    @Update
    fun updateAlarm(alarm : Alarm)

    @Query("SELECT * FROM ALARM")
    fun getAll() : LiveData<List<Alarm>>

    @Query("SELECT * FROM ALARM WHERE id= :id")
    fun getAlarm(id : Int) : Alarm
}