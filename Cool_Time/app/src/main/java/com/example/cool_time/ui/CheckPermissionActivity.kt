package com.example.cool_time.ui

import android.content.Intent
import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cool_time.databinding.ActivityPermissionCheckBinding
import com.example.cool_time.utils.Permission
import com.example.cool_time.viewmodel.LinearDecorationSpace
import com.example.cool_time.viewmodel.PermissionItem
import com.example.cool_time.viewmodel.PermissionScreenAdapter

class CheckPermissionActivity :AppCompatActivity(){
    private lateinit var adapter : PermissionScreenAdapter
    private lateinit var binding : ActivityPermissionCheckBinding
    override fun onCreate(
        savedInstanceState: Bundle?
    )/*: View*/ {
        super.onCreate(savedInstanceState)

        val use_info = PermissionItem("사용 정보 접근", "현재 실행 중인 앱을 조회합니다.")
        val drawonapp = PermissionItem("다른 앱 위에 그리기", "현재 실행 중인 앱을 조회합니다.")
        val call = PermissionItem("전화 걸기 및 관리", "전화 통화 중 잠금화면 해제를 위해 사용")
        val noti = PermissionItem("알림", "알림을 표시합니다.")
        val datas = arrayListOf(use_info, drawonapp, call, noti)
        binding = ActivityPermissionCheckBinding.inflate(layoutInflater, /*container, false*/)

        adapter = PermissionScreenAdapter(datas = datas, this, binding)

        binding.permissionList.adapter = adapter
        binding.permissionList.layoutManager  = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false,)
        binding.permissionList.addItemDecoration(LinearDecorationSpace(10))
        if(Permission(this).checkAllPermission()){
            adapter.setBtnEnable()
        }
        else{
            adapter.setBtnDisEnable()
        }

        binding!!.nextButton.setOnClickListener{
            setResult(1)
            finish()
        }
        setContentView(binding.root)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        setResult(0)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        adapter.handleActivityResult(requestCode, resultCode, data)

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        adapter.handlePermissionResult(requestCode, permissions, grantResults)
    }
}