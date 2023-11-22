package com.onlyapp.cooltime.data

import com.onlyapp.cooltime.data.entity.PhoneLock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class LockRepositoryImpl(private val lockDAO : LockDAO) : LockRepository{
    override suspend fun getLock(id: Int){
        withContext(Dispatchers.IO){ lockDAO.getLock(id) }
    }

    override suspend fun insertLock(lock : PhoneLock){
        withContext(Dispatchers.IO){ lockDAO.insertLock(lock) }
    }
    override suspend fun updateLock(lock : PhoneLock){
        withContext(Dispatchers.IO){ lockDAO.updateLock(lock) }
    }

    override suspend fun deleteLock(lock : PhoneLock){
        withContext(Dispatchers.IO){ lockDAO.deleteLock(lock) }
    }

    override suspend fun getAllFlow() : Flow<List<PhoneLock>> {
        return withContext(Dispatchers.IO) { lockDAO.getAllFlow() }
    }


}