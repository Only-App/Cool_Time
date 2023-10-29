package com.example.cool_time.utils

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
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
    val calendar = Calendar.getInstance()
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

fun load_usage(context: Context, startday:Long, endday:Long): ArrayList<Pair<String, Long>> {
    val packageManager = context.packageManager
    val packages: List<PackageInfo> = packageManager.getInstalledPackages(PackageManager.MATCH_DEFAULT_ONLY)
    var count = 0
    val list = ArrayList<Pair<String, Long>>()
    for (info: PackageInfo in packages) {
        if (info.applicationInfo.name != null) {
            val it = info.applicationInfo.packageName.toString()
            if (getAppUsageStats(context, it, startday, endday).second != 0L) {
                list.add(getAppUsageStats(context!!, it, startday, endday))
                count += 1
            }
        }
    }
    list.sortWith(
        Comparator { left, right ->
            compareValues(left.second, right.second)
            //compareValues(left.lastTimeUsed, right.lastTimeUsed)
        },
    )
    return list
}

fun load_time_usage(context: Context, calendar:Calendar): ArrayList<Long> {
    val packageManager = context.packageManager
    val packages: List<PackageInfo> = packageManager.getInstalledPackages(PackageManager.MATCH_DEFAULT_ONLY)
    var count = 0
    val list = ArrayList<Long>()
    for(i in 0 until 24) {
        val startday = calendar.clone() as Calendar
        startday.set(Calendar.HOUR_OF_DAY, i)
        var endday = calendar.clone() as Calendar
        endday.set(Calendar.HOUR_OF_DAY, i)
        endday.set(Calendar.MINUTE, 59)
        endday.set(Calendar.SECOND, 59)
        endday.set(Calendar.MILLISECOND, 999)
        var totalTime = 0L
        for (info: PackageInfo in packages) {
            if (info.applicationInfo.name != null) {
                val it = info.applicationInfo.packageName.toString()
                if (getAppUsageStats(context, it, startday.timeInMillis, endday.timeInMillis).second != 0L) {
                    totalTime += getAppUsageStats(context!!, it, startday.timeInMillis, endday.timeInMillis).second
                    count += 1
                }
            }
        }
        list.add(totalTime)
    }
    return list
}

fun getAppUsageStats(context: Context, packageName: String?, beginTime: Long, endTime: Long): Pair<String, Long> {
    val allEvents = mutableListOf<UsageEvents.Event>()
    val appUsageMap = mutableMapOf<String, Long>()
    val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    val usageEvents = usageStatsManager.queryEvents(beginTime, endTime)

    while (usageEvents.hasNextEvent()) {
        val currentEvent = UsageEvents.Event()
        usageEvents.getNextEvent(currentEvent)
        if (currentEvent.packageName == packageName || packageName == null) {
            if (currentEvent.eventType == UsageEvents.Event.ACTIVITY_RESUMED
                || currentEvent.eventType == UsageEvents.Event.ACTIVITY_PAUSED) {
                allEvents.add(currentEvent)
                val key = currentEvent.packageName
                appUsageMap.putIfAbsent(key, 0L)
            }
        }
    }

    for (i in 0 until allEvents.size - 1) {
        val E0 = allEvents[i]
        val E1 = allEvents[i + 1]

        if (E0.eventType == UsageEvents.Event.ACTIVITY_RESUMED
            && E1.eventType == UsageEvents.Event.ACTIVITY_PAUSED
            && E0.className == E1.className) {
            val diff = ((E1.timeStamp - E0.timeStamp))/1000.toLong()
            val prev = appUsageMap[E0.packageName] ?: 0L
            appUsageMap[E0.packageName] = prev + diff
        }
    }

    if(allEvents.size != 0) {
        /*
        val lastEvent = allEvents[allEvents.size - 1]
        if (lastEvent.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
            var diff = endTime - lastEvent.timeStamp
            diff /= 1000
            var prev = appUsageMap[lastEvent.packageName]
            if (prev == null) prev = 0
            appUsageMap[lastEvent.packageName] = prev + diff
        }

         */
        return Pair(packageName.toString(), appUsageMap[packageName]!!.toLong())
    }
    else {
        return Pair(packageName.toString(), 0L)
    }
}

fun getTotalTime(list :  ArrayList<Pair<String, Long>>): Long {
    var totalTime = 0L;
    for( i in 0 until list.size){
        totalTime+= list[i].second
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