package com.onlyapp.cooltime.data

import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import com.onlyapp.cooltime.data.entity.ExceptApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ExceptAppRepositoryImpl(private val exceptAppDAO: ExceptAppDAO) : ExceptAppRepository {
    override suspend fun allApps(): Flow<List<ExceptApp>> = exceptAppDAO.getAllFlow()

    override suspend fun getApp(packageName: String): ExceptApp {
        return withContext(Dispatchers.IO) { exceptAppDAO.getApp(packageName) }
    }

    override suspend fun insertApp(exceptApp: ExceptApp) {
        try {
            withContext(Dispatchers.IO) { exceptAppDAO.insertApp(exceptApp) }
        } catch (e: SQLiteConstraintException) {
            Log.e(exceptApp.packageName, "already inserted")
        }
    }

    override suspend fun deleteApp(packageName: String) {
        withContext(Dispatchers.IO) { exceptAppDAO.deleteApp(packageName) }
    }

    override suspend fun updateApp(exceptApp: ExceptApp) {
        withContext(Dispatchers.IO) { exceptAppDAO.updateApp(exceptApp) }
    }
}