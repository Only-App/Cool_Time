package com.onlyapp.cooltime.repository

import com.onlyapp.cooltime.data.entity.ExceptApp
import com.onlyapp.cooltime.model.ExceptAppModel
import kotlinx.coroutines.flow.Flow

interface ExceptAppRepository {
    suspend fun allApps(): Flow<List<ExceptAppModel>>
    suspend fun getApp(packageName: String): ExceptAppModel?
    suspend fun insertApp(exceptApp: ExceptAppModel)
    suspend fun deleteApp(packageName: String)
    suspend fun updateApp(exceptApp: ExceptAppModel)
}