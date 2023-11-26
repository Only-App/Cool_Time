package com.onlyapp.cooltime.view.adapter

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.onlyapp.cooltime.databinding.AppExceptionItemBinding

class AppItem(val name: String, val image: Drawable)

class LockViewHolder(val binding: AppExceptionItemBinding) :
    RecyclerView.ViewHolder(binding.root)

class LockScreenAdapter(private val appItems: MutableList<AppItem>, private val mStartApp: (packageName: String) -> Unit) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemCount(): Int {
        return appItems.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            RecyclerView.ViewHolder =
        LockViewHolder(AppExceptionItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as LockViewHolder).binding
        binding.appIcon.background = appItems[position].image
        binding.appIcon.setOnClickListener {
            mStartApp.invoke(appItems[position].name)
        }
    }
}

