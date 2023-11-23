package com.onlyapp.cooltime.service

import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.Observer
import com.onlyapp.cooltime.MyApplication
import com.onlyapp.cooltime.MyApplication.Companion.waitCheck
import com.onlyapp.cooltime.common.Constants
import com.onlyapp.cooltime.common.isExistMatchToday
import com.onlyapp.cooltime.data.LockRepository
import com.onlyapp.cooltime.data.LockRepositoryImpl
import com.onlyapp.cooltime.data.UserDatabase
import com.onlyapp.cooltime.data.entity.PhoneLock
import com.onlyapp.cooltime.utils.getAppUsageStats
import com.onlyapp.cooltime.utils.getTodayNow
import com.onlyapp.cooltime.utils.getTodayStart
import com.onlyapp.cooltime.utils.getTomorrowStart
import com.onlyapp.cooltime.utils.getTotalTime
import com.onlyapp.cooltime.view.viewmodel.LockViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

class UseTimeService : LifecycleService() {

    private val handler = Looper.myLooper()?.let { Handler(it) }
    private lateinit var myRunnable: Runnable
    private var lockType = POSSIBLE
    private var reuseTime = 0
    override fun onCreate() {
        super.onCreate()

        myRunnable = object : Runnable {
            override fun run() {

                val lockDao = UserDatabase.getInstance(applicationContext)?.phoneLockDao()
                val lockRepository = lockDao?.let { LockRepositoryImpl(it) }

                checkNotNull(lockDao) { "LockDao is Null" }
                checkNotNull(lockRepository) { "LockRepository is Null" }

                val beginTime = getTodayStart().timeInMillis
                val endTime = getTodayNow().timeInMillis

                val totalTime =
                    getTotalTime(getAppUsageStats(this@UseTimeService, beginTime, endTime).toList())

                CoroutineScope(Dispatchers.Main).launch {
                    MyApplication.getInstance().getDataStore()
                        .onUseTimeChanged(totalTime)  //DataStore에 총 사용 시간 저장

                    val lockStatus =
                        MyApplication.getInstance().getDataStore().lockStatus.first() //현재 잠금 상태

                    lockRepository.getAllFlow().collect { lockList ->
                        for(lockInfo in lockList){
                            val startDate = lockInfo.startDate
                            var endDate = lockInfo.endDate
                            val lockDay = lockInfo.lockDay
                            val minTime = lockInfo.minTime

                            if (endDate != -1L) {
                                val endDateCalendar = Calendar.getInstance().apply {
                                    timeInMillis = endDate
                                    set(Calendar.HOUR_OF_DAY, 23)
                                    set(Calendar.MINUTE, 59)
                                    set(Calendar.SECOND, 59)
                                    set(Calendar.MILLISECOND, 999)
                                }
                                endDate = endDateCalendar.timeInMillis
                            }

                            //잠금 기간을 설정했는데 잠금 기간에 해당되지 않은 경우
                            if(getTodayNow().timeInMillis !in startDate..endDate
                                && (startDate == -1L && endDate == -1L)) continue

                            val dayOfWeek =
                                Calendar.getInstance().get(Calendar.DAY_OF_WEEK)

                            if (!isExistMatchToday(dayOfWeek, lockDay)) continue //오늘이 설정한 요일에 포함되지 않은 경우

                            val lockOnDate = Calendar.getInstance().apply {
                                set(Calendar.HOUR_OF_DAY, lockInfo.lockOn / 60)
                                set(Calendar.MINUTE, lockInfo.lockOn % 60)
                            }

                            val lockOffDate = Calendar.getInstance().apply {
                                set(Calendar.HOUR_OF_DAY, lockInfo.lockOff / 60)
                                set(Calendar.MINUTE, lockInfo.lockOff % 60)
                            }

                            if (lockOffDate < lockOnDate) lockOffDate.add(
                                Calendar.DAY_OF_YEAR,
                                1
                            )   //종료 시간이 시작 시간 보다 빠르면 종료 날짜 + 1

                            when{
                                //잠금 시작 시간과 잠금 종료 시간에 포함되는지
                                getTodayNow().timeInMillis in lockOnDate.timeInMillis..lockOffDate.timeInMillis ->{
                                    lockType = LOCK_DURATION
                                    reuseTime =
                                        ((lockOffDate.timeInMillis - getTodayNow().timeInMillis) / 1000).toInt()
                                    return@collect
                                }
                                totalTime >= lockInfo.totalTime * 60 -> {    //총 사용 시간을 모두 사용한 경우
                                    lockType = EXCEED
                                    reuseTime =
                                        ((getTomorrowStart().timeInMillis - getTodayNow().timeInMillis) / 1000).toInt()
                                    return@collect
                                }
                                minTime != -1L && !lockStatus && !waitCheck -> {    //최소 사용 시간 간격이 설정되어 있는 경우,
                                    lockType = WAIT
                                    waitCheck = true
                                    reuseTime = (lockInfo.minTime * 60).toInt()
                                    return@collect
                                }
                            }
                        }
                    }
                }

                if (lockType != POSSIBLE) {   //현재 잠겨야 하는 상황인 경우
                    val intent = Intent(this@UseTimeService, ActiveLockService::class.java)
                    intent.action = Constants.remainingTime
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.putExtra(Constants.lockType, lockType)   //잠금 유형

                    when (lockType) {
                        WAIT -> {
                            intent.putExtra(Constants.time, reuseTime)   //최소 사용 시간 간격
                        }

                        LOCK_DURATION -> {
                            intent.putExtra(Constants.time, reuseTime)   //잠금이 해제될 때까지의 남은 시간
                        }

                        EXCEED -> {
                            intent.putExtra(Constants.time, reuseTime) //총 사용 시간
                        }
                    }

                    startService(intent)
                    stopSelf()  //서비스 종료
                }
                handler?.postDelayed(this, 500)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val active = Intent(this, ActiveLockService::class.java)
        active.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        handler?.post(myRunnable)

        return super.onStartCommand(active, flags, startId)
    }

    override fun onDestroy() {
        handler?.removeCallbacks(myRunnable)
        super.onDestroy()
    }

    companion object {
        const val POSSIBLE = 0 //현재 사용 가능 상태
        const val EXCEED = 1    //총 사용 시간을 초과한 상태
        const val LOCK_DURATION = 2 //잠금 시간이 적용된 상태
        const val WAIT = 3  //최소 사용 시간 간격으로 사용할 수 없는 상태
    }
}