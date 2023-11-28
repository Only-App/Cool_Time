package com.onlyapp.cooltime.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.onlyapp.cooltime.data.entity.PhoneLock
import kotlinx.coroutines.flow.Flow

@Dao
interface LockDao {
    @Insert
    suspend fun insertLock(phoneLock: PhoneLock)

    @Delete
    suspend fun deleteLock(phoneLock: PhoneLock)

    @Update
    suspend fun updateLock(phoneLock: PhoneLock)

    @Query("SELECT * FROM phone_lock")
    fun getAllFlow(): Flow<List<PhoneLock>>
}