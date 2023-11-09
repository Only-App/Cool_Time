package com.example.cool_time.utils

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
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
    val calendar = Calendar.getInstance()
    return calendar
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


fun getAppUsageStatsAsync(context : Context, beginTime : Long, endTime : Long)
    = CoroutineScope(Dispatchers.Default).async{
    val appUsageMap = mutableMapOf<String, Long>()
    val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    val packageManager = context.packageManager
    val usageEvents = usageStatsManager.queryEvents(beginTime, endTime)

    val list :MutableMap<String, ArrayList<Triple<String, Int, Long>>> = mutableMapOf<String, ArrayList<Triple<String, Int,Long>>>()
    while (usageEvents.hasNextEvent()) {
        val currentEvent = UsageEvents.Event()
        usageEvents.getNextEvent(currentEvent)
        if(currentEvent.eventType == UsageEvents.Event.ACTIVITY_RESUMED
            || currentEvent.eventType == UsageEvents.Event.ACTIVITY_PAUSED) {
            if (list[currentEvent.packageName] == null) {
                list.putIfAbsent(currentEvent.packageName, ArrayList<Triple<String, Int, Long>>())
                list[currentEvent.packageName]!!.add(Triple(currentEvent.className, currentEvent.eventType, currentEvent.timeStamp))
            } else {
                list[currentEvent.packageName]!!.add(Triple(currentEvent.className, currentEvent.eventType, currentEvent.timeStamp))
            }
        }
    }

    for((key, value) in list){
        val packageName = key
        if(packageName == "com.example.cool_time") continue
        if(packageManager.getLaunchIntentForPackage(packageName) != null ) {
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
        }
    }
    appUsageMap.toList().sortedBy { it.second }.toMap()
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
        if(currentEvent.eventType == UsageEvents.Event.ACTIVITY_RESUMED
            || currentEvent.eventType == UsageEvents.Event.ACTIVITY_PAUSED) {
            if (list[currentEvent.packageName] == null) {
                list.putIfAbsent(currentEvent.packageName, ArrayList<Triple<String, Int, Long>>())
                list[currentEvent.packageName]!!.add(Triple(currentEvent.className, currentEvent.eventType, currentEvent.timeStamp))
            } else {
                list[currentEvent.packageName]!!.add(Triple(currentEvent.className, currentEvent.eventType, currentEvent.timeStamp))
            }
        }
    }

    for((key, value) in list){
        val packageName = key
        if(packageName == "com.example.cool_time") continue
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
        }
    }
    return appUsageMap.toList().sortedBy { it.second }.toMap()
}

fun loadUsage(context : Context, startDay : Long, endDay : Long): List<Pair<String, Long>> {
        val stats = getAppUsageStats(context, startDay, endDay)
        return stats.toList()
}

fun loadUsageAsync(context : Context, startDay : Long, endDay : Long) =
    CoroutineScope(Dispatchers.Default).async{
        val stats = getAppUsageStatsAsync(context, startDay, endDay).await()
        stats.toList()
}


fun loadTimeUsageAsync(context : Context, calendar : Calendar) =
    CoroutineScope(Dispatchers.Default).async{
        val list = ArrayList<Long>()
        for (i in 0 until 24) {
            val startday = calendar.clone() as Calendar
            startday.set(Calendar.HOUR_OF_DAY, i)
            var endday = calendar.clone() as Calendar
            endday.set(Calendar.HOUR_OF_DAY, i)
            endday.set(Calendar.MINUTE, 59)
            endday.set(Calendar.SECOND, 59)
            endday.set(Calendar.MILLISECOND, 999)
            var totalTime = 0L

            val tmp = getAppUsageStatsAsync(
                context!!,
                startday.timeInMillis,
                endday.timeInMillis
            ).await()
            for(i in tmp) {
                totalTime += i.value
            }
            list.add(totalTime)
        }
        list
}



fun getTotalTime(list :  List<Pair<String, Long>>): Long {
    var totalTime = 0L;
    for(element in list){
        totalTime+= element.second
    }
    return totalTime
}

fun totalTimetoText(totalTime:Long): String {
    val totalHour = (totalTime/3600).toString()
    val totalMin = (totalTime/60%60).toString()
    val totalSec = (totalTime%60).toString()
    val displayTotalTime = totalHour+" : " + totalMin + " : " + totalSec
    return displayTotalTime
}

fun getDiff( totalTime:Long, yesterdayTotalTime:Long ): String {
    var diff = yesterdayTotalTime - totalTime
    var moreOrless = if(diff < 0){"더 사용"} else{ "덜 사용"}
    diff = Math.abs(diff)
    val difftotalHour = (diff/3600).toString()
    val difftotalMin = (diff/60%60).toString()
    val displaydiffTime = "어제보다 " + difftotalHour+"시간 " + difftotalMin + "분 " + moreOrless
    return displaydiffTime
}