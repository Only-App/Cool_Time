package com.onlyapp.cooltime.data

import com.onlyapp.cooltime.data.entity.PhoneLock
import kotlinx.coroutines.flow.Flow

interface LockRepository {
    suspend fun getLock(id: Int)
    suspend fun insertLock(lock : PhoneLock)
    suspend fun updateLock(lock : PhoneLock)
    suspend fun deleteLock(lock : PhoneLock)
    suspend fun getAllFlow() : Flow<List<PhoneLock>>

}