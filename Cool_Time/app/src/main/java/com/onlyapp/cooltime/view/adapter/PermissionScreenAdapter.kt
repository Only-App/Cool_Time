package com.onlyapp.cooltime.view.adapter

import android.app.Activity
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.onlyapp.cooltime.R
import com.onlyapp.cooltime.databinding.ActivityPermissionCheckBinding
import com.onlyapp.cooltime.databinding.PermissionItemBinding
import com.onlyapp.cooltime.utils.Permission


class LinearDecorationSpace(private val divHeight: Int) : RecyclerView.ItemDecoration() { // 리스트 여백 주는 데코
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        //맨아래 리스트가 아니면 아래에 설정한만큼 공간 설정
        if (parent.getChildAdapterPosition(view) != parent.adapter?.itemCount?.minus(1)) {
            outRect.bottom = divHeight
        }
    }
}

class PermissionItem(val title : String, val description : String) // 리스트 안에 필요한 데이터들을 담을 클래스

class PermissionViewHolder(val binding: PermissionItemBinding) :
    //각 리스트의 뷰와 데이터를 보유하는 객체
    //디자인한 리스트를 받아서 binding
    ViewHolder(binding.root)


class PermissionScreenAdapter(private val permissionItems:MutableList<PermissionItem>, private val activity: Activity, private val permissionBinding: ActivityPermissionCheckBinding) :
    RecyclerView.Adapter<ViewHolder>(){
    private var recyclerView: RecyclerView? = permissionBinding.permissionList // RecyclerView 변수

    override fun getItemCount(): Int {
        return permissionItems.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            ViewHolder =
        PermissionViewHolder(PermissionItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    private fun setCompleteExp(btn:AppCompatButton){
        btn.text="완료"
        btn.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.purple))
    }

    fun setBtnEnable(){
        val btn = permissionBinding.nextButton
        btn.backgroundTintList=ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.colorPrimary))
        btn.isEnabled = true // 버튼 누르면 설정한 로직 동작하도록 설정
    }
    fun setBtnDisEnable(){
        val btn = permissionBinding.nextButton
        btn.backgroundTintList=ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.light_gray))
        btn.isEnabled = false // 버튼 눌러도 동작 안하도록 설정
    }

    // 각각의 리스트들의 데이터와  뷰홀더를 결합
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val binding=(holder as PermissionViewHolder).binding

        // 넘겨받은 title을 바탕으로 특정 권한이 체크되었는지 확인해서 결과 반환
        fun chkPermission( title: String):Boolean{
            var result = false
            when(title){
                "사용 정보 접근" -> {
                    result = Permission.checkUsageStatsPermission(activity)
                }
                "다른 앱 위에 그리기" -> {
                    result = Permission.checkOverlayPermission(activity)
                }
                "전화 걸기 및 관리" -> {
                    result = Permission.checkCallPermission(activity)
                }
                "알림" -> {
                    result = Permission.checkNotificationPermission(activity)
                }
                "배터리" -> {
                    result = Permission.checkBatteryPermission(activity)
                }
            }
            return result
        }

        // 넘겨받은 title을 바탕으로 특정 권한을 설정하도록 실행
        fun activePermission(title: String) {
                when (title) {
                    "사용 정보 접근" -> {
                        Permission.requestUsageStatsPermission(activity)
                    }
                    "다른 앱 위에 그리기" -> {
                        Permission.requestOverlayPermission(activity)
                    }
                    "전화 걸기 및 관리" -> {
                        Permission.requestCallPermission(activity)
                    }
                    "알림" -> {
                        Permission.requestNotificationPermission(activity)
                    }
                    "배터리" -> {
                        Permission.requestIgnoringBatteryOptimization(activity)
                    }
                }
        }

        // 권한들과, 권한에 대한 설명을 각 뷰홀더에 binding
        binding.permissionTitle.text = permissionItems[position].title
        binding.permissionDescription.text = permissionItems[position].description

        //이미 설정되어 있으면 완료처리
        when(chkPermission(permissionItems[position].title)){
            true -> {setCompleteExp(binding.checkButton)}
            false ->{}
        }

        //설정되어 있지 않다면 눌렀을 때 해당 권한 설정 실행
        binding.checkButton.setOnClickListener{
            when(chkPermission(permissionItems[position].title)){
                true -> {}
                false -> {
                    activePermission(permissionItems[position].title)
                }
            }
        }
    }

    fun handlePermissionResult(requestCode: Int, grantResults: IntArray) {
        // requestCode를 바탕으로
        val viewHolder = getViewHolderByRequestCode(requestCode)
        val binding = (viewHolder as PermissionViewHolder).binding

        // 코드 값에 따라 무슨 권한이었는지 알 수 있으므로 해당 권한 리스트의 버튼과 연결
        val btn = binding.checkButton
        // 권한이 설정됐으면 해당 버튼을 완료처리
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setCompleteExp(btn)
            // 권한이 허용된 경우의 처리 로직
        }

        // 이후에 권한이 다 체크됐는지 확인해서 체크됐으면 버튼 활성화, 아니면 비활성화
        if(Permission.checkAllPermission(activity)){
            setBtnEnable()
        }
        else{
            setBtnDisEnable()
        }
        // 권한 처리 로직
    }

    fun handleActivityResult(requestCode: Int) {

        val viewHolder = getViewHolderByRequestCode(requestCode)
        val binding = (viewHolder as PermissionViewHolder).binding
        val btn = binding.checkButton

        // 코드 값에 따라 무슨 권한이었는지 알 수 있으므로 해당 권한이 설정되었는지 확인
        // 설정됐으면 해당 버튼을 완료처리
        when(requestCode){
            0 -> {
                if(Permission.checkUsageStatsPermission(activity)){
                    setCompleteExp(btn)
                }
            }
            1 -> {
                if(Permission.checkOverlayPermission(activity)){
                    setCompleteExp(btn)
                }
            }
            4 -> {
                if(Permission.checkBatteryPermission(activity)){
                    setCompleteExp(btn)
                }
            }

        }

        // 이후에 권한이 다 체크됐는지 확인해서 체크됐으면 버튼 활성화, 아니면 비활성화
        if(Permission.checkAllPermission(activity)){
            setBtnEnable()
        }
        else{
            setBtnDisEnable()
        }
    }

    // RequestCode로 해당 인덱스의 ViewHolder 반환
    private fun getViewHolderByRequestCode(position: Int): ViewHolder? {
            return recyclerView!!.findViewHolderForAdapterPosition(position)
    }
}

