package com.onlyapp.cooltime.view.ui.exceptionApp

import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.onlyapp.cooltime.data.ExceptAppRepository
import com.onlyapp.cooltime.data.UserDatabase
import com.onlyapp.cooltime.databinding.FragmentExceptionAppBinding
import com.onlyapp.cooltime.data.entity.ExceptApp
import com.onlyapp.cooltime.model.ExceptAppItem
import com.onlyapp.cooltime.view.adapter.AppAdapter
import com.onlyapp.cooltime.view.adapter.OnCheckBoxChangedListener
import com.onlyapp.cooltime.view.viewmodel.ExceptAppViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ExceptionAppFragment : Fragment(){
    private var recyclerViewState : Parcelable? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentExceptionAppBinding.inflate(layoutInflater, container, false)

        val db = UserDatabase.getInstance(activity!!.applicationContext)
        val repository = ExceptAppRepository(db!!.exceptAppsDao())
        val exceptViewModel = ExceptAppViewModel(repository)

        val packageManager = this@ExceptionAppFragment.activity!!.packageManager

        CoroutineScope(Dispatchers.Main).launch {
            val packages: List<PackageInfo> = async(Dispatchers.IO) {
                packageManager.getInstalledPackages(PackageManager.MATCH_DEFAULT_ONLY)
            }.await()
            val apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

            val launcherIntent = Intent(Intent.ACTION_MAIN)
            launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER)

            val resolveInfoList = packageManager.queryIntentActivities(launcherIntent, 0)

            withContext(Dispatchers.IO) {
                for(resolveInfo in resolveInfoList){
                    val appInfo = resolveInfo.activityInfo.applicationInfo
                    val packageName = appInfo.packageName
                    if(packageName == "com.onlyapp.cooltime") continue
                    val result = exceptViewModel.getApp(packageName)
                    if (result != null) continue
                    else {  //설치되어 있는 앱인데 아직 DB에 들어있지 않은 앱이라면 insert
                        exceptViewModel.insertApp(
                            ExceptApp(
                                packageName = packageName,
                                checked = false
                            )
                        )
                    }

                }

            }

            CoroutineScope(Dispatchers.Main).launch {
                binding.checkedException.layoutManager =
                    LinearLayoutManager(this@ExceptionAppFragment.context)

                exceptViewModel.exceptApp.observe(
                    this@ExceptionAppFragment,
                    Observer<List<ExceptApp>> {
                        CoroutineScope(Dispatchers.Main).launch {
                            val exceptAppList = mutableListOf<ExceptAppItem>()
                            withContext(Dispatchers.IO) {
                                    it.forEach {
                                        try {
                                            Log.d("exceptApp", it.toString())
                                            val appInfo =
                                                packageManager.getApplicationInfo(
                                                    it.packageName,
                                                    PackageManager.GET_META_DATA)

                                            exceptAppList.add(
                                                ExceptAppItem(
                                                    appInfo.loadLabel(packageManager)!!.toString(),
                                                    it.packageName,
                                                    appInfo.loadIcon(packageManager),
                                                    it.checked
                                                )
                                            )
                                        } catch (e: Exception) {   //삭제되었는데 table에 저장되어 있는 앱인 경우
                                            exceptViewModel.deleteApp(it.packageName)
                                        }
                                    }
                            }


                            binding.checkedException.adapter =
                                AppAdapter(exceptAppList, object : OnCheckBoxChangedListener {
                                    override fun onChanged(item: ExceptAppItem, position: Int) {
                                        CoroutineScope(Dispatchers.IO).launch {
                                            withContext(Dispatchers.Main) {
                                                recyclerViewState =
                                                    binding.checkedException!!.layoutManager!!.onSaveInstanceState()!! //현재 스크롤 위치를 저장
                                            }
                                            exceptViewModel.updateApp(
                                                ExceptApp(
                                                    item.packageName,
                                                    item.checked
                                                )
                                            )

                                        }
                                    }
                                })

                            if(recyclerViewState != null){
                                binding.checkedException!!.layoutManager!!.onRestoreInstanceState(
                                    recyclerViewState
                                )    //업데이트 후에 복원
                            }

                        }
                    })

            }
        }

        return binding.root
    }
}