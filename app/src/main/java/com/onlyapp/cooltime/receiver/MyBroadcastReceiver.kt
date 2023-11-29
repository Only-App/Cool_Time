package com.onlyapp.cooltime.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.onlyapp.cooltime.MyApplication
import com.onlyapp.cooltime.MyApplication.Companion.waitCheck
import com.onlyapp.cooltime.R
import com.onlyapp.cooltime.common.Constants
import com.onlyapp.cooltime.common.intentSerializable
import com.onlyapp.cooltime.repository.AlarmRepositoryImpl
import com.onlyapp.cooltime.data.DataStoreModule
import com.onlyapp.cooltime.data.UserDatabase
import com.onlyapp.cooltime.model.AlarmModel
import com.onlyapp.cooltime.model.DateStatModel
import com.onlyapp.cooltime.repository.DateStatRepositoryImpl
import com.onlyapp.cooltime.service.UseTimeService
import com.onlyapp.cooltime.utils.AlarmScheduler
import com.onlyapp.cooltime.utils.getYesterdayEnd
import com.onlyapp.cooltime.utils.getYesterdayStart
import com.onlyapp.cooltime.utils.loadTimeUsage
import com.onlyapp.cooltime.utils.loadUsage
import com.onlyapp.cooltime.view.ui.alarm.ActiveAlarmActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MyBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_USER_PRESENT -> //잠금 해제 상황에서
                CoroutineScope(Dispatchers.Main).launch {
                    MyApplication.getInstance().getDataStore().increaseCnt()
                    MyApplication.getInstance().getDataStore().updateLockStatus()

                }

            Intent.ACTION_SCREEN_OFF -> //화면이 꺼진 상황에서
                CoroutineScope(Dispatchers.Main).launch {
                    val status = MyApplication.getInstance().getDataStore().lockStatus.first()
                    if (status) { //화면 잠금 해제되어 있는 상태일 때
                        MyApplication.getInstance().getDataStore()
                            .updateLatestUseTime(System.currentTimeMillis())
                        MyApplication.getInstance().getDataStore().updateLockStatus()
                        waitCheck = false
                    }

                }

            //날짜 변경될 때
            Intent.ACTION_DATE_CHANGED -> {
                CoroutineScope(Dispatchers.Main).launch {
                    MyApplication.getInstance().getDataStore().onDateChanged()  //정보 초기화
                }
                CoroutineScope(Dispatchers.IO).launch { //어제의 사용 정보 저장
                    val db = checkNotNull(UserDatabase.getInstance(context)) { "Database is Null" }
                    val dateStatDao = checkNotNull(db.dateStatDao())
                    val dateStatRepository = DateStatRepositoryImpl(dateStatDao)

                    val yesterdayStart = getYesterdayStart().timeInMillis
                    val yesterdayEnd = getYesterdayEnd().timeInMillis
                    val yesterdayAppStat: List<Pair<String, Long>> =    //앱별 사용시간
                        loadUsage(context, yesterdayStart, yesterdayEnd)
                    val yesterdayHourState: List<Long> = loadTimeUsage(context, getYesterdayStart()).toList()    //시간별 사용시간

                    dateStatRepository.insertDateStat(
                        DateStatModel(
                            yesterdayStart,
                            yesterdayAppStat,
                            yesterdayHourState
                        )
                    )
                }
            }

            Intent.ACTION_BOOT_COMPLETED -> {//부팅됐다는 알람 왔을 때 처리할 로직
                context.startService(Intent(context, UseTimeService::class.java))
                //부팅되었을 때 다시 저장된 모든 알람을 설정
                CoroutineScope(Dispatchers.Main).launch {
                    val alarmDao = UserDatabase.getInstance(context)?.alarmDao()
                    val alarmRepository = alarmDao?.let { AlarmRepositoryImpl(alarmDao) }
                    alarmRepository?.getAllFlow()?.collect {
                        it.forEach { alarm ->
                            AlarmScheduler.registerAlarm(
                                AlarmModel(
                                    alarm.id,
                                    alarm.name,
                                    alarm.day,
                                    alarm.time,
                                    ""
                                ), context
                            )
                        }
                    }
                }
            }

            Constants.reservedAlarm -> {   //알람 상황에서
                CoroutineScope(Dispatchers.Main).launch {
                    //알람 객체를 전달 받음
                    val alarm = intent.intentSerializable(
                        context.getString(R.string.alarm_en),
                        AlarmModel::class.java
                    ) ?: return@launch
                    Log.d("reservedAlarm", alarm.toString())
                    val id = alarm.id
                    if (id != -1) {
                        Log.d(
                            context.getString(R.string.checkResult),
                            AlarmScheduler.checkDay(id, context).toString()
                        )
                        //설정된 알람의 요일이 오늘 요일에 해당되는지 확인
                        val checkResult = async { AlarmScheduler.checkDay(id, context) }.await()
                        if (checkResult) {
                            try {
                                //알람 화면 띄움
                                context.startActivity(
                                    Intent(
                                        context,
                                        ActiveAlarmActivity::class.java
                                    ).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        putExtra(context.getString(R.string.message), alarm.name)
                                    })
                            } catch (e: Exception) {
                                Log.e(context.getString(R.string.errorContent), e.toString())
                            }
                        }
                        //무조건 지정한 시간보다 먼저 알람이 오지 않는다는 것을 가정
                        AlarmScheduler.registerAlarm(alarm, context)
                    }
                }
            }
        }
    }

}