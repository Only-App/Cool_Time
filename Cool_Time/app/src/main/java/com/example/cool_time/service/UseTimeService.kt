package com.example.cool_time.service

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.Observer
import com.example.cool_time.MyApplication
import com.example.cool_time.MyApplication.Companion.waitCheck
import com.example.cool_time.data.LockRepository
import com.example.cool_time.data.UserDatabase
import com.example.cool_time.model.PhoneLock
import com.example.cool_time.utils.getTodayNow
import com.example.cool_time.utils.getTodayStart
import com.example.cool_time.utils.getTomorrowStart
import com.example.cool_time.utils.getTotalTime
import com.example.cool_time.viewmodel.LockViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

class UseTimeService : LifecycleService() {
    companion object{
        const val POSSIBLE  = 0 //현재 사용 가능 상태
        const val EXCEED = 1    //총 사용 시간을 초과한 상태
        const val LOCK_DURATION = 2 //잠금 시간이 적용된 상태
        const val WAIT = 3  //최소 사용 시간 간격으로 사용할 수 없는 상태
    }
    private val handler = Handler(Looper.myLooper()!!)
    private lateinit var myRunnable: Runnable
    private var lockType = POSSIBLE
    private var reuseTime = 0
    override fun onCreate(){
        super.onCreate()

        myRunnable = object : Runnable{
            override fun run(){

                val usageStatsManager =
                    getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
                val packageManager = packageManager

                val lockDao = UserDatabase.getInstance(applicationContext)!!.phoneLockDao()
                val lockRepository =  LockRepository(lockDao)
                val lockViewModel = LockViewModel(lockRepository)

                val appUsageMap = mutableMapOf<String, Long>()
                val beginTime = getTodayStart().timeInMillis
                val endTime = getTodayNow().timeInMillis

                val usageEvents = usageStatsManager.queryEvents(beginTime, endTime)

                val list: MutableMap<String, ArrayList<Triple<String, Int, Long>>> =
                    mutableMapOf<String, ArrayList<Triple<String, Int, Long>>>()

                while (usageEvents.hasNextEvent()) {
                    val currentEvent = UsageEvents.Event()
                    usageEvents.getNextEvent(currentEvent)
                    if (currentEvent.eventType == UsageEvents.Event.ACTIVITY_RESUMED
                        || currentEvent.eventType == UsageEvents.Event.ACTIVITY_PAUSED
                    ) {
                        if (list[currentEvent.packageName] == null) {
                            list.putIfAbsent(
                                currentEvent.packageName,
                                ArrayList<Triple<String, Int, Long>>()
                            )
                            list[currentEvent.packageName]!!.add(
                                Triple(
                                    currentEvent.className,
                                    currentEvent.eventType,
                                    currentEvent.timeStamp
                                )
                            )
                        } else {
                            list[currentEvent.packageName]!!.add(
                                Triple(
                                    currentEvent.className,
                                    currentEvent.eventType,
                                    currentEvent.timeStamp
                                )
                            )
                        }
                    }
                }

                for ((key, value) in list) {
                    val packageName = key
                    if(packageName == "com.example.cool_time") continue //CoolTime앱은 제외
                    if (packageManager.getLaunchIntentForPackage(packageName) != null) {
                        if (appUsageMap[packageName] == null) {
                            appUsageMap.putIfAbsent(packageName, 0L)
                        }

                        for (i in 0 until value.size - 1) {
                            val E0 = value[i]
                            val E1 = value[i + 1]

                            if (//E0.first == E1.first &&
                                E0.second == UsageEvents.Event.ACTIVITY_RESUMED
                                && E1.second == UsageEvents.Event.ACTIVITY_PAUSED
                            ) {
                                val diff = ((E1.third - E0.third)) / 1000.toLong()
                                val prev = appUsageMap[packageName] ?: 0L
                                appUsageMap[packageName] = prev + diff
                            }
                        }

                        val lastEvent = value[value.size - 1]

                        if(lastEvent.second == UsageEvents.Event.ACTIVITY_RESUMED){
                            val prev = appUsageMap[packageName] ?: 0L
                            appUsageMap[packageName] = prev + (getTodayNow().timeInMillis - lastEvent.third) /1000.toLong()
                        }


                    }
                }


                val totalTime = getTotalTime(appUsageMap.toList().sortedBy { it.second }.toMap().toList())

                CoroutineScope(Dispatchers.Main).launch {
                    MyApplication.getInstance().getDataStore()
                        .onUseTimeChanged(totalTime)  //DataStore에 총 사용 시간 저장

                    val lockStatus = MyApplication.getInstance().getDataStore().lockStatus.first() //현재 잠금 상태

                    lockViewModel.lock_list.observe(
                        this@UseTimeService,
                        Observer<List<PhoneLock>> {
                            it.forEach {
                                val lockInfo = it
                                val startDate = lockInfo.start_date
                                var endDate = lockInfo.end_date
                                val lockDay = lockInfo.lock_day
                                val minTime = lockInfo.min_time

                                if(endDate != -1L) {
                                    val endDateCalendar = Calendar.getInstance()
                                    endDateCalendar.timeInMillis = endDate
                                    endDateCalendar.set(Calendar.HOUR_OF_DAY, 23)
                                    endDateCalendar.set(Calendar.MINUTE, 59)
                                    endDateCalendar.set(Calendar.SECOND, 59)
                                    endDateCalendar.set(Calendar.MILLISECOND, 999)

                                    endDate = endDateCalendar.timeInMillis
                                }


                                if ((getTodayNow().timeInMillis in startDate..endDate)  //잠금 기간에 해당되는지
                                    || (startDate == -1L && endDate == -1L) //잠금 기간이 없는 경우
                                ) {
                                    val dayOfWeek =
                                        Calendar.getInstance().get(Calendar.DAY_OF_WEEK)

                                    if ((1 shl (6 - (dayOfWeek + 5) % 7)) and lockDay != 0) { //오늘이 설정한 요일에 포함되는지
                                        val lockOnDate = Calendar.getInstance()
                                        lockOnDate.set(
                                            Calendar.HOUR_OF_DAY,
                                            lockInfo.lock_on / 60
                                        )
                                        lockOnDate.set(Calendar.MINUTE, lockInfo.lock_on % 60)

                                        val lockOffDate = Calendar.getInstance()

                                        lockOffDate.set(
                                            Calendar.HOUR_OF_DAY,
                                            lockInfo.lock_off / 60
                                        )

                                        lockOffDate.set(Calendar.MINUTE, lockInfo.lock_off % 60)

                                        if (getTodayNow().timeInMillis in lockOnDate.timeInMillis..lockOffDate.timeInMillis) {   //잠금 시작 시간과 잠금 종료 시간에 포함되는지
                                            lockType = LOCK_DURATION
                                            reuseTime = ((lockOffDate.timeInMillis - getTodayNow().timeInMillis) / 1000).toInt()
                                            return@Observer
                                        } else if (totalTime >= lockInfo.total_time * 60) {  //총 사용 시간을 모두 사용한 경우
                                            lockType = EXCEED
                                            reuseTime = ((getTomorrowStart().timeInMillis - getTodayNow().timeInMillis) / 1000).toInt()
                                            return@Observer
                                        }

                                        else if (minTime != -1L && !lockStatus && !waitCheck) {  //최소 사용 시간 간격이 설정되어 있는 경우,
                                            lockType = WAIT
                                            waitCheck = true
                                            reuseTime = (lockInfo.min_time * 60).toInt()
                                            return@Observer
                                        }


                                    }
                                }

                            }
                        })
                }

                if(lockType != POSSIBLE){   //현재 잠겨야 하는 상황인 경우
                    val intent = Intent(this@UseTimeService, ActiveLockService::class.java)
                    intent.action = "remaining time"
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.putExtra("lockType", lockType)   //잠금 유형

                    when(lockType){
                        WAIT ->{
                            intent.putExtra("time", reuseTime)   //최소 사용 시간 간격
                        }
                        LOCK_DURATION -> {
                            intent.putExtra("time", reuseTime)   //잠금이 해제될 때까지의 남은 시간
                        }
                        EXCEED -> {
                            intent.putExtra("time", reuseTime) //총 사용 시간
                        }
                    }

                    startService(intent)
                    stopSelf()  //서비스 종료
                }

                handler.postDelayed(this, 500)

            }
        }
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val intent = Intent(this, ActiveLockService::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        handler.post(myRunnable)

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy(){
        handler.removeCallbacks(myRunnable)
        super.onDestroy()
    }
    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }
}