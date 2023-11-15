package com.onlyapp.cooltime.utils

import android.Manifest
import android.app.Activity
import android.app.AppOpsManager
import android.content.Context
import android.content.Context.POWER_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

//Permissino 클래스는 권한을 요청하거나 권한 설정이 되어 있는지 체크하는 클래스
class Permission(val activity: Activity){
    // 리사이클러 뷰 생성 순서대로(코드 값이 곧 인덱스) 설정 => code 값 보고서 어떤 권한 눌렀는지 알아내고, 해당 리스트에 접근해야 되기 때문에
    // => 요청 코드 값이 리스트에서 해당 권한의 index와 동일하도록 설정해줌=> 코드 == index가 성립하도록
    val USAGE_STATS_PERMISSION_REQUEST = 0
    val OVERLAY_PERMISSION_REQUEST_CODE = 1
    val CALL_PERMISSION_REQUEST_CODE = 2
    val NOTIFICATION_PERMISSION_REQUEST = 3
    val BATTERY_PERMISSION_REQUEST = 4

    fun checkOverlayPermission():Boolean{
        val appOpsManager = activity.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOpsManager.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW,
            android.os.Process.myUid(),
            activity.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }
    fun checkUsageStatsPermission():Boolean{
        val appOpsManager = activity.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOpsManager.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            activity.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun checkCallPermission():Boolean{
        return ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED
    }
    fun checkNotificationPermission():Boolean{
        return ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }
    fun checkBatteryPermission():Boolean{
        val powerManager = activity.getSystemService(POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(activity.packageName)
    }
    fun checkAllPermission():Boolean{
        return checkUsageStatsPermission()&&
                checkOverlayPermission()&&
                checkNotificationPermission()&&
                checkCallPermission()&&
                checkBatteryPermission()
    }
    internal fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + activity.packageName)
            )
            ActivityCompat.startActivityForResult(activity, intent, OVERLAY_PERMISSION_REQUEST_CODE, null)
        }
    }

    internal fun requestUsageStatsPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_USAGE_ACCESS_SETTINGS,
                Uri.parse("package:" + activity.packageName)
            )
            ActivityCompat.startActivityForResult(activity, intent, USAGE_STATS_PERMISSION_REQUEST, null)
        }
    }

    internal fun requestCallPermission() {
        ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.CALL_PHONE), CALL_PERMISSION_REQUEST_CODE)
    }

    internal fun requestNotificationPermission() {
        ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.POST_NOTIFICATIONS), NOTIFICATION_PERMISSION_REQUEST)
    }

    internal fun requestIgnoringBatteryOptimization(){
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
        intent.data = Uri.parse("package:${activity.packageName}")
        ActivityCompat.startActivityForResult(activity, intent, BATTERY_PERMISSION_REQUEST, null)
    }
}