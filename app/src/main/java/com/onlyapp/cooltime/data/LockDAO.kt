package com.onlyapp.cooltime.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.onlyapp.cooltime.data.entity.PhoneLock
import kotlinx.coroutines.flow.Flow

@Dao
interface LockDAO {
    @Insert
    fun insertLock(phoneLock : PhoneLock)

    @Delete
    fun deleteLock(phoneLock : PhoneLock)

    @Update
    fun updateLock(phoneLock : PhoneLock)

    @Query("SELECT * FROM phone_lock")
    fun getAll() : LiveData<List<PhoneLock>>


    @Query("SELECT * FROM phone_lock WHERE id = :id")
    fun getLock(id : Int) : PhoneLock

    @Query("SELECT * FROM phone_lock")
    fun getAllFlow() : Flow<List<PhoneLock>>
}