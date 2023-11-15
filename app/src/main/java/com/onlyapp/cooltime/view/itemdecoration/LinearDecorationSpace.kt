package com.onlyapp.cooltime.view.itemdecoration

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class LinearDecorationSpace(private val divHeight: Int) : RecyclerView.ItemDecoration() { // 리스트 여백 주는 데코
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        //맨아래 리스트가 아니면 아래에 설정한만큼 공간 설정
        if (parent.getChildAdapterPosition(view) != parent.adapter?.itemCount?.minus(1)) {
            outRect.bottom = divHeight
        }
    }
}