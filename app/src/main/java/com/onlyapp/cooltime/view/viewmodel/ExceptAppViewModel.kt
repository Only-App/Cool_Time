package com.onlyapp.cooltime.view.viewmodel

import android.graphics.drawable.Drawable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onlyapp.cooltime.repository.ExceptAppRepository
import com.onlyapp.cooltime.data.entity.ExceptApp
import com.onlyapp.cooltime.model.ExceptAppModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ExceptAppViewModel(
    private val repository: ExceptAppRepository,
    private val getAppInfo: (exceptApp: ExceptAppModel) -> Pair<String, Drawable>
) : ViewModel() {
    private var _exceptAppModelList: MutableLiveData<List<ExceptAppModel>> = MutableLiveData()
    val exceptAppModelList: LiveData<List<ExceptAppModel>>
        get() = _exceptAppModelList

    init {
        fetchData()
    }

    private fun fetchData() {
        viewModelScope.launch {
            repository.allApps().collect { exceptAppModelList ->
                _exceptAppModelList.value = exceptAppModelList.map{
                        exceptAppModel ->
                    val appInfo = getAppInfo.invoke(exceptAppModel)
                    exceptAppModel.copy(
                        appName = appInfo.first,
                        appIcon = appInfo.second
                    )
                }
            }
        }

    }

    fun insertApp(exceptAppModel: ExceptAppModel) {
        viewModelScope.launch {
            repository.insertApp(exceptAppModel)
        }
    }

    fun deleteApp(packageName: String) {
        viewModelScope.launch { repository.deleteApp(packageName) }
    }

    suspend fun getApp(packageName: String): ExceptAppModel {
        return repository.getApp(packageName)
    }

    fun updateApp(exceptAppModel: ExceptAppModel) {
        viewModelScope.launch {
            repository.updateApp(
                exceptAppModel.copy(
                    checked = !exceptAppModel.checked
                )
            )
        }
    }

}

