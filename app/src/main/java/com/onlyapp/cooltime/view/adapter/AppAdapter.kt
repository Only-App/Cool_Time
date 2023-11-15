package com.onlyapp.cooltime.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.onlyapp.cooltime.databinding.CheckExceptionAppItemBinding
import com.onlyapp.cooltime.model.ExceptAppItem

class AppListViewHolder(
    val binding: CheckExceptionAppItemBinding) :
    RecyclerView.ViewHolder(binding.root)

class AppAdapter (private val data : MutableList<ExceptAppItem>,
                  private var mListener: OnCheckBoxChangedListener
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun getItemCount(): Int {
        return data.size
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            RecyclerView.ViewHolder =
            AppListViewHolder(CheckExceptionAppItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding=(holder as AppListViewHolder).binding

        binding.appName.text = data[position].appName
        binding.appIcon.background = data[position].appIcon
        binding.exceptCheckbox.isChecked = data[position].checked

        binding.exceptCheckbox.setOnClickListener {
            data[position] = ExceptAppItem(data[position].appName, data[position].packageName, data[position].appIcon, !data[position].checked)
            mListener.onChanged(data[position], position)
        }
    }
}

interface OnCheckBoxChangedListener{
    fun onChanged(item : ExceptAppItem,  position : Int)
}