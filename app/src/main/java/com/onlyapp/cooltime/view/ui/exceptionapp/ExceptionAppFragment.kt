package com.onlyapp.cooltime.view.ui.exceptionapp

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.onlyapp.cooltime.common.Constants
import com.onlyapp.cooltime.data.ExceptAppRepository
import com.onlyapp.cooltime.data.ExceptAppRepositoryImpl
import com.onlyapp.cooltime.data.UserDatabase
import com.onlyapp.cooltime.data.entity.ExceptApp
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
    private var recyclerViewState: Parcelable? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentExceptionAppBinding.inflate(layoutInflater, container, false)
        val act = checkNotNull(activity) { "MainActivty is Null" }
        val db =
            checkNotNull(UserDatabase.getInstance(act.applicationContext)) { "UserDatabase is Null" }
        val repository = ExceptAppRepositoryImpl(db.exceptAppsDao())
        val packageManager = act.packageManager

        val exceptViewModel = ExceptAppViewModel(repository) { exceptApp ->
            val appInfo =
                packageManager.getApplicationInfo(
                    exceptApp.packageName,
                    PackageManager.GET_META_DATA
                )
            Pair(
                appInfo.loadLabel(packageManager).toString(),
                appInfo.loadIcon(packageManager)
            )

        }

        lifecycleScope.launch {
            val launcherIntent = Intent(Intent.ACTION_MAIN)
            launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER)
            val resolveInfoList = packageManager.queryIntentActivities(launcherIntent, 0)

            withContext(Dispatchers.IO) {
                for (resolveInfo in resolveInfoList) {
                    val appInfo = resolveInfo.activityInfo.applicationInfo
                    val packageName = appInfo.packageName
                    if (packageName == Constants.coolTimePackageName) continue
                    val result = async { exceptViewModel.getApp(packageName) }.await()
                    //설치되어 있는 앱인데 아직 DB에 들어있지 않은 앱이라면 insert
                    if (result != null) continue
                    else {  //설치되어 있는 앱인데 아직 DB에 들어있지 않은 앱이라면 insert
                        val appInfo =
                            packageManager.getApplicationInfo(
                                packageName,
                                PackageManager.GET_META_DATA
                            )
                        exceptViewModel.insertApp(
                            ExceptAppItem(
                                appInfo.loadLabel(packageManager).toString(),
                                packageName,
                                appInfo.loadIcon(packageManager),
                                false
                            )
                        )
                    }
                }
            }

            lifecycleScope.launch {
                binding.checkedException.layoutManager =
                    LinearLayoutManager(this@ExceptionAppFragment.context)

                exceptViewModel.exceptAppItemList.observe(
                    this@ExceptionAppFragment
                ) {
                    CoroutineScope(Dispatchers.Main).launch {
                        val exceptAppList = mutableListOf<ExceptAppItem>()
                        withContext(Dispatchers.IO) {
                            it.forEach { app ->
                                try {
                                    Log.d("exceptApp", app.toString())
                                    val appInfo =
                                        packageManager.getApplicationInfo(
                                            app.packageName,
                                            PackageManager.GET_META_DATA
                                        )
                                    exceptAppList.add(
                                        ExceptAppItem(
                                            appInfo.loadLabel(packageManager).toString(),
                                            app.packageName,
                                            appInfo.loadIcon(packageManager),
                                            app.checked
                                        )
                                    )
                                } catch (e: Exception) {   //삭제되었는데 table에 저장되어 있는 앱인 경우
                                    exceptViewModel.deleteApp(app.packageName)
                                }
                            }
                        }

                        binding.checkedException.adapter =
                            AppAdapter(exceptAppList) { item ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    withContext(Dispatchers.Main) {
                                        recyclerViewState =
                                            binding.checkedException.layoutManager!!.onSaveInstanceState()!! //현재 스크롤 위치를 저장
                                    }
                                    exceptViewModel.updateApp(item)
                                }
                            }
                        if (recyclerViewState != null) {
                            binding.checkedException.layoutManager!!.onRestoreInstanceState(
                                recyclerViewState
                            )    //업데이트 후에 복원
                        }
                    }
                }
            }

        }
        return binding.root
    }
}