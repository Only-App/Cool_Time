package com.example.cool_time.utils

import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Path
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.cool_time.databinding.FragmentHourChartBinding
import com.github.mikephil.charting.animation.ChartAnimator
import com.github.mikephil.charting.charts.BarChart

import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.renderer.BarChartRenderer
import com.github.mikephil.charting.utils.ViewPortHandler


// 직접 차트 커스텀 하기 위해 BarChartRenderer 상속 받아서 설정
class BarChartRender(aChart: BarChart, aAnimator: ChartAnimator,
                     aViewPortHandler: ViewPortHandler,

                     )
    : BarChartRenderer(aChart, aAnimator, aViewPortHandler) {
    //둥글게 설정할 크기 설정
    private val mRadius = 30F

    //상하좌우 위치 받고, 각 축별로 round 할 크기와 네 모서리의 round 유무를 bool 값으로 받아서 round 처리해줌
    private fun roundedRect(
        left: Float, top: Float, right: Float, bottom: Float, rx: Float, ry: Float,
        tl: Boolean, tr: Boolean, br: Boolean, bl: Boolean
    ): Path {
        // 각 축별 round할 크기 설정
        // rxvalue는 원의 x축 반지름, ryvalue는 원의 y축 반지름이라고 생각하면 됨 (각 값이 다르면 타원 같은 느낌)
        //x축 round 할 크기 설정
        var rxValue = rx
        //y축 round 할 크기 설정
        var ryValue = ry

        // 음수 같은 비정상적인 값이 들어왔을 땐 그냥 rounding 없도록 설정
        if (rxValue < 0) rxValue = 0f
        if (ryValue < 0) ryValue = 0f

        //막대 바의 width와 height를 좌우, 상하 위치 차이로 계산
        val width = right - left
        val height = bottom - top

        // x와 y축에 대한 rounding의 최대 크기를 막대바의 width 혹은 height 둘 중 작은 값의 절반을 넘지 않도록 설정함 (절반보다 크면 제대로 원형으로 rounding이 예쁘게 안될테니까, x,y의 rounding 크기가 달라도 예쁘지 않고)
        if (rxValue > width / 2) {
            rxValue = width / 2; ryValue = rxValue
        }
        if (ryValue > height / 2) {
            ryValue = height / 2; rxValue = ryValue
        }

        //val widthMinusCorners = (width - (2 * rxValue))

        // 곡선을 제외한 x축으로부터 수직인 바의 길이
        val heightMinusCorners = (height - (2 * ryValue))

        // 안드로이드에서 그래픽을 그리고 경로를 정의하는데 사용되는 클래스
        val path = Path()
        /* 참고사항 : path관련 상대좌표를 매개변수로 받는 함수에서
                    y인자를 음수로 설정하면 막대바의 위치가 오히려 더 위로 올라감, 양수를 줘야지 아래로 내려감,
                    x값은 음수면 왼쪽, 플러스면 오른쪽으로 이동함
         */

        // round를 그릴 시작점 설정 -> y가 단순히 top이 아니라 top + ryvalue인 이유는 막대 위에 ryvalue 높이만큼을 올라가면서 그릴거기 때문에 top에서 ryvalue만큼 아래에 위치해야 하기 때문에 ryvalue 만큼을 더해줌
        path.moveTo(right, top + ryValue)

        // 만약 top-right의 radius 설정을 true로 했다면 굴곡 설정
        // rQuadTo는 현재위치로부터 중간 좌표() 목표 좌표를 넘겨받아서 시작 좌표 => 현재 좌표에서 목표 좌표로 가는 직선이 있다 가정했을 때 그 선을 중간 좌표 위치로 활 시위를 잡아 당기듯이 당겼을 때 생기는 꼴의 곡선을 그려주는 함수
        // => 만약 현재 좌표 <=> 중간 좌표, 중간 좌표 <=> 목표 좌표 간의 거리가 동일하다면 반지름이 일정한 반원 같은 꼴의 곡선을 그려줄 것임을 알 수 있음
        // 현재 위치가 (right, top + ryValue) =>(막대 바의 오른쪽 위 굴곡이 시작되는 점)이기 때문에 여기에서 rxValue만큼 왼쪽으로, ryValue만큼 위로 이동하면서 곡선을 그려 둥글게 해준다.
        if (tr) path.rQuadTo(0f, -ryValue, -rxValue, -ryValue) // top-right corner =>
        else {
            // 만약 설정 안했으면 직선으로 그려야 하므로 원래 y값(top)까지 그대로 위로 쭉 직선으로 그린다. ㅣ 꼴로 그리는 것
            // 참고사항 : rLineTo, rQuadTo 등으로 그리고 나면 현재 위치가 바뀐다 => 다음에 다시 rLineTo등 상대 좌표를 이용한 함수를 이용할 때 현재 위치를 계산할 필요가 있음
            //위로 그렸으니까 왼쪽으로 rxValue만큼 직선으로 다시 그어준다. - 꼴로 그리는 것 => 즉 결과적으로 ㄱ자 꼴로 막대 테두리 그려준 셈
            path.rLineTo(0f, -ryValue)
            path.rLineTo(-rxValue, 0f)
        }

        // path.rLineTo(-widthMinusCorners, 0f) //굳이 필요 x
        // 현재위치는 반원 테두리에서 중앙
        // 만약 top-left radius 설정을 true로 했다면 왼쪽 아래로 내려가면서 반원을 그린다
        if (tl) path.rQuadTo(-rxValue, 0f, -rxValue, ryValue) // top-left corner
        else {
            // 설정 안했으면 그냥 왼쪽으로 직선 긋고 아래로 다시 직선 그어서 직각 테두리 완성
            path.rLineTo(-rxValue, 0f)
            path.rLineTo(0f, ryValue)
        }

        /* 위에는 완성, 이제 아래로 내려가는데 이미 위에서 곡선 or 직각 모서리를 그리면서 ryValue 만큼 내려왔고
            밑에서 radius를 줄 수도 있으니까 총 height 크기에서 y축 반지름인 ryValue를 2번 뺀 크기만큼 직선으로 내려오면서 그려준다.
         */
        path.rLineTo(0f, heightMinusCorners)

        //이하 top-right, top-left를 그릴 때와 동일한 방법으로 체크 했으면 동일하게 처리해서 그려준다.
        if (bl) path.rQuadTo(0f, ryValue, rxValue, ryValue) // bottom-left corner
        else {
            path.rLineTo(0f, ryValue)
            path.rLineTo(rxValue, 0f)
        }

        //path.rLineTo(widthMinusCorners, 0f) 굳이 필요 x

        if (br) path.rQuadTo(rxValue, 0f, rxValue, -ryValue) // bottom-right corner
        else {
            path.rLineTo(rxValue, 0f)
            path.rLineTo(0f, -ryValue)
        }

        // 아까와 마찬가지로 이번엔 남은 우측 변의 길이만큼 그려줌
        path.rLineTo(0f, -heightMinusCorners)
        // 다 그렸으니까 종료 처리
        path.close() // Given close, last lineto can be removed.
        return path
    }

    override fun drawDataSet(c: Canvas?, dataSet: IBarDataSet?, index: Int) { // 내부적으로 그래프 그릴 때 호출되는 함수
        val trans = mChart.getTransformer(dataSet!!.axisDependency)
        mShadowPaint.color = dataSet.barShadowColor
        val phaseX = mAnimator.phaseX //애니메이션(막대바 데이터에 맞게 바 길이 늘어나는 애니메이션)의 x축에 대한 진행 정도
        val phaseY = mAnimator.phaseY //애니메이션(막대바 데이터에 맞게 바 길이 늘어나는 애니메이션)의 y축에 대한 진행 정도

        if (mBarBuffers != null) {
            // Initialize the buffer
            val buffer = mBarBuffers[index] // mBarBuffers는 막대 바를 가지는 array로 판단됨
            // 해당 막대의 애니메이션 진행도를 설정
            // ex: 막대바 길이가 100이고 phase가 0.5로 설정되어 있으면 50은 이미 그려진 상태에서 애니메이션 시작
            buffer.setPhases(phaseX, phaseY)
            //barWidth 등
            buffer.setBarWidth(mChart.barData.barWidth)
            buffer.setInverted(mChart.isInverted(dataSet.axisDependency))
            buffer.feed(dataSet) // 주어진 데이터 바탕으로 버퍼 구축 후 막대바에게 데이터 공급이 끝나면 버퍼를 초기화 함
            trans.pointValuesToPixel(buffer.buffer) //buffer 안에 값을 pixel단위로 바꿔주는 듯 함 확실 X

            // If multiple colors
            if (dataSet.colors.size > 1) {
                var j = 0
                //막대가 뷰포트(화면에 표시된 차트 영역)내에서 좌측 경계, 우측 경계 내에 있는지를 확인해서 조건 처리
                while (j < buffer.size()) {
                    if (!mViewPortHandler.isInBoundsLeft(buffer.buffer[j + 2])) {
                        continue
                    }
                    if (!mViewPortHandler.isInBoundsRight(buffer.buffer[j])) {
                        break
                    }

                    //barChart에 그림자 효과 적용했다면
                    if (mChart.isDrawBarShadowEnabled) {
                        if (mRadius > 0) { // 굴곡 값도 설정되어 있다면
                            val path = roundedRect(
                                buffer.buffer[j],
                                buffer.buffer[j + 1],
                                buffer.buffer[j + 2],
                                buffer.buffer[j + 3],
                                mRadius, mRadius,
                                true, true, true, true // 설정할 곳 true로 해서 보내기
                            )
                            c!!.drawPath(path, mShadowPaint)
                        } else {
                            c!!.drawRect( // 그냥 그리기
                                buffer.buffer[j],
                                mViewPortHandler.contentTop(),
                                buffer.buffer[j + 2],
                                mViewPortHandler.contentBottom(),
                                mShadowPaint
                            )
                        }
                    }

                    // Set the color for the currently drawn value. If the index is out of bounds, reuse colors.
                    //데이터
                    mRenderPaint.color = dataSet.getColor(j / 4)
                    if (mRadius > 0) {
                        val path = roundedRect(
                            buffer.buffer[j],
                            buffer.buffer[j + 1],
                            buffer.buffer[j + 2],
                            buffer.buffer[j + 3],
                            mRadius, mRadius,
                            true, true, true, true
                        )
                        c!!.drawPath(path, mRenderPaint)
                    } else {
                        c!!.drawRect(
                            buffer.buffer[j],
                            buffer.buffer[j + 1],
                            buffer.buffer[j + 2],
                            buffer.buffer[j + 3],
                            mRenderPaint
                        )
                    }
                    j += 4
                }
            } else { //컬러가 한개일 때
                mRenderPaint.color = dataSet.color
                var j = 0
                while (j < buffer.size()) {
                    if (!mViewPortHandler.isInBoundsLeft(buffer.buffer[j + 2])) {
                        continue
                    }
                    if (!mViewPortHandler.isInBoundsRight(buffer.buffer[j])) {
                        break
                    }
                    if (mChart.isDrawBarShadowEnabled) {
                        if (mRadius > 0) {
                            val path = roundedRect(
                                buffer.buffer[j],
                                buffer.buffer[j + 1],
                                buffer.buffer[j + 2],
                                buffer.buffer[j + 3],
                                mRadius, mRadius,
                                true, true, true, true
                            )
                            //path 추출해서, 그림
                            c!!.drawPath(path, mRenderPaint)
                        } else {
                            c!!.drawRect(
                                buffer.buffer[j],
                                mViewPortHandler.contentTop(),
                                buffer.buffer[j + 2],
                                mViewPortHandler.contentBottom(),
                                mShadowPaint
                            )
                        }
                    }
                    if (mRadius > 0) { //컬러도 한개고 그림자 설정 안하고 굴곡 설정했을때 -- 우리차트는 이 조건식에 부합해 이부분의 코드 수정
                        val path = roundedRect(
                            buffer.buffer[j],
                            buffer.buffer[j + 1],
                            buffer.buffer[j + 2],
                            buffer.buffer[j + 3],
                            mRadius, mRadius,
                            true, true, false, false
                        )
                        c!!.drawPath(path, mRenderPaint)
                    } else { //설정 안했을 때
                        c!!.drawRect(
                            buffer.buffer[j],
                            buffer.buffer[j + 1],
                            buffer.buffer[j + 2],
                            buffer.buffer[j + 3],
                            mRenderPaint
                        )
                    }
                    j += 4 //=> 한 entry에 상하좌우 4개를 사용하므로 4씩 건너 뛰어야 함
                }
            }
        }
    }
}
class ChartHourFragment : Fragment(){

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

        barChart.run{

            setTouchEnabled(false)}
    }

    // 앱이 스마트폰 내에 설치되어 있는지 확인
    private fun isAppInstalled(packageName : String, packageManager : PackageManager) : Boolean{
        return try{
            packageManager.getPackageInfo(packageName, 0) // 있으면 true
            true
        }catch (ex : PackageManager.NameNotFoundException){ // 없으면 false
            false
        }
    }

    // 차트 데이터 설정
    private fun setData(barChart: BarChart) {

        // Zoom In / Out 가능 여부 설정
        barChart.setScaleEnabled(false)

        val valueList = ArrayList<BarEntry>() // 하나의 막대 바 만드는데 필요한 값들을 넣는 BarEntry들을 담을 리스트 생성
        val title = "차트" // 어차피 안보이게 할거라 필요는 없지만 차트 이름 설정

        // 임의 수동 반복문 데이터
        for (i in 0 until 24) {
            //테스트 위해서 그냥 임의로 큰값 한번 넣어줌
            if(i == 3) {
                valueList.add(BarEntry(i.toFloat(), i * 1000f,))
            }
            else{
                valueList.add(BarEntry(i.toFloat(), i * 100f,))
            }
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