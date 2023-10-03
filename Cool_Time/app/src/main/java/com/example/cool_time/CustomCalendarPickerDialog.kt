package com.example.cool_time

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.window.layout.WindowMetricsCalculator
import com.example.cool_time.databinding.CustomCalendarPickerDialogBinding
import com.example.cool_time.databinding.CustomTimePickerDialogBinding
import java.text.SimpleDateFormat
import java.util.Date

class CustomCalendarPickerDialog(confirmDialogInterface : CustomCalendarPickerDialog.OnDateChangeListener) :DialogFragment(){
    private lateinit var binding: CustomCalendarPickerDialogBinding
    private  var confirmDialogInterface: CustomCalendarPickerDialog.OnDateChangeListener? = null
    private var date : String = ""
    init{this.confirmDialogInterface= confirmDialogInterface}
    var dateFormat = SimpleDateFormat("yy년 MM월 dd일")
    lateinit var initDate: Date;
    override fun onResume() {
        super.onResume()
        val windowMetrics = WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(activity!!)
        val currentBounds = windowMetrics.bounds // E.g. [0 0 1350 1800]
        val width = (currentBounds.width() * 0.7).toInt()
        val height = (currentBounds.height() * 0.7).toInt()
        val params = dialog!!.window!!.attributes

        params.width = width
        //params.height = height
        params.horizontalMargin = 0.0f

        dialog!!.window!!.attributes = params
    }
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View {
        binding = CustomCalendarPickerDialogBinding.inflate(inflater, container, false)


        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        initDate = Date(binding.phoneLockCalendarPicker.date)
        date = dateFormat.format(initDate)

        // 취소 버튼 클릭

        binding.phoneLockCalendarPicker.setOnDateChangeListener {
            calendarView, year, month, dayofMonth ->
            date = "${year%100}년 ${month+1}월 ${dayofMonth}일"
        }
        binding.cancleButton.setOnClickListener {
            dismiss()
        }
        // 확인 버튼 클릭
        binding.confirmButton.setOnClickListener {
            this.confirmDialogInterface!!.onYesButtonClick(date)
            dismiss()
        }
        //binding.calendarPicker.date
        return binding.root
    }

    interface OnDateChangeListener {
        fun onYesButtonClick(date:String)
    }
}
//폰락 캘린더 날짜 고르는 것 까지 하던 중