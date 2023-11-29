package com.onlyapp.cooltime.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.onlyapp.cooltime.data.entity.DateStat

@Dao
interface DateStatDao {
    @Insert
    suspend fun insertDateStat(dateStat : DateStat)

    @Query("SELECT * FROM date_stat WHERE date=:date")
    suspend fun getDateStat(date : Long) : DateStat?


}