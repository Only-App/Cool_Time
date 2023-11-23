package com.onlyapp.cooltime.utils

import android.app.Activity
import android.app.AppOpsManager
import android.content.Context
import android.content.Context.POWER_SERVICE
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.core.app.ActivityCompat

//Permissino 클래스는 권한을 요청하거나 권한 설정이 되어 있는지 체크하는 클래스
object Permission{
    // 리사이클러 뷰 생성 순서대로(코드 값이 곧 인덱스) 설정 => code 값 보고서 어떤 권한 눌렀는지 알아내고, 해당 리스트에 접근해야 되기 때문에
    // => 요청 코드 값이 리스트에서 해당 권한의 index와 동일하도록 설정해줌=> 코드 == index가 성립하도록
    private const val usageStatsPermissionRequest = 0
    private const val overlayPermissionRequestCode = 1
    private const val batteryPermissionRequest = 2
    //private val resultLauncher
    fun checkOverlayPermission(activity: Activity):Boolean{
        val appOpsManager = activity.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOpsManager.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW,
            android.os.Process.myUid(),
            activity.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }
    fun checkUsageStatsPermission(activity: Activity):Boolean{
        val appOpsManager = activity.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOpsManager.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            activity.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun checkBatteryPermission(activity: Activity):Boolean{
        val powerManager = activity.getSystemService(POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(activity.packageName)
    }
    fun checkAllPermission(activity: Activity):Boolean{
        return checkUsageStatsPermission(activity)&&
                checkOverlayPermission(activity)&&
                checkBatteryPermission(activity)
    }
    fun requestOverlayPermission(activity: Activity) {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:" + activity.packageName)
        )
        ActivityCompat.startActivityForResult(activity, intent, overlayPermissionRequestCode, null)
    }

    fun requestUsageStatsPermission(activity: Activity) {
        val intent = Intent(
            Settings.ACTION_USAGE_ACCESS_SETTINGS,
            Uri.parse("package:" + activity.packageName)
        )
        ActivityCompat.startActivityForResult(activity, intent, usageStatsPermissionRequest, null)
    }

    fun requestIgnoringBatteryOptimization(activity: Activity){
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
        intent.data = Uri.parse("package:${activity.packageName}")
        ActivityCompat.startActivityForResult(activity, intent, batteryPermissionRequest, null)
    }
}
