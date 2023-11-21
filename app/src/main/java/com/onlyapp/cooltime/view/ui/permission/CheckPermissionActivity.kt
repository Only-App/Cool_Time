package com.onlyapp.cooltime.view.ui.permission

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
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

        val useInfo = PermissionItem("사용 정보 접근", "현재 실행 중인 앱을 조회합니다.")
        val drawOnApp = PermissionItem("다른 앱 위에 그리기", "현재 실행 중인 앱을 조회합니다.")
        val call = PermissionItem("전화 걸기 및 관리", "전화 통화 중 잠금화면 해제를 위해 사용")
        val notification = PermissionItem("알림", "알림을 표시합니다.")
        val battery = PermissionItem("배터리", "절전모드로 인한 서비스 장애를 방지합니다.")
        val permissionList = arrayListOf(useInfo, drawOnApp, call, notification, battery)

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

    override fun onBackPressed() {
        super.onBackPressed()
        setResult(0) //그냥 뒤로 나가면 0을 반환하도록 함
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