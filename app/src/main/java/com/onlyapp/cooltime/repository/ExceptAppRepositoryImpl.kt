package com.onlyapp.cooltime.repository

import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import com.onlyapp.cooltime.data.dao.ExceptAppDao
import com.onlyapp.cooltime.data.entity.ExceptApp
import com.onlyapp.cooltime.model.ExceptAppModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ExceptAppRepositoryImpl(private val exceptAppDao: ExceptAppDao) : ExceptAppRepository {
    override suspend fun allApps(): Flow<List<ExceptAppModel>> {
        return exceptAppDao.getAllFlow().map {
            it.map { exceptAppEntity ->
                ExceptAppModel(
                    appName = "",
                    packageName = exceptAppEntity.packageName,
                    checked = exceptAppEntity.checked,
                )
            }
        }
    }

    override suspend fun getApp(packageName: String): ExceptAppModel? {
        val exceptAppEntity = exceptAppDao.getApp(packageName)
        return if(exceptAppEntity == null) null
        else ExceptAppModel(
            appName = "",
            packageName = exceptAppEntity.packageName,
            checked = exceptAppEntity.checked
        )
    }

    override suspend fun insertApp(exceptAppModel: ExceptAppModel) {
        try {
            exceptAppDao.insertApp(ExceptApp(
                exceptAppModel.packageName,
                exceptAppModel.checked
            ))
        } catch (e: SQLiteConstraintException) {
            Log.e(exceptAppModel.packageName, "already inserted")
        }
    }

    override suspend fun deleteApp(packageName: String) {
        exceptAppDao.deleteApp(packageName)
    }

    override suspend fun updateApp(exceptAppModel: ExceptAppModel) {
        exceptAppDao.updateApp(ExceptApp(
            exceptAppModel.packageName,
            exceptAppModel.checked
        ))
    }
}