package com.onlyapp.cooltime.data

import androidx.lifecycle.LiveData
import com.onlyapp.cooltime.data.entity.PhoneLock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LockRepository(private val lockDAO : LockDAO) {
    val allLock : LiveData<List<PhoneLock>> = lockDAO.getAll()
    suspend fun getLock(id: Int){
        withContext(Dispatchers.IO){ lockDAO.getLock(id) }
    }

    suspend fun insertLock(lock : PhoneLock){
        withContext(Dispatchers.IO){ lockDAO.insertLock(lock) }
    }
    suspend fun updateLock(lock : PhoneLock){
        withContext(Dispatchers.IO){ lockDAO.updateLock(lock) }
    }

    suspend fun deleteLock(lock : PhoneLock){
        withContext(Dispatchers.IO){ lockDAO.deleteLock(lock) }
    }


}