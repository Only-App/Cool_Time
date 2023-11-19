package com.onlyapp.cooltime.view.ui.chart
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import com.onlyapp.cooltime.databinding.FragmentAppChartBinding
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.utils.Utils
import com.onlyapp.cooltime.view.chartrenderer.HorizontalBarChartIconRenderer

class ChartAppFragment(private var appList : List<Pair<String, Long>>  = ArrayList()) : Fragment(){

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = FragmentAppChartBinding.inflate(inflater, container, false)
        initBarChart(binding.chart)
        setData(binding.chart)
        return binding.root
    }

    // 바 차트 설정
    private fun initBarChart(barChart: HorizontalBarChart) {
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
        //barChart.layoutParams.width = 800
        // x축 위치 설정

        xAxis.position = XAxis.XAxisPosition.BOTTOM
        // 그리드 선 수평 거리 설정
        xAxis.granularity = 1f
        // x축 텍스트 컬러 설정
        //xAxis.textColor = Color.RED

        xAxis.setDrawLabels(false)
        //xAxis.draw
        // x축 선 설정 (default = true)
        xAxis.setDrawAxisLine(false)
        // 격자선 설정 (default = true)
        xAxis.setDrawGridLines(false)
        //xAxis.axisMinimum = Utils.convertDpToPixel(2F)

        val leftAxis: YAxis = barChart.axisLeft
        // 좌측 선 설정 (default = true)
        leftAxis.setDrawAxisLine(false)
        // 좌측 텍스트 컬러 설정
        leftAxis.textColor = Color.BLUE

        leftAxis.isEnabled = false

        val rightAxis: YAxis = barChart.axisRight
        // 우측 선 설정 (default = true)
        rightAxis.setDrawAxisLine(false)
        // 우측 텍스트 컬러 설정
        rightAxis.textColor = Color.GREEN

        rightAxis.isEnabled = false


        // 바차트의 타이틀
        val legend: Legend = barChart.legend
        //범례 안보이게 막음
        legend.isEnabled = false

    }

    // 차트 데이터 설정
    private fun setData(barChart: HorizontalBarChart) {
        // Zoom In / Out 가능 여부 설정
        barChart.setScaleEnabled(false)

        val valueList = ArrayList<BarEntry>()
        val title = "앱 사용 시간"
        val map = ArrayList<Bitmap>()
        val packageManager = this.activity?.packageManager
        // 임의 데이터
        var cnt = 0
        packageManager?.let {
            for (i in appList.indices) {
                cnt++
                val packageName = appList[i].first

                val appIcon = it.getApplicationIcon(packageName)
                map.add(appIcon.toBitmap())
                valueList.add(BarEntry(i.toFloat(), (appList[i].second).toFloat()))
            }
        }
        // barChart.~height가 9999까진 괜찮다가 10000부터 이상해짐
        // => 리스트 1개만 있어도 안짤리는 크기가 55dp => 실험 결과 최대 69개까지만 보여주는 쪽으로 해야 함
        barChart.layoutParams.height = (Utils.convertDpToPixel(55F)*cnt).toInt()
        val barDataSet = BarDataSet(valueList, title)
        // 바 색상 설정 (ColorTemplate.LIBERTY_COLORS)
        barDataSet.setColors(
            // 막대바 색깔 설정 => 여러개 넣으면 순서대로 나뉘어서 설정
            Color.rgb(238, 71, 77)
        )

        //barChart.setBackgroundColor(Color.RED)

        // DataSet들을 하나의 BarData로 초기화
        // 만약 여러 DataSet들을 넘겨준다면 여러 차트가 하나의 X축과 Y축을 바탕으로 하는 화면에 같이 그려짐

        val data = BarData(barDataSet)

        data.barWidth = 0.05f //상대적인 bar 크기

        data.setValueTextSize(Utils.convertDpToPixel(5F))

        //차트의 데이터 바인딩, 세팅된 데이터 값을 차트에 넣어줌
        barChart.data = data
        val chartAnimator = barChart.animator
        val viewPortHandler = barChart.viewPortHandler

        //rendering 어떻게 할 건지 설정 => 커스텀 하려면 직접 그려야 해서 필요
        barChart.renderer = HorizontalBarChartIconRenderer(barChart, chartAnimator, viewPortHandler, appList, map, context!!)
        // 차트를 새로 그리는 메소드
        barChart.setTouchEnabled(false)
        barChart.invalidate()
    }
}