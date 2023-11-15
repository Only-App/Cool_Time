package com.onlyapp.cooltime.data

import androidx.lifecycle.LiveData
import com.onlyapp.cooltime.model.PhoneLock

class LockRepository(private val lockDAO : LockDAO) {
    var allLock : LiveData<List<PhoneLock>> = lockDAO.getAll()
    fun getLock(id: Int){
        lockDAO.getLock(id)
    }

    fun insertLock(lock : PhoneLock){
        lockDAO.insertLock(lock)
    }
    fun updateLock(lock : PhoneLock){
        lockDAO.updateLock(lock)
    }

    fun deleteLock(lock : PhoneLock){
        lockDAO.deleteLock(lock)
    }


}