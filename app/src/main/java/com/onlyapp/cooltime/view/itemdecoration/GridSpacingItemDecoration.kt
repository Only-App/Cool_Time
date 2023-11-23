package com.onlyapp.cooltime.view.itemdecoration

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class GridSpacingItemDecoration(
    private val spanCount: Int, // Grid의 column 수
    private val spacing: Int // 간격
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position: Int = parent.getChildAdapterPosition(view)
        if (position >= 0) {
            val column = position % spanCount // item column
            outRect.apply {
                left = (1 - column / spanCount) * spacing
                right = (0 + (column + 1) / spanCount) * spacing
                if (position < spanCount) top = spacing
                bottom = spacing
            }
        } else {
            outRect.apply {
                left = 0
                right = 0
                top = 0
                bottom = 0
            }
        }
    }
}