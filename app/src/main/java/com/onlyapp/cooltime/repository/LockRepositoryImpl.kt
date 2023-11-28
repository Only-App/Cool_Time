package com.onlyapp.cooltime.repository

import com.onlyapp.cooltime.data.dao.LockDao
import com.onlyapp.cooltime.data.entity.PhoneLock
import com.onlyapp.cooltime.model.PhoneLockModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LockRepositoryImpl(private val lockDao: LockDao) : LockRepository {

    override suspend fun insertLock(lockModel: PhoneLockModel) {
        val lockEntity = PhoneLock(
            id = lockModel.id,
            totalTime = lockModel.totalTime,
            minTime = lockModel.minTime,
            lockOn = lockModel.lockOn,
            lockOff = lockModel.lockOff,
            lockDay = lockModel.lockDay,
            startDate = lockModel.startDate,
            endDate = lockModel.endDate
        )
        lockDao.insertLock(lockEntity)
    }

    override suspend fun updateLock(lockModel: PhoneLockModel) {
        val lockEntity = PhoneLock(
            id = lockModel.id,
            totalTime = lockModel.totalTime,
            minTime = lockModel.minTime,
            lockOn = lockModel.lockOn,
            lockOff = lockModel.lockOff,
            lockDay = lockModel.lockDay,
            startDate = lockModel.startDate,
            endDate = lockModel.endDate
        )
        lockDao.updateLock(lockEntity)
    }

    override suspend fun deleteLock(lockModel: PhoneLockModel) {
        val lockEntity = PhoneLock(
            id = lockModel.id,
            totalTime = lockModel.totalTime,
            minTime = lockModel.minTime,
            lockOn = lockModel.lockOn,
            lockOff = lockModel.lockOff,
            lockDay = lockModel.lockDay,
            startDate = lockModel.startDate,
            endDate = lockModel.endDate
        )
        lockDao.deleteLock(lockEntity)
    }

    override suspend fun getAllFlow(): Flow<List<PhoneLockModel>> {
        return lockDao.getAllFlow().map{
            it.map {
                lockEntity ->
                PhoneLockModel(
                    id = lockEntity.id,
                    totalTime = lockEntity.totalTime,
                    minTime = lockEntity.minTime,
                    lockOn = lockEntity.lockOn,
                    lockOff = lockEntity.lockOff,
                    lockDay = lockEntity.lockDay,
                    startDate = lockEntity.startDate,
                    endDate = lockEntity.endDate
                )
            }
        }
    }


}