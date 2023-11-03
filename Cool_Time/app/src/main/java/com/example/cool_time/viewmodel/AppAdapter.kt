package com.example.cool_time.viewmodel

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.cool_time.databinding.CheckExceptionAppItemBinding
import com.example.cool_time.model.ExceptAppItem

class AppListViewHolder(
    val binding: CheckExceptionAppItemBinding) :
    RecyclerView.ViewHolder(binding.root)

class AppAdapter (private val datas : MutableList<ExceptAppItem>,
                  private var mListener: OnCheckBoxChangedListener) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun getItemCount(): Int {
        return datas.size
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            RecyclerView.ViewHolder =
            AppListViewHolder(CheckExceptionAppItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding=(holder as AppListViewHolder).binding

        binding.appName.text = datas[position].appName
        binding.appIcon.background = datas[position].appIcon
        binding.exceptCheckbox.isChecked = datas[position].checked

        binding.exceptCheckbox.setOnClickListener {
            datas[position].checked = !datas[position].checked
            mListener.onChanged(datas[position], position)
        }
    }
}

interface OnCheckBoxChangedListener{
    fun onChanged(item : ExceptAppItem,  position : Int)
}