package com.onlyapp.cooltime.view.adapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.ContextCompat.startActivity
import com.onlyapp.cooltime.databinding.AppExceptionItemBinding

class AppItem(val name : String, val image : Drawable)

internal class GridSpacingItemDecoration(
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
                left = (1- column/spanCount) * spacing
                right =(0 + (column + 1) / spanCount) * spacing
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

class LockViewHolder(val binding: AppExceptionItemBinding) :
    RecyclerView.ViewHolder(binding.root)

class LockScreenAdapter(private val activity: Context, private val datas:MutableList<AppItem>, private val packageManager: PackageManager) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemCount(): Int {
        return datas.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            RecyclerView.ViewHolder =
        LockViewHolder(AppExceptionItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding=(holder as LockViewHolder).binding
        //binding.appName.text = datas[position].name
        binding.appIcon.background = datas[position].image
        binding.appIcon.setOnClickListener{
            val intent = packageManager.getLaunchIntentForPackage(datas[position].name)
            intent!!.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(activity, intent, null)
        }
    }
}

