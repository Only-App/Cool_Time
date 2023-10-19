package com.example.cool_time.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cool_time.databinding.FragmentPermissionCheckBinding
import com.example.cool_time.viewmodel.LockScreenAdapter
import com.example.cool_time.viewmodel.PermissionItem
import com.example.cool_time.viewmodel.PermissionScreenAdapter

class CheckPermissionFragment :AppCompatActivity(){
    override fun onCreate(
        //inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    )/*: View*/ {
        super.onCreate(savedInstanceState)
        val use_info = PermissionItem("사용 정보 접근", "현재 실행 중인 앱을 조회합니다.")
        val drawonapp = PermissionItem("다른 앱 위에 그리기", "현재 실행 중인 앱을 조회합니다.")
        val call = PermissionItem("전화 걸기 및 관리", "전화 통화 중 잠금화면 해제를 위해 사용")
        val noti = PermissionItem("알림", "알림을 표시합니다.")
        val datas = mutableListOf<PermissionItem>()// arrayListOf(use_info, drawonapp, call, noti)
        val binding = FragmentPermissionCheckBinding.inflate(layoutInflater, /*container, false*/)
        for(i in 1.. 4){
            datas.add(use_info)
        }
        val adapter = PermissionScreenAdapter(datas = datas, this, binding)

        binding.permissionList.adapter = adapter
        binding.permissionList.layoutManager  = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false,)

        //return binding.root
    }
}