package com.onlyapp.cooltime.utils

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.util.Log
import com.onlyapp.cooltime.R
import java.util.Calendar
import kotlin.math.abs

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

fun getTomorrowStart(): Calendar {
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

fun getSomedayStart(year: Int, month: Int, day: Int): Calendar {
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

fun getSomedayEnd(year: Int, month: Int, day: Int): Calendar {
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


fun getAppUsageStats(context: Context, beginTime: Long, endTime: Long): Map<String, Long> {
    val appUsageMap = mutableMapOf<String, Long>()
    val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    val packageManager = context.packageManager
    val usageEvents = usageStatsManager.queryEvents(beginTime, endTime)

    val list: MutableMap<String, ArrayList<Triple<String, Int, Long>>> = mutableMapOf()
    while (usageEvents.hasNextEvent()) {
        val currentEvent = UsageEvents.Event()
        usageEvents.getNextEvent(currentEvent)
        when (currentEvent.eventType) {
            UsageEvents.Event.ACTIVITY_RESUMED, UsageEvents.Event.ACTIVITY_PAUSED, UsageEvents.Event.ACTIVITY_STOPPED -> {
                if (list[currentEvent.packageName] == null) {
                    list.putIfAbsent(currentEvent.packageName, ArrayList())
                    list[currentEvent.packageName]?.add(Triple(currentEvent.className, currentEvent.eventType, currentEvent.timeStamp))
                } else {
                    list[currentEvent.packageName]?.add(Triple(currentEvent.className, currentEvent.eventType, currentEvent.timeStamp))
                }
            }
        }
    }

    for ((packageName, value) in list) {
        if (packageName == "com.onlyapp.cooltime") continue
        if (packageManager.getLaunchIntentForPackage(packageName) != null) {
            if (appUsageMap[packageName] == null) {
                appUsageMap.putIfAbsent(packageName, 0L)
            }
            for (i in 0 until value.size - 1) {
                val e0 = value[i]
                val e1 = value[i + 1]
                if (//E0.first == E1.first &&
                    (e0.second == UsageEvents.Event.ACTIVITY_RESUMED &&
                    e1.second == UsageEvents.Event.ACTIVITY_PAUSED) ||
                    (e0.second == UsageEvents.Event.ACTIVITY_RESUMED &&
                            e1.second == UsageEvents.Event.ACTIVITY_STOPPED)
                ) {
                    val diff = ((e1.third - e0.third)) / 1000.toLong()
                    val prev = appUsageMap[packageName] ?: 0L
                    appUsageMap[packageName] = prev + diff
                }
            }
            val lastEvent = value[value.size - 1]
            val firstEvent = value[0]
            if (firstEvent.second == UsageEvents.Event.ACTIVITY_PAUSED || firstEvent.second == UsageEvents.Event.ACTIVITY_STOPPED){
                val prev = appUsageMap[packageName] ?: 0L
                appUsageMap[packageName] = prev + ((firstEvent.third - beginTime) / 1000).toLong()
            }
            if (lastEvent.second == UsageEvents.Event.ACTIVITY_RESUMED) {
                val prev = appUsageMap[packageName] ?: 0L
                appUsageMap[packageName] = prev + ((endTime - lastEvent.third) / 1000).toLong()

            }

        }

    }

    return appUsageMap.toList().sortedBy { it.second }.toMap()
}

fun checkContinueUsage(context: Context, beginTime: Long, endTime: Long, target: String ): Triple<Boolean, String, String> {
    val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    val packageManager = context.packageManager
    val usageEvents = usageStatsManager.queryEvents(beginTime, endTime)

    val list: MutableMap<String, ArrayList<Triple<String, Int, Long>>> = mutableMapOf()
    while (usageEvents.hasNextEvent()) {
        val currentEvent = UsageEvents.Event()
        usageEvents.getNextEvent(currentEvent)
        when (currentEvent.eventType) {
            UsageEvents.Event.ACTIVITY_RESUMED, UsageEvents.Event.ACTIVITY_PAUSED, UsageEvents.Event.ACTIVITY_STOPPED -> {
                if (list[currentEvent.packageName] == null) {
                    list.putIfAbsent(currentEvent.packageName, ArrayList())
                    list[currentEvent.packageName]?.add(Triple(currentEvent.className, currentEvent.eventType, currentEvent.timeStamp))
                } else {
                    list[currentEvent.packageName]?.add(Triple(currentEvent.className, currentEvent.eventType, currentEvent.timeStamp))
                }
            }
        }
    }

    for ((packageName, value) in list) {
        if (packageName == "com.onlyapp.cooltime") continue
        if (packageManager.getLaunchIntentForPackage(packageName) != null) {
            val lastEvent = value[value.size - 1]
            val firstEvent = value[0]
            if(target == "Start"){
                if (lastEvent.second == UsageEvents.Event.ACTIVITY_RESUMED) {
                    return Triple(true, "Start" ,packageName)
                }
            }
            if(target == "End"){
                Log.d("tstst", packageName + "  " + firstEvent.second)
                if (firstEvent.second == UsageEvents.Event.ACTIVITY_PAUSED || firstEvent.second == UsageEvents.Event.ACTIVITY_STOPPED){
                    Log.d("tstst", packageName + " 성공")
                    return Triple(true, "End" , packageName)
                }
            }
        }
    }

    return Triple(false,"" ,  "")
}

fun loadUsage(context: Context, startDay: Long, endDay: Long): List<Pair<String, Long>> {
    val stats = getAppUsageStats(context, startDay, endDay)
    return stats.toList()
}

fun loadTimeUsage(context: Context, calendar: Calendar): ArrayList<Long> {
    val list = ArrayList<Long>()
    for (i in 0 until 24) {
        val startDay = calendar.clone() as Calendar
        startDay.set(Calendar.HOUR_OF_DAY, i)
        startDay.set(Calendar.MINUTE, 0)
        startDay.set(Calendar.SECOND, 0)
        startDay.set(Calendar.MILLISECOND, 0)
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
        if(usageStats.isEmpty()){
            var flag = false
            for( j in i-1 downTo  0){
                val searchLeftStart = calendar.clone() as Calendar
                searchLeftStart.set(Calendar.HOUR_OF_DAY, j)
                searchLeftStart.set(Calendar.MINUTE, 0)
                searchLeftStart.set(Calendar.SECOND, 0)
                searchLeftStart.set(Calendar.MILLISECOND, 0)
                val searchLeftEnd = calendar.clone() as Calendar
                searchLeftEnd.set(Calendar.HOUR_OF_DAY, j)
                searchLeftEnd.set(Calendar.MINUTE, 59)
                searchLeftEnd.set(Calendar.SECOND, 59)
                searchLeftEnd.set(Calendar.MILLISECOND, 999)
                val searchLeft = checkContinueUsage(
                    context,
                    searchLeftStart.timeInMillis,
                    searchLeftEnd.timeInMillis,
                    "Start"
                )
                if(getAppUsageStats(context, searchLeftStart.timeInMillis, searchLeftEnd.timeInMillis).isNotEmpty()
                    && !searchLeft.first){

                    break
                }
                if(searchLeft.first && searchLeft.second == "Start"){
                    for(k in i+1 until 24){
                        val searchRightStart = calendar.clone() as Calendar
                        searchRightStart.set(Calendar.HOUR_OF_DAY, k)
                        searchRightStart.set(Calendar.MINUTE, 0)
                        searchRightStart.set(Calendar.SECOND, 0)
                        searchRightStart.set(Calendar.MILLISECOND, 0)
                        val searchRightEnd = calendar.clone() as Calendar
                        searchRightEnd.set(Calendar.HOUR_OF_DAY, k)
                        searchRightEnd.set(Calendar.MINUTE, 59)
                        searchRightEnd.set(Calendar.SECOND, 59)
                        searchRightEnd.set(Calendar.MILLISECOND, 999)

                        val searchRight = checkContinueUsage(
                            context,
                            searchRightStart.timeInMillis,
                            searchRightEnd.timeInMillis,
                            "End"
                        )

                        if(getAppUsageStats(context, searchRightStart.timeInMillis, searchRightEnd.timeInMillis).isNotEmpty()
                            && !searchRight.first){
                            flag = true
                            Log.d("tstst", "실패")
                            break

                        }

                        if(searchRight.first && searchRight.second == "End" && (searchLeft.third == searchRight.third)){
                            totalTime += 3600
                            flag = true
                            break
                        }
                    }
                }
                if(flag){break}
            }
        }

        for (app in usageStats) {
            Log.d("tstst", i.toString() + " " + app.key + " " + app.value)
            totalTime += app.value
        }
        Log.d("tstst", i.toString() + "시간 total Time 은 " + (totalTime/60).toString())
        list.add(totalTime)
    }
    return list
}


fun getTotalTime(list: List<Pair<String, Long>>): Long {
    var totalTime = 0L
    for (element in list) {
        totalTime += element.second
    }
    return totalTime
}

fun getDiff(totalTime: Long, yesterdayTotalTime: Long, context: Context): String {
    var diff = yesterdayTotalTime - totalTime
    val moreOrLess = if (diff < 0) {
        "더 사용"
    } else {
        "덜 사용"
    }
    diff = abs(diff)
    val diffTotalHour = (diff / 3600).toString()
    val diffTotalMin = (diff / 60 % 60).toString()
    return context.getString(R.string.cmp_total_time_before, diffTotalHour, diffTotalMin, moreOrLess)
}