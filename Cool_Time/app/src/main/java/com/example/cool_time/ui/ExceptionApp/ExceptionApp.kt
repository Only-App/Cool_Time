package com.example.cool_time.ui.ExceptionApp

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cool_time.databinding.FragmentExceptionAppBinding
import com.example.cool_time.viewmodel.AppItem
import com.example.cool_time.viewmodel.AppAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ExceptionApp : Fragment(){
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentExceptionAppBinding.inflate(layoutInflater, container, false)

        CoroutineScope(Dispatchers.Main).launch {
            val uncheckeddatas = ObservableArrayList<AppItem>()
            val checkeddatas = ObservableArrayList<AppItem>()
            var count = 0
            val packageManager = this@ExceptionApp.activity!!.packageManager
            val packages: List<PackageInfo> = async(Dispatchers.Default) {
                packageManager.getInstalledPackages(PackageManager.MATCH_DEFAULT_ONLY)
            }.await()
            withContext(Dispatchers.Default) {
                for (info: PackageInfo in packages) {
                    if (info.applicationInfo.name != null) {
                        val iticon: Drawable = info.applicationInfo.loadIcon(packageManager)
                        val it =
                            AppItem(
                                info.applicationInfo.loadLabel(packageManager).toString(),
                                iticon
                            )
                        uncheckeddatas.add(it)
                        count += 1
                    }
                }
            }


            val checkedadapter = AppAdapter(checkeddatas, binding.checkedException, uncheckeddatas,)
            val uncheckedadapter =
                AppAdapter(uncheckeddatas, binding.uncheckedException, checkeddatas)
            binding.checkedException.adapter = checkedadapter
            binding.uncheckedException.adapter = uncheckedadapter



            uncheckeddatas.addOnListChangedCallback(object :
                ObservableList.OnListChangedCallback<ObservableList<AppItem>>() {
                override fun onChanged(sender: ObservableList<AppItem>?) {
                    uncheckedadapter.notifyDataSetChanged()
                }

                override fun onItemRangeChanged(
                    sender: ObservableList<AppItem>?,
                    positionStart: Int,
                    itemCount: Int
                ) {
                    // 특정 범위 내의 항목이 변경될 때 수행할 작업
                    uncheckedadapter.notifyDataSetChanged()
                }

                override fun onItemRangeInserted(
                    sender: ObservableList<AppItem>?,
                    positionStart: Int,
                    itemCount: Int
                ) {
                    uncheckedadapter.notifyDataSetChanged()
                }

                override fun onItemRangeMoved(
                    sender: ObservableList<AppItem>?,
                    fromPosition: Int,
                    toPosition: Int,
                    itemCount: Int
                ) {
                    uncheckedadapter.notifyDataSetChanged()
                }

                override fun onItemRangeRemoved(
                    sender: ObservableList<AppItem>?,
                    positionStart: Int,
                    itemCount: Int
                ) {
                    uncheckedadapter.notifyDataSetChanged()
                }
            })

            checkeddatas.addOnListChangedCallback(object :
                ObservableList.OnListChangedCallback<ObservableList<AppItem>>() {
                override fun onChanged(sender: ObservableList<AppItem>?) {
                    checkedadapter.notifyDataSetChanged()
                }

                override fun onItemRangeChanged(
                    sender: ObservableList<AppItem>?,
                    positionStart: Int,
                    itemCount: Int
                ) {
                    // 특정 범위 내의 항목이 변경될 때 수행할 작업
                    checkedadapter.notifyDataSetChanged()
                }

                override fun onItemRangeInserted(
                    sender: ObservableList<AppItem>?,
                    positionStart: Int,
                    itemCount: Int
                ) {
                    checkedadapter.notifyDataSetChanged()
                }

                override fun onItemRangeMoved(
                    sender: ObservableList<AppItem>?,
                    fromPosition: Int,
                    toPosition: Int,
                    itemCount: Int
                ) {
                    checkedadapter.notifyDataSetChanged()
                }

                override fun onItemRangeRemoved(
                    sender: ObservableList<AppItem>?,
                    positionStart: Int,
                    itemCount: Int
                ) {
                    checkedadapter.notifyDataSetChanged()
                }
            })

            binding.uncheckedException.layoutManager =
                LinearLayoutManager(this@ExceptionApp.activity, LinearLayoutManager.VERTICAL, false)
            binding.checkedException.layoutManager =
                LinearLayoutManager(this@ExceptionApp.activity, LinearLayoutManager.VERTICAL, false)
        }
        return binding.root
    }
}