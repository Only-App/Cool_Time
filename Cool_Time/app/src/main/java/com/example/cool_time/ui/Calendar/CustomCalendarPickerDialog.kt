package com.example.cool_time.ui.Calendar

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.window.layout.WindowMetricsCalculator
import com.example.cool_time.databinding.CustomCalendarPickerDialogBinding
import java.text.SimpleDateFormat
import java.util.Date

class CustomCalendarPickerDialog(confirmDialogInterface : OnDateChangeListener) :DialogFragment(){ //다이얼로그 출력 위한 DialogFragment
    private lateinit var binding: CustomCalendarPickerDialogBinding //binding 변수
    private  var confirmDialogInterface: OnDateChangeListener // 긍정 버튼 눌렀을 떄 처리할 함수를 작동시키기 위한 인터페이스
    private var date : String = "" //날짜를 담을 변수, String은 lateinit이 안되기에 우선 빈문자열로 초기화
    init{this.confirmDialogInterface= confirmDialogInterface} // 인터페이스를 위에 클래스에서 받은 인자로 초기화
    private var dateFormat = SimpleDateFormat("yyyy.MM.dd", java.util.Locale.getDefault()) //"yy년 MM월 dd일" 꼴로 날짜를 표기하게 하고 날짜 표시 방법을 현재 휴대폰 기기의 디폴트 언어로 설정
    private lateinit var initDate: Date
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

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View {
        binding = CustomCalendarPickerDialogBinding.inflate(inflater, container, false) //바인딩
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) //Radius등 내가 원하는대로 배경 설정하기 위해서 설정
        initDate = Date(binding.phoneLockCalendarPicker.date) //현재 날짜로 값 할당
        date = dateFormat.format(initDate) // 설정해둔 표현식에 맞춰서 date로 할당
        // 확인 버튼 클릭
        binding.phoneLockCalendarPicker.setOnDateChangeListener { //날짜가 변했을 때
            _, year, month, dayofMonth ->
            date = "${year}.${month+1}.${dayofMonth}" //년은 끝자리 2개만 출력할꺼니가 100으로 나눈 나머지 값으로 전달
            //Month는 0부터 시작해서 +1을 추가해야 원래 Month가 나옴
        }
        // 확인 버튼 클릭
        binding.confirmButton.setOnClickListener {
            this.confirmDialogInterface.onYesButtonClick(date) // 확인버튼을 눌렀을 때 코드를 담아둔 함수 실행
            dismiss()
        }
        // 취소 버튼 클릭
        binding.cancleButton.setOnClickListener {
            dismiss() // 취소 버튼을 누르면 볼일 끝났으니까 그냥 해제 시킴
        }
        return binding.root
    }

    interface OnDateChangeListener {
        fun onYesButtonClick(date:String)
    }
}