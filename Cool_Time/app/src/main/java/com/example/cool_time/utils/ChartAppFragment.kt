package com.example.cool_time.utils

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Path
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import com.example.cool_time.R
import com.example.cool_time.databinding.FragmentAppChartBinding
import com.github.mikephil.charting.animation.ChartAnimator
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.renderer.HorizontalBarChartRenderer
import com.github.mikephil.charting.utils.Utils
import com.github.mikephil.charting.utils.ViewPortHandler

class HorizontalBarChartIconRenderer(aChart: HorizontalBarChart, aAnimator: ChartAnimator,
                                     aViewPortHandler: ViewPortHandler, aImageList: ArrayList<Bitmap>,
                                     aContext: Context
)
    : HorizontalBarChartRenderer(aChart, aAnimator, aViewPortHandler) {

    private val mContext = aContext
    private val mImageList = aImageList
    var mRadius=100F;

    fun setmRadius(mRadius: Float) {
        this.mRadius = mRadius;
    }
    fun RoundedRect(
        left: Float, top: Float, right: Float, bottom: Float, rx: Float, ry: Float,
        tl: Boolean, tr: Boolean, br: Boolean, bl: Boolean
    ): Path {
        var rxValue = rx
        var ryValue = ry

        if (rxValue < 0) rxValue = 0f
        if (ryValue < 0) ryValue = 0f

        val width = right - left
        val height = bottom - top

        if (rxValue > width / 2) {rxValue = width / 2; ryValue = rxValue}
        if (ryValue > height / 2) {ryValue = height / 2; rxValue = ryValue}

        val widthMinusCorners = (width - (2 * rxValue))
        val heightMinusCorners = (height - (2 * ryValue))
        var path = Path()
        path.moveTo(right, top + ryValue)

        if (tr) path.rQuadTo(0f, -ryValue, -rxValue, -ryValue) // top-right corner
        else {

            path.rLineTo(0f, -ryValue)
            path.rLineTo(-rxValue, 0f)
        }

        path.rLineTo(-widthMinusCorners, 0f)

        if (tl) path.rQuadTo(-rxValue, 0f, -rxValue, ryValue) // top-left corner
        else {
            path.rLineTo(-rxValue, 0f)
            path.rLineTo(0f, ryValue)
        }

        path.rLineTo(0f, heightMinusCorners)

        if (bl) path.rQuadTo(0f, ryValue, rxValue, ryValue) // bottom-left corner
        else {
            path.rLineTo(0f, ryValue)
            path.rLineTo(rxValue, 0f)
        }

        path.rLineTo(widthMinusCorners, 0f)

        if (br) path.rQuadTo(rxValue, 0f, rxValue, -ryValue) // bottom-right corner
        else {
            path.rLineTo(rxValue, 0f)
            path.rLineTo(0f, -ryValue)
        }

        path.rLineTo(0f, -heightMinusCorners)
        path.close() // Given close, last lineto can be removed.

        return path
    }
    override fun drawDataSet(c: Canvas?, dataSet: IBarDataSet?, index: Int) {


        val trans = mChart.getTransformer(dataSet!!.axisDependency)
        mShadowPaint.color = dataSet.barShadowColor
        val phaseX = mAnimator.phaseX
        val phaseY = mAnimator.phaseY

        if (mBarBuffers != null) {
            // Initialize the buffer
            val buffer = mBarBuffers[index]
            buffer.setPhases(phaseX, phaseY)
            //buffer. = index
            buffer.setBarWidth(mChart.barData.barWidth)
            buffer.setInverted(mChart.isInverted(dataSet.axisDependency))
            buffer.feed(dataSet)
            trans.pointValuesToPixel(buffer.buffer)

            // If multiple colors
            if (dataSet.colors.size > 1) {
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
                            val path = RoundedRect(
                                buffer.buffer[j],
                                buffer.buffer[j + 1],
                                buffer.buffer[j + 2],
                                buffer.buffer[j + 3],
                                mRadius, mRadius,
                                true, true, true, true
                            )
                            c!!.drawPath(path, mShadowPaint)
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

                    // Set the color for the currently drawn value. If the index is out of bounds, reuse colors.
                    mRenderPaint.color = dataSet.getColor(j / 4)
                    if (mRadius > 0) {
                        val path = RoundedRect(
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
            } else {
                mRenderPaint.color = dataSet.color
                var j = 0
                while (j < buffer.size()) {
                    /*
                    if (!mViewPortHandler.isInBoundsLeft(buffer.buffer[j + 2])) {
                        continue
                    }

                    if (!mViewPortHandler.isInBoundsRight(buffer.buffer[j])) {
                        break
                    }

                     */
                    if (mChart.isDrawBarShadowEnabled) {
                        if (mRadius > 0) {
                            val path = RoundedRect(
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
                                mViewPortHandler.contentTop(),
                                buffer.buffer[j + 2],
                                mViewPortHandler.contentBottom(),
                                mShadowPaint
                            )
                        }
                    }
                    if (mRadius > 0) {
                        val path = RoundedRect(
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
            }
        }
    }
    override fun drawValues(aCanvas: Canvas) {


        if (!isDrawingValuesAllowed(mChart)) {
            return
        }
        val datasets = mChart.barData.dataSets
        val valueOffsetPlus = Utils.convertDpToPixel(30f)
        val valueTextHeight = Utils.calcTextHeight(mValuePaint, "8")

        //Utils.drawImage()

        for ((index, set) in datasets.withIndex()) {
            if (!shouldDrawValues(set)) {
                continue
            }

            applyValueTextStyle(set)

            var posOffset = if (mChart.isDrawValueAboveBarEnabled) {
                -valueOffsetPlus
            } else {
                valueTextHeight + valueOffsetPlus
            }

            var negOffset = if (mChart.isDrawValueAboveBarEnabled) {
                valueTextHeight + valueOffsetPlus
            } else {
                -valueOffsetPlus
            }

            val inverted = mChart.isInverted(set.axisDependency)
            if (inverted) {
                posOffset = -posOffset - valueTextHeight
                negOffset = -negOffset - valueTextHeight
            }

            val buffer = mBarBuffers[index]
            val phaseY = mAnimator.phaseY
            val formatter = set.valueFormatter

            var j = -4

            // iconsOffset

            while (true) {
                j += 4
                if (j >= buffer.buffer.size * mAnimator.phaseX) {
                    println(j)
                    break
                }
                /*
                                val left = buffer.buffer[j]
                                val top = buffer.buffer[j + 1]
                                val right = buffer.buffer[j + 2]
                                val bottom = buffer.buffer[j + 3]


                 */
                val left = buffer.buffer[j]
                val top = buffer.buffer[j + 1]
                val right = buffer.buffer[j + 2]
                val bottom = buffer.buffer[j + 3]


//                val x = (left + right) / 2f
                val entry = set.getEntryForIndex(j / 4)

                /*
                val y = if (entry.y >= 0) {
                    top + posOffset
                } else {
                    bottom + negOffset
                }
                 */
                val x = (left + right) / 2f
                val y = if (entry.y >= 0) {
                    top + posOffset
                } else {
                    bottom + negOffset
                }
                if (!mViewPortHandler.isInBoundsRight(x)) {
                    break
                }

                if (!mViewPortHandler.isInBoundsY(top) or !mViewPortHandler.isInBoundsLeft(x)) {
                    continue
                }

                val packageName = "com.google.android.gm"
                val pm :PackageManager = this.mContext!!.packageManager
                val a = pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0))
                if (set.isDrawValuesEnabled) {
                    drawValue(aCanvas, a.toString(), left, (top) + (top-bottom)/5,
                        set.getValueTextColor(j / 4))

                }

                val bitmap = mImageList.getOrNull(j / 4)
                if (bitmap != null) {
                    val scaledBitmap = getScaledBitmap(bitmap)
                    aCanvas.drawBitmap(scaledBitmap, (left-valueOffsetPlus)/2F,
                        top - Utils.convertDpToPixel(15F)/*top + (bottom-top)/2F - valueOffsetPlus /2*/, null)
                }
            }
        }
    }

    private fun getScaledBitmap(aBitmap: Bitmap): Bitmap {
        val width = mContext.resources.getDimension(R.dimen.barchart_icon_size).toInt()
        val height = mContext.resources.getDimension(R.dimen.barchart_icon_size).toInt()
        return Bitmap.createScaledBitmap(aBitmap, width, height, true)
    }




}

class ChartAppFragment : Fragment(){

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var binding = FragmentAppChartBinding.inflate(inflater, container, false)
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
        // x축 위치 설정
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        // 그리드 선 수평 거리 설정
        xAxis.granularity = 1f
        // x축 텍스트 컬러 설정
        xAxis.textColor = Color.RED
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

    // 차트 데이터 설정
    private fun setData(barChart: HorizontalBarChart) {

        // Zoom In / Out 가능 여부 설정
        barChart.setScaleEnabled(false)

        val valueList = ArrayList<BarEntry>()
        val title = "걸음 수"
        var map = ArrayList<Bitmap>()
        // 임의 데이터
        var cnt = 0
        for (i in 0 until 50) {
            cnt++
            val packageName = "com.google.android.gm"
            fun isAppInstalled(packageName : String, packageManager : PackageManager) : Boolean{
                return try{
                    packageManager.getPackageInfo(packageName, 0)
                    true
                }catch (ex : PackageManager.NameNotFoundException){
                    false
                }
            }


            val pm :PackageManager = this.activity!!.packageManager
            val a = pm.getApplicationIcon(packageName)

            if(isAppInstalled(packageName, pm)){
                map.add(a.toBitmap())
                if(i == 3) {
                    valueList.add(BarEntry(i.toFloat(), i * 10000f,))
                }
                else{
                    valueList.add(BarEntry(i.toFloat(), i * 100f,))
                }
            }




        }
        val pixelPerUnit = resources.displayMetrics.densityDpi/160.0F
        barChart.layoutParams.height = (cnt * 60F*pixelPerUnit).toInt()

        val barDataSet = BarDataSet(valueList, title)
        val drawable = GradientDrawable()
        barChart.setPadding(0,0,0,0)
        // 바 색상 설정 (ColorTemplate.LIBERTY_COLORS)
        barDataSet.setColors(
            Color.rgb(238, 71, 77)
        )


        //barChart.setBackgroundColor(Color.RED)

        val data = BarData(barDataSet)

        data.barWidth = 0.1f

        data.setValueTextSize(Utils.convertDpToPixel(5F))
        barChart.data = data
        var ca = barChart.animator
        var vp = barChart.viewPortHandler
        barChart.renderer = HorizontalBarChartIconRenderer(barChart, ca, vp, map, context!!)


        barChart.invalidate()

    }
}