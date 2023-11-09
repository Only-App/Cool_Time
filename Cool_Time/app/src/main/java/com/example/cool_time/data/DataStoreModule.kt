package com.example.cool_time.data

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class DataStoreModule(private val context: Context) {
    private val Context.dataStore by preferencesDataStore(name = "dataStore")

    private val todayCntKey = intPreferencesKey("todayCntKey")  //당일 사용 횟수 키
    private val lockStatusKey = booleanPreferencesKey("lockStatusKey") //잠금 상태 키
    private val latestUseTimeKey = longPreferencesKey("latestUseTimeKey") // 최근 사용 시간 키
    private val enduredTimeKey = intPreferencesKey("patienceTimeKey") //당일 인내한 시간 키
    private val todayUseTimeKey = longPreferencesKey("todayUseTimeKey") //당일 사용 시간 키

    private val yesterdayCntKey= intPreferencesKey("yesterdayCntKey")   //어제 사용 횟수 키
    private val yesterdayEnduredTimeKey= intPreferencesKey("yesterdayEnduredTimeKey")   //어제 인내한 시간 키
    private val yesterdayUseTimeKey= longPreferencesKey("yesterdayUseTimeKey")   //어제 사용 시간 키

    val todayCnt : Flow<Int> = context.dataStore.data // 당일 사용 횟수
        .catch{ exception ->
            if(exception is IOException){
                emit(emptyPreferences())
            }
            else{
                throw exception
            }
        }
        .map{ preferences ->
            preferences[todayCntKey] ?: 0
        }
    val lockStatus : Flow<Boolean> = context.dataStore.data //당일 잠금 상태
        .catch{
            exception ->
            if(exception is IOException){
                emit(emptyPreferences())
            }
            else throw exception
        }
        .map{
            preferences -> preferences[lockStatusKey] ?: true
        }
    val latestUseTime : Flow<Long> = context.dataStore.data //최근 사용 시간
        .catch{
            exception ->
            if(exception is IOException){
                emit(emptyPreferences())
            }
            else throw exception
        }
        .map{
            preferences -> preferences[latestUseTimeKey] ?: 0L
        }
    val enduredTime : Flow<Int> = context.dataStore.data    //당일 바로 잠금으로 인내한 시간
        .catch{
            exception ->
            if(exception is IOException){
                emit(emptyPreferences())
            }
            else throw exception
        }
        .map{
            preferences -> preferences[enduredTimeKey] ?: 0
        }

    val todayUseTime : Flow<Long> = context.dataStore.data  //당일 총 사용 시간
        .catch{
            exception ->
            if(exception is IOException){
                emit(emptyPreferences())
            }
            else throw exception
        }
        .map{
            preferences -> preferences[todayUseTimeKey] ?: 0L
        }

    val yesterdayCnt : Flow<Int> = context.dataStore.data   //어제 사용 횟수
        .catch{
            exception ->
            if(exception is IOException){
                emit(emptyPreferences())
            }
            else throw exception
        }
        .map{
            preferences -> preferences[yesterdayCntKey] ?: 0
        }

    val yesterdayEnduredTime : Flow<Int> = context.dataStore.data   //어제 인내한 시간
        .catch{
            exception ->
            if(exception is IOException){
                emit(emptyPreferences())
            }
            else throw exception
        }
        .map{
            preferences -> preferences[yesterdayEnduredTimeKey] ?: 0
        }

    val yesterdayUseTime : Flow<Long> = context.dataStore.data
        .catch{
            exception ->
            if(exception is IOException){
                emit(emptyPreferences())
            }
            else throw exception
        }
        .map{
            preferences -> preferences[yesterdayUseTimeKey] ?: 0L
        }
    suspend fun increaseCnt(){  //사용 횟수 증가 함수
        context.dataStore.edit{
            preferences ->
            Log.d("preferences", "${preferences[todayCntKey]}")

            val cnt = preferences[todayCntKey] ?: 0
            preferences[todayCntKey] = cnt + 1
        }
    }

    suspend fun updateLockStatus(){ //잠금 상태 변경 함수
        context.dataStore.edit{
            preferences ->

            val status = preferences[lockStatusKey] ?: true
            preferences[lockStatusKey] = !status
        }
    }
    suspend fun updateLatestUseTime(currentTime : Long){ //최근 사용 시간 업데이트 함
        context.dataStore.edit{
            preferences ->
            preferences[latestUseTimeKey] = currentTime

        }
    }

    suspend fun increaseEnduredTime(time : Int){    //바로 잠금으로 인내한 시간 증가
        context.dataStore.edit{
            preferences ->
            val curEnduredTime = preferences[enduredTimeKey] ?: 0
            preferences[enduredTimeKey] = curEnduredTime + time
        }
    }

    suspend fun onDateChanged(){    //날짜가 변경할 때 정보 초기화
        context.dataStore.edit{
            preferences ->

            preferences[yesterdayCntKey] = preferences[todayCntKey] ?: 0    //어제 사용 횟수에 오늘 사용한 횟수 값을 할당
            preferences[yesterdayEnduredTimeKey] = preferences[enduredTimeKey] ?: 0 //어제 인내한 시간에 오늘 인내한 시간을 할당


            preferences[todayCntKey] = 0    //오늘 사용한 횟수 0으로 초기화
            preferences[enduredTimeKey] = 0 //오늘 인내한 시간 0으로 초기화

            preferences[yesterdayUseTimeKey] = preferences[todayUseTimeKey] ?: 0L   //어제 총 사용 시간에 오늘 총 사용 시간을 할당
            preferences[todayUseTimeKey] = 0L    //오늘 총 사용 시간을 0으로 초기화
        }
    }
    suspend fun onUseTimeChanged(totalUseTime : Long){
        context.dataStore.edit{
            preferences -> preferences[todayUseTimeKey] = totalUseTime
        }
    }
}