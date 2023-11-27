package com.onlyapp.cooltime.view.ui.chart

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.onlyapp.cooltime.databinding.FragmentHourChartBinding
import com.onlyapp.cooltime.view.chartrenderer.BarChartRender

class ChartHourFragment(val list: List<Long> = ArrayList()) : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = FragmentHourChartBinding.inflate(inflater, container, false)
        initBarChart(binding.chart)
        setData(binding.chart)
        return binding.root
    }

    // 바 차트 설정
    private fun initBarChart(barChart: BarChart) {
        // 차트 회색 배경 설정 (default = false)
        barChart.setDrawGridBackground(false)
        // 막대 그림자 설정 (default = false)
        barChart.setDrawBarShadow(false)
        // 차트 테두리 설정 (default = false)
        barChart.setDrawBorders(false)

        val description = Description()
        // 오른쪽 하단 모서리 설명 레이블 텍스트 표시 (default = false)
        description.isEnabled = false
        barChart.description = description

        // X, Y 바의 애니메이션 효과
        barChart.animateY(0)
        barChart.animateX(0)


        // 바텀 좌표 값
        val xAxis: XAxis = barChart.xAxis
        // x축 위치 설정
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        // 그리드 선 수평 거리 설정
        xAxis.granularity = 1f
        // x축 텍스트 컬러 설정
        xAxis.textColor = Color.BLACK
        xAxis.setDrawLabels(true)
        xAxis.labelCount = 24
        //xAxis.draw
        // x축 선 설정 (default = true)
        xAxis.setDrawAxisLine(false)
        // 격자선 설정 (default = true)
        xAxis.setDrawGridLines(false)

        val leftAxis: YAxis = barChart.axisLeft
        // 좌측 선 설정 (default = true)
        leftAxis.setDrawAxisLine(false)
        leftAxis.setDrawGridLines(true)
        // 좌측 텍스트 컬러 설정
        leftAxis.textColor = Color.BLACK

        leftAxis.isEnabled = true

        val rightAxis: YAxis = barChart.axisRight
        // 우측 선 설정 (default = true)
        rightAxis.setDrawAxisLine(false)
        // 우측 텍스트 컬러 설정
        rightAxis.textColor = Color.GREEN

        rightAxis.isEnabled = false


        // 바차트의 타이틀
        val legend: Legend = barChart.legend
        // 범례 모양 설정 (default = 정사각형)
        legend.form = Legend.LegendForm.LINE
        // 타이틀 텍스트 사이즈 설정
        legend.textSize = 20f
        // 타이틀 텍스트 컬러 설정
        legend.textColor = Color.BLACK
        // 범례 위치 설정
        legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        // 범례 방향 설정
        legend.orientation = Legend.LegendOrientation.HORIZONTAL
        // 차트 내부 범례 위치하게 함 (default = false)
        legend.setDrawInside(false)
        //범례 안보이게 막음
        legend.isEnabled = false

        barChart.run {

            setTouchEnabled(false)
        }
    }

    // 차트 데이터 설정
    private fun setData(barChart: BarChart) {

        // Zoom In / Out 가능 여부 설정
        barChart.setScaleEnabled(false)

        val valueList = ArrayList<BarEntry>() // 하나의 막대 바 만드는데 필요한 값들을 넣는 BarEntry들을 담을 리스트 생성
        val title = "차트" // 어차피 안보이게 할거라 필요는 없지만 차트 이름 설정

        // 임의 수동 반복문 데이터
        for (i in list.indices) {

            valueList.add(BarEntry(i.toFloat(), list[i] / 60.toFloat()))
        }
        // 막대바들의 정보를 담은 BarEntry List와 차트 이름을 넣어서 하나의 차트를 만드는데 필요한 DataSet 생성
        val barDataSet = BarDataSet(valueList, title)

        // 막대바 위에 값을 출력해줌
        barDataSet.setDrawValues(false)

        // 값 출력해줄 때 텍스트의 크기 설정하는건데 false로 설정했으니까 애초에 필요 X
        //data.setValueTextSize(Utils.convertDpToPixel(5F))

        barDataSet.setColors(
            // 막대바 색깔 설정 => 여러개 넣으면 순서대로 나뉘어서 설정
            Color.rgb(238, 71, 77)
        )

        // DataSet들을 하나의 BarData로 초기화
        // 만약 여러 DataSet들을 넘겨준다면 여러 차트가 하나의 X축과 Y축을 바탕으로 하는 화면에 같이 그려짐
        val data = BarData(barDataSet)

        // 상대적인 bar 두께 설정함 ==> ★ 리사이클러 뷰마냥 유동적으로 차트 크기 설정하는게 아닌, 상위 레이아웃의 크기에 맞춰서 차트가 구현됨
        // => 막대 두께도 상위 레이아웃 크기에 따라 달라짐
        data.barWidth = 0.3f

        //차트의 데이터 바인딩, 세팅된 데이터 값을 차트에 넣어줌
        barChart.data = data

        // 차트 데이터 그릴 때 설정할 애니메이션 효과 정보 담겨있는 변수
        val animator = barChart.animator

        // 차트의 화면 영역, 확대 축소 등을 담당하는데 width와 height 조절이 불가능 set 함수가 없음....
        val viewPortHandler = barChart.viewPortHandler

        //rendering 어떻게 할 건지 설정 => 커스텀 하려면 직접 그려야 해서 필요
        barChart.renderer = BarChartRender(barChart, animator, viewPortHandler)
        barChart.invalidate() // 차트를 새로 그리는 메소드
    }
}