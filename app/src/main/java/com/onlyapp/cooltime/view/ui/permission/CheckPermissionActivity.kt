package com.onlyapp.cooltime.view.ui.permission

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.window.OnBackInvokedDispatcher
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.onlyapp.cooltime.R
import com.onlyapp.cooltime.databinding.ActivityPermissionCheckBinding
import com.onlyapp.cooltime.utils.Permission
import com.onlyapp.cooltime.view.adapter.PermissionItem
import com.onlyapp.cooltime.view.adapter.PermissionScreenAdapter
import com.onlyapp.cooltime.view.itemdecoration.LinearDecorationSpace

class CheckPermissionActivity :AppCompatActivity(){
    // 권한 목록 리스트를 만들 recycler뷰의 어댑터
    private lateinit var adapter : PermissionScreenAdapter
    private lateinit var binding : ActivityPermissionCheckBinding
    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            onBackInvokedDispatcher.registerOnBackInvokedCallback(
                OnBackInvokedDispatcher.PRIORITY_DEFAULT
            ) {
                setResult(0) //그냥 뒤로 나가면 0을 반환하도록 함
            }
        } else {
            onBackPressedDispatcher.addCallback(this /* lifecycle owner */) {
                setResult(0) //그냥 뒤로 나가면 0을 반환하도록 함
            }
        }
        val useInfo = PermissionItem(getString(R.string.permission_use_stat_title), getString(R.string.permission_use_stat_detail))
        val drawOnApp = PermissionItem(getString(R.string.permission_draw_on_app_title), getString(R.string.permission_draw_on_app_detail))
        val battery = PermissionItem(getString(R.string.permission_battery_title), getString(R.string.permission_battery_detail))
        val permissionList = arrayListOf(useInfo, drawOnApp, battery)

        binding = ActivityPermissionCheckBinding.inflate(layoutInflater)
        adapter = PermissionScreenAdapter(permissionList, this, binding)

        binding.permissionList.adapter = adapter
        binding.permissionList.layoutManager  = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        //리스트 사이 여백 설정
        binding.permissionList.addItemDecoration(LinearDecorationSpace(10))

        // 만약 모든 권한이 설정됐으면 다음으로 넘어가는 버튼 활성화, 아니면 비활성화
        if(Permission.checkAllPermission(this)){
            adapter.setBtnEnable()
        }
        else{
            adapter.setBtnDisEnable()
        }

        // 다음으로 가는 버튼 클릭했을 때 1을 반환하고 현재 액티비티 종료하도록 함.
        // 버튼이 비활성화 되어 있으면 눌러도 작동하지 않음
        binding.nextButton.setOnClickListener{
            setResult(1)
            finish()
        }
        setContentView(binding.root)
    }

    //★ 다른 앱 위에 그리기, 사용정보 알아내는 권한의 응답이 오면 안드로이드 시스템 내부에서 자동적으로 실행되는 함수
    // 둘 다 응답이 오면 어댑터 내에서 처리할 수 있도록 설정
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        adapter.handleActivityResult(requestCode)

    }
    //★ 전화, 알람 권한의 응답이 오면 안드로이드 시스템 내부에서 자동적으로 실행되는 함수
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        adapter.handlePermissionResult(requestCode, grantResults)
    }
}