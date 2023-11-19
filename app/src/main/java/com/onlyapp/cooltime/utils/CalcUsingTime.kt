package com.onlyapp.cooltime.utils

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.util.Log
import java.util.Calendar

fun getTodayStart(): Calendar {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar
}
fun getTodayNow(): Calendar {
    return Calendar.getInstance()
}

fun getTomorrowStart() : Calendar{
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_MONTH, 1)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar
}

fun getYesterdayStart(): Calendar {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_MONTH, -1)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar
}

fun getYesterdayEnd(): Calendar {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_MONTH, -1)
    calendar.set(Calendar.HOUR_OF_DAY, 23)
    calendar.set(Calendar.MINUTE, 59)
    calendar.set(Calendar.SECOND, 59)
    calendar.set(Calendar.MILLISECOND, 999)
    return calendar
}

fun getSomedayStart(year : Int,  month : Int,  day:Int): Calendar{
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.YEAR, year)
    calendar.set(Calendar.MONTH, month)
    calendar.set(Calendar.DAY_OF_MONTH, day)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar
}

fun getSomedayEnd(year : Int,  month : Int,  day:Int): Calendar{
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.YEAR, year)
    calendar.set(Calendar.MONTH, month)
    calendar.set(Calendar.DAY_OF_MONTH, day)
    calendar.set(Calendar.HOUR_OF_DAY, 23)
    calendar.set(Calendar.MINUTE, 59)
    calendar.set(Calendar.SECOND, 59)
    calendar.set(Calendar.MILLISECOND, 999)
    return calendar
}



fun getAppUsageStats(context : Context, beginTime : Long, endTime : Long): Map<String, Long> {
    val appUsageMap = mutableMapOf<String, Long>()
    val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    val packageManager = context.packageManager
    val usageEvents = usageStatsManager.queryEvents(beginTime, endTime)

    val list :MutableMap<String, ArrayList<Triple<String, Int, Long>>> = mutableMapOf<String, ArrayList<Triple<String, Int,Long>>>()
    while (usageEvents.hasNextEvent()) {
        val currentEvent = UsageEvents.Event()
        usageEvents.getNextEvent(currentEvent)
        when (currentEvent.eventType) {
            UsageEvents.Event.ACTIVITY_RESUMED, UsageEvents.Event.ACTIVITY_PAUSED -> {
                if (list[currentEvent.packageName] == null) {
                    list.putIfAbsent(currentEvent.packageName, ArrayList<Triple<String, Int, Long>>())
                    list[currentEvent.packageName]?.add(Triple(currentEvent.className, currentEvent.eventType, currentEvent.timeStamp))
                } else {
                    list[currentEvent.packageName]?.add(Triple(currentEvent.className, currentEvent.eventType, currentEvent.timeStamp))
                }
            }
        }
    }

    for((packageName, value) in list){
        if(packageName == "com.onlyapp.cooltime") continue
        if(packageManager.getLaunchIntentForPackage(packageName) != null ) {
            if (appUsageMap[packageName] == null) {
                appUsageMap.putIfAbsent(packageName, 0L)
            }
            for (i in 0 until value.size - 1) {
                val E0 = value[i]
                val E1 = value[i + 1]
                if (//E0.first == E1.first &&
                    E0.second == UsageEvents.Event.ACTIVITY_RESUMED &&
                    E1.second == UsageEvents.Event.ACTIVITY_PAUSED
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
                Log.d("packageName",  packageName)
            }

        }

    }

    return appUsageMap.toList().sortedBy { it.second }.toMap()
}

fun loadUsage(context : Context, startDay : Long, endDay : Long): List<Pair<String, Long>> {
    val stats = getAppUsageStats(context, startDay, endDay)
    return stats.toList()
}

fun loadTimeUsage(context : Context, calendar : Calendar): ArrayList<Long> {
    val list = ArrayList<Long>()
    for (i in 0 until 24) {
        val startDay = calendar.clone() as Calendar
        startDay.set(Calendar.HOUR_OF_DAY, i)
        val endDay = calendar.clone() as Calendar
        endDay.set(Calendar.HOUR_OF_DAY, i)
        endDay.set(Calendar.MINUTE, 59)
        endDay.set(Calendar.SECOND, 59)
        endDay.set(Calendar.MILLISECOND, 999)
        var totalTime = 0L

        val usageStats = getAppUsageStats(
            context,
            startDay.timeInMillis,
            endDay.timeInMillis
        )
        for(app in usageStats) {
            totalTime += app.value
        }
        list.add(totalTime)
    }
    return list
}



fun getTotalTime(list :  List<Pair<String, Long>>): Long {
    var totalTime = 0L;
    for(element in list){
        totalTime+= element.second
    }
    return totalTime
}

fun getDiff( totalTime:Long, yesterdayTotalTime:Long ): String {
    var diff = yesterdayTotalTime - totalTime
    val moreOrLess = if(diff < 0){"더 사용"} else{ "덜 사용"}
    diff = Math.abs(diff)
    val diffTotalHour = (diff/3600).toString()
    val diffTotalMin = (diff/60%60).toString()
    return "어제보다 " + diffTotalHour+"시간 " + diffTotalMin + "분 " + moreOrLess
}