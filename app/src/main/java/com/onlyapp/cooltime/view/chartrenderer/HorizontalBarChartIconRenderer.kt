package com.onlyapp.cooltime.view.chartrenderer

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Path
import com.github.mikephil.charting.animation.ChartAnimator
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.renderer.HorizontalBarChartRenderer
import com.github.mikephil.charting.utils.Utils
import com.github.mikephil.charting.utils.ViewPortHandler
import com.onlyapp.cooltime.R

class HorizontalBarChartIconRenderer(aChart: HorizontalBarChart, aAnimator: ChartAnimator,
                                     aViewPortHandler: ViewPortHandler, appList : List<Pair<String, Long>>, aImageList: ArrayList<Bitmap>,
                                     aContext: Context
)
: HorizontalBarChartRenderer(aChart, aAnimator, aViewPortHandler) {
    private val mAppList = appList
    private val mContext = aContext
    private val mImageList = aImageList
    private var mRadius=100F // round 크기 변수

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

        // x와 y축에 대한 rounding의 최대 크기를 막대바의 width 혹은 height 둘 중 작은 값의 절반을 넘지 않도록 설정함
        // (절반보다 크면 제대로 원형으로 rounding이 예쁘게 안될테니까, x,y의 rounding 크기가 달라도 예쁘지 않고)
        if (rxValue > width / 2) {rxValue = width / 2; ryValue = rxValue}
        if (ryValue > height / 2) {ryValue = height / 2; rxValue = ryValue}

        // 곡선을 제외한 y축으로부터 수직인 바의 길이
        val widthMinusCorners = (width - (2 * rxValue))
        //val heightMinusCorners = (height - (2 * ryValue))

        // 안드로이드에서 그래픽을 그리고 경로를 정의하는데 사용되는 클래스
        /* 참고사항 : path관련 상대좌표를 매개변수로 받는 함수에서
                    y인자를 음수로 설정하면 막대바의 위치가 오히려 더 위로 올라감, 양수를 줘야지 아래로 내려감,
                    x값은 음수면 왼쪽, 플러스면 오른쪽으로 이동함
         */
        val path = Path()

        // round를 그릴 시작점 설정 -> y가 단순히 top이 아니라 top + ryvalue인 이유는 막대 위에 ryvalue 높이만큼을 올라가면서 그릴거기 때문에 top에서 ryvalue만큼 아래에 위치해야 하기 때문에 ryvalue 만큼을 더해줌
        // 만약 top-right의 radius 설정을 true로 했다면 굴곡 설정
        // rQuadTo는 현재위치로부터 중간 좌표() 목표 좌표를 넘겨받아서 시작 좌표 => 현재 좌표에서 목표 좌표로 가는 직선이 있다 가정했을 때 그 선을 중간 좌표 위치로 활 시위를 잡아 당기듯이 당겼을 때 생기는 꼴의 곡선을 그려주는 함수
        // => 만약 현재 좌표 <=> 중간 좌표, 중간 좌표 <=> 목표 좌표 간의 거리가 동일하다면 반지름이 일정한 반원 같은 꼴의 곡선을 그려줄 것임을 알 수 있음
        // 현재 위치가 (right, top + ryValue) =>(막대 바의 오른쪽 위 굴곡이 시작되는 점)이기 때문에 여기에서 rxValue만큼 왼쪽으로, ryValue만큼 위로 이동하면서 곡선을 그려 둥글게 해준다.
        path.moveTo(right, top + ryValue)

        if (tr) path.rQuadTo(0f, -ryValue, -rxValue, -ryValue) // top-right corner
        else {
            // 만약 설정 안했으면 직선으로 그려야 하므로 원래 y값(top)까지 그대로 위로 쭉 직선으로 그린다. ㅣ 꼴로 그리는 것
            // 참고사항 : rLineTo, rQuadTo 등으로 그리고 나면 현재 위치가 바뀐다 => 다음에 다시 rLineTo등 상대 좌표를 이용한 함수를 이용할 때 현재 위치를 계산할 필요가 있음
            //위로 그렸으니까 왼쪽으로 rxValue만큼 직선으로 다시 그어준다. - 꼴로 그리는 것 => 즉 결과적으로 ㄱ자 꼴로 막대 테두리 그려준 셈
            path.rLineTo(0f, -ryValue)
            path.rLineTo(-rxValue, 0f)
        }

        // top-left의 곡선 시작 부분까지 직선으로 쭉 그으며 접근
        path.rLineTo(-widthMinusCorners, 0f)

        // 현재위치는 top-left의 시작부분
        // 만약 top-left radius 설정을 true로 했다면 왼쪽 아래로 내려가면서 반원을 그린다
        if (tl) path.rQuadTo(-rxValue, 0f, -rxValue, ryValue) // top-left corner
        else {
            path.rLineTo(-rxValue, 0f)
            path.rLineTo(0f, ryValue)
        }

        //path.rLineTo(0f, heightMinusCorners)

        // 현재 좌변의 중앙, top-left를 그릴 때와 동일한 방법으로 체크 했으면 동일하게 처리해서 그려준다.
        if (bl) path.rQuadTo(0f, ryValue, rxValue, ryValue) // bottom-left corner
        else {
            path.rLineTo(0f, ryValue)
            path.rLineTo(rxValue, 0f)
        }

        // bottom-left의 곡선 종료 부분에서 bottom-right의 곡선 시작 부분까지 직선으로 쭉 그으며 접근
        path.rLineTo(widthMinusCorners, 0f)

        //이하 bottom-right, 나머지와 동일한 방법으로 체크 했으면 동일하게 처리해서 그려준다.
        if (br) path.rQuadTo(rxValue, 0f, rxValue, -ryValue) // bottom-right corner
        else {
            path.rLineTo(rxValue, 0f)
            path.rLineTo(0f, -ryValue)
        }

        //path.rLineTo(0f, -heightMinusCorners)
        path.close() // Given close, last lineto can be removed.

        return path // 막대 반환
    }
    override fun drawDataSet(c: Canvas, dataSet: IBarDataSet, index: Int) {
        val trans = mChart.getTransformer(dataSet.axisDependency)
        mShadowPaint.color = dataSet.barShadowColor
        val phaseX = mAnimator.phaseX
        val phaseY = mAnimator.phaseY

        if (mBarBuffers != null) {
            // Initialize the buffer
            val buffer = mBarBuffers[index]
            buffer.setPhases(phaseX, phaseY)
            //buffer. = index
            buffer.setBarWidth(mChart.barData.barWidth)

            //x or y 축이 반전되었는지 확인해서 그대로 설정
            buffer.setInverted(mChart.isInverted(dataSet.axisDependency))

            //dataset 이용해서 데이터 처리 및 좌표 계산
            buffer.feed(dataSet)

            // 데이터 좌표 값을 pixel 값으로 변환
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
                            val path = roundedRect(
                                buffer.buffer[j],
                                buffer.buffer[j + 1],
                                buffer.buffer[j + 2],
                                buffer.buffer[j + 3],
                                mRadius, mRadius,
                                tl = true, tr = true, br = true, bl = true
                            )
                            c.drawPath(path, mShadowPaint)
                        } else {
                            c.drawRect(
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
                        val path = roundedRect(
                            buffer.buffer[j],
                            buffer.buffer[j + 1],
                            buffer.buffer[j + 2],
                            buffer.buffer[j + 3],
                            mRadius, mRadius,
                            tl = true, tr = true, br = true, bl = true
                        )
                        c.drawPath(path, mRenderPaint)
                    } else {
                        c.drawRect(
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
                    if (mChart.isDrawBarShadowEnabled) {
                        if (mRadius > 0) {
                            val path = roundedRect(
                                buffer.buffer[j],
                                buffer.buffer[j + 1],
                                buffer.buffer[j + 2],
                                buffer.buffer[j + 3],
                                mRadius, mRadius,
                                tl = true, tr = true, br = true, bl = true
                            )
                            c.drawPath(path, mRenderPaint)
                        } else {
                            c.drawRect(
                                buffer.buffer[j],
                                mViewPortHandler.contentTop(),
                                buffer.buffer[j + 2],
                                mViewPortHandler.contentBottom(),
                                mShadowPaint
                            )
                        }
                    }
                    if (mRadius > 0) { //결론적으로 본 앱에서 차트 그릴 때 사용할 코드
                        buffer.buffer[j] = 100F
                        val path = roundedRect(
                            buffer.buffer[j],
                            buffer.buffer[j + 1],
                            buffer.buffer[j + 2],
                            buffer.buffer[j + 3],
                            mRadius, mRadius,
                            tl = true, tr = true, br = true, bl = true
                        )
                        c.drawPath(path, mRenderPaint) // path에 해당하는 부분을 그림
                    } else {
                        c.drawRect(
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

    // 막대 데이터 값을 커스텀하기 위해 override
    override fun drawValues(aCanvas: Canvas) {

        // 애초에 설정을 안했으면 그대로 종료

        val datasets = mChart.barData.dataSets
        // 아이콘 크기만큼 떨어진 채로 시작할 offset
        val iconSize = Utils.convertPixelsToDp(mContext.resources.getDimension(R.dimen.barchart_icon_size))

        //Utils.drawImage()

        for ((index, set) in datasets.withIndex()) {
            if (!shouldDrawValues(set)) {
                continue
            }

            applyValueTextStyle(set) // 막대 차트 데이터 값 표시

            val buffer = mBarBuffers[index]

            // iconsOffset
            var j = -4
            while (true) {
                j += 4

                // buffer.buffer.size는 (상하좌우)4 * 막대 개수 크기 반환 => 모든 막대에 대해 처리하고 나면 탈출
                if (j >= buffer.buffer.size) {
                    break
                }

                val left = buffer.buffer[j]
                val top = buffer.buffer[j + 1]
                val right = buffer.buffer[j + 2]
                val bottom = buffer.buffer[j + 3]

                val packageName = mAppList[j/4].first
                val packageManager : PackageManager = this.mContext.packageManager
                val appinfo: ApplicationInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)

                val appName = packageManager.getApplicationLabel(appinfo).toString()//packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, 0))
                if (set.isDrawValuesEnabled) {
                    // 앱 이름 나올 위치 설정 => 막대 차트의 왼쪽과 아이콘 상단 라인에 맞춰서
                    drawValue(aCanvas, appName + " " +
                            (mAppList[j/4].second/3600).toInt()+"시간 " +
                            (mAppList[j/4].second.toFloat()/60%60).toInt()+"분 " +
                            mAppList[j/4].second%60+"초",
                        left, top-15 ,
                        set.getValueTextColor(j / 4))

                }

                val bitmap = mImageList.getOrNull(j / 4)
                if (bitmap != null) {
                    val scaledBitmap = getScaledBitmap(bitmap)
                    // 앱 좌상단이 기준이어서 차트의 바닥 라인과 맞출수 있도록 아이콘 크기만큼 아래로 내려가게, => 같은 크기로 했더니 착시 효과로 라인이 더 높게 보여서 살짝 덜 올라가게 함
                    // 맨 왼쪽과 차트 사이 공간의 중간에 위치하도록
                    aCanvas.drawBitmap(scaledBitmap, (left- Utils.convertDpToPixel(iconSize))/2F,
                        bottom- Utils.convertDpToPixel(23F), null)
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