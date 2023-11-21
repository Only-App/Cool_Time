package com.onlyapp.cooltime.data

import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import androidx.lifecycle.LiveData
import com.onlyapp.cooltime.data.entity.ExceptApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ExceptAppRepository(private val exceptAppDAO : ExceptAppDAO) {
    val allApps : LiveData<List<ExceptApp>> = exceptAppDAO.getAll()

    suspend fun getApp(packageName : String) : ExceptApp {
        return withContext(Dispatchers.IO){ exceptAppDAO.getApp(packageName) }
    }
    suspend fun getAllApps() : List<ExceptApp>{
        return withContext(Dispatchers.IO){ exceptAppDAO.getAllNotLive() }
    }
    suspend fun insertApp(exceptApp: ExceptApp){
        try {
            withContext(Dispatchers.IO) { exceptAppDAO.insertApp(exceptApp) }
        }catch(e : SQLiteConstraintException){
            Log.e(exceptApp.packageName, "already inserted")
        }
    }
    suspend fun deleteApp(packageName : String){
        withContext(Dispatchers.IO){ exceptAppDAO.deleteApp(packageName) }
    }

    suspend fun updateApp(exceptApp: ExceptApp){
        withContext(Dispatchers.IO){ exceptAppDAO.updateApp(exceptApp) }
    }
}