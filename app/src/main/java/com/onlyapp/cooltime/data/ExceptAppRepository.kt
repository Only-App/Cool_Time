package com.onlyapp.cooltime.data

import com.onlyapp.cooltime.data.entity.ExceptApp
import kotlinx.coroutines.flow.Flow

interface ExceptAppRepository {
    suspend fun allApps(): Flow<List<ExceptApp>>
    suspend fun getApp(packageName: String): ExceptApp
    suspend fun insertApp(exceptApp: ExceptApp)
    suspend fun deleteApp(packageName: String)
    suspend fun updateApp(exceptApp: ExceptApp)
}