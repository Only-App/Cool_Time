package com.example.cool_time

import android.app.Application
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

class MyApplication : Application() {
    private lateinit var dataStore : DataStoreModule
    companion object{
        private lateinit var myApplication: MyApplication
        fun getInstance() = myApplication
    }
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate() {
        super.onCreate()
        val br =  MyBroadcastReceiver()
        myApplication = this
        dataStore = DataStoreModule(this)

        registerReceiver(br, IntentFilter().apply{  //브로드 캐스트 리시버 등록
            addAction(Intent.ACTION_USER_PRESENT)
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_DATE_CHANGED)
        })
        appUseStats()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun appUseStats(){  //매 초마다 총 사용 시간 갱신
        Thread {
            while(true){
                var totalTime = 0L

                val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

                usageStatsManager?.run {
                    val todayDate = Calendar.getInstance()  //당일 00시 00분으로 설정 (제대로 안됨)

                    todayDate.set(Calendar.HOUR, 0)
                    todayDate.set(Calendar.MINUTE, 0)
                    todayDate.set(Calendar.SECOND, 0)
                    todayDate.set(Calendar.AM_PM, 0)


                    val allAppList = usageStatsManager.queryUsageStats(   //모든 앱에 대한 오늘 사용 정보에 대한 리스트(당일 자정부터 현재 시간까지의 기간)
                        UsageStatsManager.INTERVAL_BEST,
                        todayDate.timeInMillis,
                        System.currentTimeMillis()
                    )
                    Log.d("todayDate", todayDate.time.toString())
                    Log.d("currentDate", Date(System.currentTimeMillis()).toString())
                    val mainIntent = Intent(Intent.ACTION_MAIN, null)
                    mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)

                    val availAppList = packageManager.queryIntentActivities(mainIntent, 0)  // 현재 실행 가능한 앱 리스트
                    val availAppNameList = availAppList.map { it.activityInfo.packageName } //현재 실행 가능한 앱의 패키지 이름에 대한 리스트

                    val appList = packageManager.getInstalledApplications(0)
                    val appNameList = appList.map{it.packageName}

                    for (item in allAppList) {
                        val itemName = item.packageName
                        val itemUseTime = item.totalTimeInForeground / 1000
                        if (itemName in availAppNameList && itemUseTime != 0L) {    //어떤 앱이 실행 가능한 앱이고, 실행 시간이 0초보다 크다면
                            if(itemName == "com.example.cool_time") continue
                            totalTime += itemUseTime
                        }
                    }
                    CoroutineScope(Dispatchers.Main).launch{
                        getInstance().getDataStore().onUseTimeChanged(totalTime)  //DataStore에 총 사용 시간 저장
                    }
                }
                Thread.sleep(1000)
            }
        }.start()

    }


    fun getDataStore() = dataStore
}