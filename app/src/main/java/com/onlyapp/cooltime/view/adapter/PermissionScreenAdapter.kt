package com.onlyapp.cooltime.view.adapter

import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.onlyapp.cooltime.R
import com.onlyapp.cooltime.databinding.PermissionItemBinding
import com.onlyapp.cooltime.utils.Permission

class PermissionItem(val title : String, val description : String) // 리스트 안에 필요한 데이터들을 담을 클래스

class PermissionViewHolder(val binding: PermissionItemBinding) :
//각 리스트의 뷰와 데이터를 보유하는 객체
//디자인한 리스트를 받아서 binding
    ViewHolder(binding.root)

class PermissionScreenAdapter(
    private val permissionItems:MutableList<PermissionItem>,
    private val mCompletePermissionButton : (btn: AppCompatButton) -> Unit, // 이름 : (건네줄 인자들) -> 함수의 리턴값 꼴로 작성
    private val mChkPermissionByCode : (requestCode: Int) -> Boolean,
    private val mChkPermissionByTitle : (title: String) -> Boolean,
    private val mRequestPermission : (title: String) -> Unit,
    private val mSetNextButton : () -> Unit,
    private val mGetViewHolder : (requestCode:Int) -> ViewHolder?,
    ) :
    RecyclerView.Adapter<ViewHolder>(){

    override fun getItemCount(): Int {
        return permissionItems.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            ViewHolder =
        PermissionViewHolder(PermissionItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    // 각각의 리스트들의 데이터와  뷰홀더를 결합
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val binding=(holder as PermissionViewHolder).binding

        // 권한들과, 권한에 대한 설명을 각 뷰홀더에 binding
        binding.permissionTitle.text = permissionItems[position].title
        binding.permissionDescription.text = permissionItems[position].description

        //이미 설정되어 있으면 완료처리
        when(mChkPermissionByTitle.invoke(permissionItems[position].title)){
            true -> {
                mCompletePermissionButton.invoke(binding.checkButton)
            }
            false ->{}
        }

        //설정되어 있지 않다면 눌렀을 때 해당 권한 설정 실행
        binding.checkButton.setOnClickListener{
            when(mChkPermissionByTitle.invoke(permissionItems[position].title)){
                true -> {}
                false -> {
                    mRequestPermission.invoke(permissionItems[position].title)
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
            mCompletePermissionButton.invoke(btn)
            // 권한이 허용된 경우의 처리 로직
        }
        // 이후에 권한이 다 체크됐는지 확인해서 체크됐으면 버튼 활성화, 아니면 비활성화
        mSetNextButton.invoke()
        // 권한 처리 로직
    }

    fun handleActivityResult(requestCode: Int) {
        val viewHolder = getViewHolderByRequestCode(requestCode)
        val binding = (viewHolder as PermissionViewHolder).binding
        val btn = binding.checkButton

        // 코드 값에 따라 무슨 권한이었는지 알 수 있으므로 해당 권한이 설정되었는지 확인
        // 설정됐으면 해당 버튼을 완료처리
        if(mChkPermissionByCode.invoke(requestCode)){
            mCompletePermissionButton.invoke(btn)
        }

        // 이후에 권한이 다 체크됐는지 확인해서 체크됐으면 버튼 활성화, 아니면 비활성화
        mSetNextButton.invoke()
    }

    // RequestCode로 해당 인덱스의 ViewHolder 반환
    private fun getViewHolderByRequestCode(requestCode: Int): ViewHolder? {
        return mGetViewHolder.invoke(requestCode)
    }
}