package com.example.cool_time.viewmodel

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Rect
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.cool_time.R
import com.example.cool_time.databinding.FragmentPermissionCheckBinding
import com.example.cool_time.databinding.PermissionItemRecyclerviewBinding
import com.example.cool_time.utils.Permission
import kotlinx.coroutines.runBlocking


class LinearDecorationSpace(private val divHeight: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        if (parent.getChildAdapterPosition(view) != parent.adapter?.itemCount?.minus(1)) {
            outRect.bottom = divHeight
        }
    }
}

class PermissionItem(val title : String, val description : String)

class PermissionViewHolder(val binding: PermissionItemRecyclerviewBinding) :
    RecyclerView.ViewHolder(binding.root)


class PermissionScreenAdapter( val datas:MutableList<PermissionItem>, val activity: Activity, val permissionBinding: FragmentPermissionCheckBinding) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    private var recyclerView: RecyclerView? = permissionBinding.permissionList // RecyclerView 변수
    override fun getItemCount(): Int {
        return datas.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            RecyclerView.ViewHolder =
        PermissionViewHolder(PermissionItemRecyclerviewBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    fun setCompleteExp(btn:AppCompatButton){
        btn.text="완료"
        btn.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.purple))
    }

    fun setBtnEnable(){
        val btn = permissionBinding.nextButton
        btn.backgroundTintList=ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.colorPrimary))
    }
    fun setBtnDisEnable(){
        val btn = permissionBinding.nextButton
        btn.backgroundTintList=ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.light_gray))
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding=(holder as PermissionViewHolder).binding

        fun chkPermission( title: String):Boolean{
            var result = false
            when(title){
                "사용 정보 접근" -> {
                    result = Permission(activity).checkUsageStatsPermission()
                }

                "다른 앱 위에 그리기" -> {
                    result = Permission(activity).checkOverlayPermission()
                }
                "전화 걸기 및 관리" -> {
                    result = Permission(activity).checkCallPermission()
                }
                "알림" -> {
                    result = Permission(activity).checkNotificationPermission()
                }
            }
            return result
        }
        fun activePermission(title: String, btn:AppCompatButton) {

                when (title) {
                    "사용 정보 접근" -> {
                        Permission(activity).requestUsageStatsPermission()
                    }
                    "다른 앱 위에 그리기" -> {
                        Permission(activity).requestOverlayPermission()
                    }
                    "전화 걸기 및 관리" -> {
                        Permission(activity).requestCallPermission()
                    }
                    "알림" -> {
                        Permission(activity).requestNotificationPermission()
                    }
                }
        }

        binding.permissionTitle.text = datas[position].title
        binding.permissionDescription.text = datas[position].description

        when(chkPermission(datas[position].title)){
            true -> {setCompleteExp(binding.checkButton)}
            false ->{}
        }
        binding.checkButton.setOnClickListener{
            when(chkPermission(datas[position].title)){
                true -> {}
                false -> {
                    activePermission(datas[position].title, binding.checkButton)
                }
            }
        }
    }

    fun handlePermissionResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {

        var tmp = getViewHolderByItemID(requestCode)

        var temp = (tmp as PermissionViewHolder).binding
        var btn = temp.checkButton

        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                setCompleteExp(btn)

            // 권한이 허용된 경우의 처리 로직
            // 예: 카메라 권한이 허용되었을 때, 카메라 기능을 사용할 수 있도록 설정
        } else {
            // 권한이 거부된 경우의 처리 로직
            // 예: 사용자에게 권한이 필요하다는 메시지를 표시하거나 다른 대안을 제공

        }
        if(Permission(activity).checkAllPermission()){
            setBtnEnable()
        }
        else{
            setBtnDisEnable()
        }
        // 권한 처리 로직
    }

    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        var tmp = getViewHolderByItemID(requestCode)
        var temp = (tmp as PermissionViewHolder).binding
        var btn = temp.checkButton
        when(requestCode){
            0 -> {
                if(Permission(activity).checkUsageStatsPermission()){
                    setCompleteExp(btn)
                }
            }
            1 -> {
                if(Permission(activity).checkOverlayPermission()){
                    setCompleteExp(btn)
                }
            }
        }
        if(Permission(activity).checkAllPermission()){
            setBtnEnable()
        }
        else{
            setBtnDisEnable()
        }
    }

    fun getViewHolderByItemID(position: Int): ViewHolder? {
            return recyclerView!!.findViewHolderForAdapterPosition(position)
        // 아이템을 찾지 못한 경우
    }
}

