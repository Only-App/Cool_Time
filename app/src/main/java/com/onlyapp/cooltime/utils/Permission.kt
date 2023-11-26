package com.onlyapp.cooltime.utils

import android.app.Activity
import android.app.AppOpsManager
import android.content.Context
import android.content.Context.POWER_SERVICE
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity

//Permissino 클래스는 권한을 요청하거나 권한 설정이 되어 있는지 체크하는 클래스
object Permission {
    // 리사이클러 뷰 생성 순서대로(코드 값이 곧 인덱스) 설정 => code 값 보고서 어떤 권한 눌렀는지 알아내고, 해당 리스트에 접근해야 되기 때문에
    // => 요청 코드 값이 리스트에서 해당 권한의 index와 동일하도록 설정해줌=> 코드 == index가 성립하도록

    fun checkOverlayPermission(activity: Activity): Boolean {
        val appOpsManager = activity.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOpsManager.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW,
            android.os.Process.myUid(),
            activity.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun checkUsageStatsPermission(activity: Activity): Boolean {
        val appOpsManager = activity.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOpsManager.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            activity.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun checkBatteryPermission(activity: Activity): Boolean {
        val powerManager = activity.getSystemService(POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(activity.packageName)
    }

    fun checkAllPermission(activity: Activity): Boolean {
        return checkUsageStatsPermission(activity) &&
                checkOverlayPermission(activity) &&
                checkBatteryPermission(activity)
    }

    fun requestOverlayPermission(activity: AppCompatActivity, activityResultLauncher: ActivityResultLauncher<Intent>) {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:" + activity.packageName)
        )
        activityResultLauncher.launch(intent)
    }

    fun requestUsageStatsPermission(activity: AppCompatActivity, activityResultLauncher: ActivityResultLauncher<Intent>) {
        val intent = Intent(
            Settings.ACTION_USAGE_ACCESS_SETTINGS,
            Uri.parse("package:" + activity.packageName)
        )
        activityResultLauncher.launch(intent)

        /*
        이제 안쓰지만 혹시 모를 경우를 대비해서 원본 남겨둠
                val requestUsageStatsPermissionLauncher =
                    activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                        // 사용자가 설정 화면에서 돌아왔을 때, 권한이 부여되었는지 확인
                        if (checkUsageStatsPermission(activity)) {
                            // 권한이 부여되었다면 버튼 완료 처리
                            mCompleteButton.invoke()
                        }
                    }
        //ActivityCompat.startActivityForResult(activity, intent, usageStatsPermissionRequest, null)

         */
    }

    fun requestIgnoringBatteryOptimization(activity: AppCompatActivity, activityResultLauncher: ActivityResultLauncher<Intent>) {
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
        intent.data = Uri.parse("package:${activity.packageName}")
        activityResultLauncher.launch(intent)
    }
}
