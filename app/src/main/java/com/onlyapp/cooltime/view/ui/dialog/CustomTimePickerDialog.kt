package com.onlyapp.cooltime.view.ui.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import androidx.fragment.app.DialogFragment
import androidx.window.layout.WindowMetricsCalculator
import com.onlyapp.cooltime.databinding.CustomTimePickerDialogBinding


class CustomTimePickerDialog(confirmDialogInterface : ConfirmDialogInterface) :DialogFragment(){
    private lateinit var binding: CustomTimePickerDialogBinding
    private  var confirmDialogInterface: ConfirmDialogInterface // 긍정 버튼 눌렀을 떄 처리할 함수를 작동시키기 위한 인터페이스
    private lateinit var hourPick : NumberPicker // 시간 입력하는 Numberpicker 관리하는 변수
    private lateinit var minPick : NumberPicker // 분 입력하는 Numberpicker 관리하는 변수
    init{this.confirmDialogInterface= confirmDialogInterface}

    override fun onResume() {
        super.onResume()
        //androidx.window:window:1.0.0 를 이용해 custom dialog 사용시 내용물의 크기를 임의로 수정함(match parent쓰거나 해도 이상하게 레이아웃 구성 됐었음)
        val windowMetrics = WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(activity!!)
        val currentBounds = windowMetrics.bounds // 네이게이션과 디스플레이 컷아웃 영역을 제외한 현재 띄워지고 있던 창이 차지하고 있던 영역의 크기를 받아옴(보통 화면 전체)
        val width = (currentBounds.width() * 0.7).toInt() // 가로의 70프로를 채우도록 설정
        val params = dialog!!.window!!.attributes // dialog 창 속성 받아옴
        params.width = width //width를 원하던 값으로 변경해줌
        params.horizontalMargin = 0.0f
        dialog!!.window!!.attributes = params //다시 설정한 값으로 대입
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = CustomTimePickerDialogBinding.inflate(inflater, container, false) //바인딩
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) //Radius등 내가 원하는대로 배경 설정하기 위해서 설정
        timeInit()
        // 확인 버튼 클릭
        binding.confirmButton.setOnClickListener {
            this.confirmDialogInterface.onYesButtonClick(hourPick.value, minPick.value) // 확인버튼을 눌렀을 때 코드를 담아둔 함수 실행
            dismiss()
        }
        // 취소 버튼 클릭
        binding.cancelButton.setOnClickListener {
            dismiss()
        }
        return binding.root
    }
    private fun timeInit(){ // Time Picker 위한 초기 설정
        hourPick = binding.dialogPicker.hourPicker
        minPick = binding.dialogPicker.minPicker
        hourPick.wrapSelectorWheel = false // 숫자 값을 키보드로 입력하는 것을 막음
        hourPick.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS // 최대값에서 최소값으로 순환하는 것을 막음

        minPick.wrapSelectorWheel = false
        minPick.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS

        hourPick.minValue = 0 //0시 00분 ~ 23시 59분까지 설정가능하게
        hourPick.maxValue = 23

        minPick.minValue = 0 //0시 00분 ~ 23시 59분까지 설정가능하게
        minPick.maxValue = 59
    }
    interface ConfirmDialogInterface {
        fun onYesButtonClick( hour: Int, min: Int)
    }
}