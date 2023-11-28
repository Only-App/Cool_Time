package com.onlyapp.cooltime.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.onlyapp.cooltime.data.entity.Alarm
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmDao {
    @Insert
    suspend fun insertAlarm(alarm: Alarm): Long

    @Delete
    suspend fun deleteAlarm(alarm: Alarm)

    @Update
    suspend fun updateAlarm(alarm: Alarm): Int

    @Query("SELECT * FROM alarm")
    fun getAllFlow(): Flow<List<Alarm>>

    @Query("SELECT * FROM alarm WHERE id= :id")
    suspend fun getAlarm(id: Int): Alarm
}