package com.onlyapp.cooltime.view.viewmodel

import android.graphics.drawable.Drawable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.onlyapp.cooltime.data.ExceptAppRepository
import com.onlyapp.cooltime.data.entity.ExceptApp
import com.onlyapp.cooltime.model.ExceptAppItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ExceptAppViewModel(
    private val repository: ExceptAppRepository,
    private val getAppInfo: (exceptApp: ExceptApp) -> Pair<String, Drawable>
) : ViewModel() {
    private var _exceptAppItemList: MutableLiveData<List<ExceptAppItem>> = MutableLiveData()
    val exceptAppItemList: LiveData<List<ExceptAppItem>>
        get() = _exceptAppItemList

    init {
        fetchData()
    }

    private fun fetchData() {
        viewModelScope.launch {
            repository.allApps().collect { exceptAppList ->
                val exceptAppItemList = exceptAppList.map { exceptApp ->
                    convertToExceptAppItem(exceptApp)
                }
                _exceptAppItemList.value = exceptAppItemList
            }
        }

    }

    fun insertApp(exceptAppItem: ExceptAppItem) {
        viewModelScope.launch {
            repository.insertApp(
                ExceptApp(
                    exceptAppItem.packageName,
                    exceptAppItem.checked
                )
            )
        }
    }

    fun deleteApp(packageName: String) {
        viewModelScope.launch { repository.deleteApp(packageName) }
    }

    suspend fun getApp(packageName: String): ExceptApp? {
        return repository.getApp(packageName)
    }

    fun updateApp(exceptAppItem: ExceptAppItem) {
        viewModelScope.launch {
            repository.updateApp(
                ExceptApp(
                    exceptAppItem.packageName,
                    exceptAppItem.checked
                )
            )
        }
    }

    private suspend fun convertToExceptAppItem(exceptApp: ExceptApp): ExceptAppItem {
        return withContext(Dispatchers.IO){
            val appInfo = getAppInfo.invoke(exceptApp)
            ExceptAppItem(
                appInfo.first,
                exceptApp.packageName,
                appInfo.second,
                exceptApp.checked
            )
        }
    }
}

