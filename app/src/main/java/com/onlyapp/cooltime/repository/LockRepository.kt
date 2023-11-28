package com.onlyapp.cooltime.repository

import com.onlyapp.cooltime.model.PhoneLockModel
import kotlinx.coroutines.flow.Flow

interface LockRepository {
    suspend fun insertLock(lockModel: PhoneLockModel)
    suspend fun updateLock(lockModel: PhoneLockModel)
    suspend fun deleteLock(lockModel: PhoneLockModel)
    suspend fun getAllFlow(): Flow<List<PhoneLockModel>>

}