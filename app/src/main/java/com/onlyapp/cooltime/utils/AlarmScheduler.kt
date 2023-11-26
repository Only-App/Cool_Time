package com.onlyapp.cooltime.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.onlyapp.cooltime.R
import com.onlyapp.cooltime.common.Constants
import com.onlyapp.cooltime.data.AlarmRepositoryImpl
import com.onlyapp.cooltime.data.UserDatabase
import com.onlyapp.cooltime.model.AlarmModel
import com.onlyapp.cooltime.receiver.MyBroadcastReceiver
import java.util.Calendar

object AlarmScheduler {
    fun registerAlarm(alarmModel: AlarmModel, context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val id = alarmModel.id
        val hour = alarmModel.time / 60
        val minute = alarmModel.time % 60

        val alarmCalendar = getTodayStart().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }

        if (alarmCalendar.timeInMillis < getTodayNow().timeInMillis) {
            alarmCalendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        val alarmTime = alarmCalendar.timeInMillis
        val intent = Intent(context, MyBroadcastReceiver::class.java).apply {
            action = Constants.reservedAlarm
            putExtra(context.getString(R.string.alarm_en), alarmModel)
            Log.d("registerAlarm", alarmModel.toString())
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        /*setRepeating 함수는 정확한 시간에 알람이 안되는 것 같음
           대신 정확한 시간에 알람을 제공하는 함수인 setExact를 사용하여서 알람을 울리게 하고
           알람 Intent를 받으면 다음 알람을 예약하도록
         */
        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            alarmTime,
            pendingIntent
        )

    }

    fun cancelAlarm(id: Int, context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, MyBroadcastReceiver::class.java).apply {
            action = Constants.reservedAlarm
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    suspend fun checkDay(id: Int, context: Context): Boolean {
        val alarmDao = UserDatabase.getInstance(context)?.alarmDao()
        val alarmRepository = alarmDao?.let { AlarmRepositoryImpl(alarmDao) }


        val alarm = checkNotNull(alarmRepository?.getAlarm(id))
        val alarmDay = alarm.day


        val dayOfWeek = getTodayNow().get(Calendar.DAY_OF_WEEK)

        val result = (1 shl (6 - (dayOfWeek + 5) % 7)) and alarmDay != 0

        Log.d("dayOfWeek", dayOfWeek.toString())
        Log.d("alarmDay", alarmDay.toString())

        return result
    }
}