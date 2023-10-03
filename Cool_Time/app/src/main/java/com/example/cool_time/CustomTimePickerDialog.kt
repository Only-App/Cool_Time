package com.example.cool_time

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import androidx.fragment.app.DialogFragment
import androidx.window.layout.WindowMetricsCalculator
import com.example.cool_time.databinding.CustomTimePickerDialogBinding


class CustomTimePickerDialog(confirmDialogInterface : ConfirmDialogInterface) :DialogFragment(){
    private lateinit var binding: CustomTimePickerDialogBinding
    private  var confirmDialogInterface: ConfirmDialogInterface? = null
    private lateinit var hourPick : NumberPicker // 시간 입력하는 Numberpicker 관리하는 변수
    private lateinit var minPick : NumberPicker // 분 입력하는 Numberpicker 관리하는 변수
    init{this.confirmDialogInterface= confirmDialogInterface}

    override fun onResume() {
        super.onResume()
        val windowMetrics = WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(activity!!)
        val currentBounds = windowMetrics.bounds // E.g. [0 0 1350 1800]
        val width = (currentBounds.width()).toInt()
        val params = dialog!!.window!!.attributes
        params.width = width
        params.horizontalMargin = 0.0f

        dialog!!.window!!.attributes = params
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = CustomTimePickerDialogBinding.inflate(inflater, container, false)
        timeInit()

        val view = binding.root

        // 레이아웃 배경을 투명하게 해줌, 필수 아님
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // 취소 버튼 클릭
        binding.cancelButton.setOnClickListener {
            dismiss()
        }
        // 확인 버튼 클릭
        binding.confirmButton.setOnClickListener {
            this.confirmDialogInterface!!.onYesButtonClick(hourPick.value, minPick.value)
            dismiss()
        }
        return view
    }
    fun timeInit(){ // Time Picker 위한 초기 설정
        hourPick = binding.dialogPicker.hourPicker
        minPick = binding.dialogPicker.minPicker
        hourPick.wrapSelectorWheel = false; // 숫자 값을 키보드로 입력하는 것을 막음
        hourPick.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS // 최대값에서 최소값으로 순환하는 것을 막음

        minPick.wrapSelectorWheel = false;
        minPick.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS

        hourPick.minValue = 0 //0시 00분 ~ 23시 59분까지 설정가능하게
        hourPick.maxValue = 23

        minPick.minValue = 0 //0시 00분 ~ 23시 59분까지 설정가능하게
        minPick.maxValue = 59
    }
    interface ConfirmDialogInterface {
        fun onYesButtonClick( h: Int, m: Int)
    }
}