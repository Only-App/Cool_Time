package com.onlyapp.cooltime.view.ui.exceptionapp

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.onlyapp.cooltime.common.Constants
import com.onlyapp.cooltime.data.ExceptAppRepositoryImpl
import com.onlyapp.cooltime.data.UserDatabase
import com.onlyapp.cooltime.databinding.FragmentExceptionAppBinding
import com.onlyapp.cooltime.model.ExceptAppItem
import com.onlyapp.cooltime.view.adapter.AppAdapter
import com.onlyapp.cooltime.view.viewmodel.ExceptAppViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ExceptionAppFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val binding = FragmentExceptionAppBinding.inflate(layoutInflater, container, false)
        val act = checkNotNull(activity) { "MainActivty is Null" }
        val db =
            checkNotNull(UserDatabase.getInstance(act.applicationContext)) { "UserDatabase is Null" }
        val repository = ExceptAppRepositoryImpl(db.exceptAppsDao())
        val packageManager = act.packageManager

        val exceptViewModel = ExceptAppViewModel(repository) { exceptApp ->
            val appInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getApplicationInfo(
                    exceptApp.packageName,
                    PackageManager.ApplicationInfoFlags.of(0)
                )
            } else {
                packageManager.getApplicationInfo(
                    exceptApp.packageName,
                    PackageManager.GET_META_DATA
                )
            }
            Pair(
                appInfo.loadLabel(packageManager).toString(), appInfo.loadIcon(packageManager)
            )
        }

        lifecycleScope.launch {
            val launcherIntent = Intent(Intent.ACTION_MAIN)
            launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER)
            val resolveInfoList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.queryIntentActivities(
                    launcherIntent,
                    PackageManager.ResolveInfoFlags.of(PackageManager.GET_META_DATA.toLong())
                )
            } else {
                packageManager.queryIntentActivities(launcherIntent, 0)
            }

            withContext(Dispatchers.IO) {
                for (resolveInfo in resolveInfoList) {
                    val appInfo = resolveInfo.activityInfo.applicationInfo
                    val packageName = appInfo.packageName
                    if (packageName == Constants.coolTimePackageName) continue
                    val result = async { exceptViewModel.getApp(packageName) }.await()
                    //설치되어 있는 앱인데 아직 DB에 들어있지 않은 앱이라면 insert
                    if (result != null) continue
                    else {  //설치되어 있는 앱인데 아직 DB에 들어있지 않은 앱이라면 insert
                        val applicationInfo =
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                packageManager.getApplicationInfo(
                                    packageName,
                                    PackageManager.ApplicationInfoFlags.of(0)
                                )
                            } else {
                                packageManager.getApplicationInfo(
                                    packageName,
                                    PackageManager.GET_META_DATA
                                )
                            }
                        exceptViewModel.insertApp(
                            ExceptAppItem(
                                applicationInfo.loadLabel(packageManager).toString(),
                                packageName,
                                applicationInfo.loadIcon(packageManager),
                                false
                            )
                        )
                    }
                }
            }

            val adapter = AppAdapter { item ->
                CoroutineScope(Dispatchers.IO).launch {
                    exceptViewModel.updateApp(item)
                }
            }

            binding.checkedException.itemAnimator = null
            binding.checkedException.adapter = adapter
            binding.checkedException.layoutManager =
                LinearLayoutManager(this@ExceptionAppFragment.context)

            lifecycleScope.launch {
                exceptViewModel.exceptAppItemList.observe(
                    this@ExceptionAppFragment
                ) {
                    adapter.replaceItems(it)
                    it.forEach { app ->
                        try {
                            Log.d("exceptApp", app.toString())
                            val appInfo = packageManager.getApplicationInfo(
                                app.packageName, PackageManager.GET_META_DATA
                            )

                        } catch (e: Exception) {   //삭제되었는데 table에 저장되어 있는 앱인 경우
                            exceptViewModel.deleteApp(app.packageName)
                        }
                    }

                }
            }

        }
        return binding.root
    }
}
