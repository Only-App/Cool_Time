package com.onlyapp.cooltime.view.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onlyapp.cooltime.data.ExceptAppRepository
import com.onlyapp.cooltime.data.entity.ExceptApp
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class ExceptAppViewModel(private val repository: ExceptAppRepository) : ViewModel() {
    val exceptApp : LiveData<List<ExceptApp>> = repository.allApps

    fun insertApp(app : ExceptApp) {
        viewModelScope.launch{ repository.insertApp(app) }
    }

    fun deleteApp(packageName : String){
        viewModelScope.launch{ repository.deleteApp(packageName) }
    }
    suspend fun getApp(packageName : String) : ExceptApp {
        return viewModelScope.async { repository.getApp(packageName) }.await()
    }

    fun updateApp(app : ExceptApp){
        viewModelScope.launch { repository.updateApp(app) }
    }
}

