package com.example.cool_time.data

import androidx.lifecycle.LiveData
import com.example.cool_time.model.ExceptApp

class ExceptAppRepository(private val exceptAppDAO : ExceptAppDAO) {
    val allApps : LiveData<List<ExceptApp>> = exceptAppDAO.getAll()

    fun getApp(packageName : String) : ExceptApp{
        return exceptAppDAO.getApp(packageName)
    }
    fun insertApp(exceptApp: ExceptApp){
        exceptAppDAO.insertApp(exceptApp)
    }
    fun deleteApp(packageName : String){
        exceptAppDAO.deleteApp(packageName)
    }

    fun updateApp(exceptApp: ExceptApp){
        exceptAppDAO.updateApp(exceptApp)
    }
}