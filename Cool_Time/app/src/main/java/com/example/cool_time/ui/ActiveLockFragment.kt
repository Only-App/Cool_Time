package com.example.cool_time.ui

import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.cool_time.databinding.FragmentActiveLockBinding
import com.example.cool_time.viewmodel.GridSpacingItemDecoration
import com.example.cool_time.viewmodel.AppItem
import com.example.cool_time.viewmodel.LockScreenAdapter




class ActiveLockFragment: Fragment(){

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentActiveLockBinding.inflate(layoutInflater, container, false)
        //val _binding = FragmentPhoneLockBinding.inflate(layoutInflater)
        var togle = false;
        binding.scroll.visibility=View.GONE
        binding.lastUseComment.visibility=View.VISIBLE
        binding.lastUseTime.visibility=View.VISIBLE

        val datas = mutableListOf<AppItem>()
        val packageManager = this.activity!!.packageManager
        val packages:List<PackageInfo> = packageManager.getInstalledPackages(PackageManager.MATCH_DEFAULT_ONLY)
        var count = 0
        for(info: PackageInfo in packages){
            if(packageManager.getLaunchIntentForPackage(info.packageName) != null && info.applicationInfo.name != null) {
                /*
                val i = Intent().apply {
                    this.setClassName(info.packageName, )
                }

                 */
                /*
                try {
                    for (i in packages.indices) {
                        Log.d("app name", packages[i].packageName)
                        if (packages[i].packageName.startsWith("com.kakao.talk")) {
                            val intent = packageManager.getLaunchIntentForPackage("com.kakao.talk")
                            intent!!.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                            Log.d("tstst", "tstst")
                        }
                    }
                } catch (e: java.lang.Exception) {

                }
                 */
                val iticon: Drawable = info.applicationInfo.loadIcon(packageManager)
                val it = AppItem(info.packageName, iticon)
                datas.add(it)
                count += 1
            }
        }

        val adapter = LockScreenAdapter(this.activity!!, datas, packageManager)
        binding.appList.adapter = adapter
        binding.appList.layoutManager  = GridLayoutManager(this.activity!!, 3, GridLayoutManager.VERTICAL, false,)
        binding.appList.addItemDecoration(GridSpacingItemDecoration(spanCount = 3, spacing = 10))


        binding.menu.setOnClickListener{
            if(togle){
                binding.scroll.visibility=View.GONE
                binding.lastUseComment.visibility=View.VISIBLE
                binding.lastUseTime.visibility=View.VISIBLE
            }
            else{
                binding.scroll.visibility=View.VISIBLE
                binding.lastUseComment.visibility=View.GONE
                binding.lastUseTime.visibility=View.GONE


            }
            togle = !togle
        }


        return binding.root
    }
}