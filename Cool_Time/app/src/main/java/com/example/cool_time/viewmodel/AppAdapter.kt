package com.example.cool_time.viewmodel

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.cool_time.databinding.CheckExceptionAppItemBinding

class AppListViewHolder(val binding: CheckExceptionAppItemBinding) :
    RecyclerView.ViewHolder(binding.root)

class AppAdapter (private val datas:MutableList<AppItem>, private val recyclerView: RecyclerView, private val uncheckData:MutableList<AppItem>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun getItemCount(): Int {
        return datas.size
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            RecyclerView.ViewHolder =
            AppListViewHolder(CheckExceptionAppItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding=(holder as AppListViewHolder).binding
        binding.appName.text = datas[position].name
        binding.appIcon.background = datas[position].image
        binding.checkButton.setOnCheckedChangeListener { button, b ->
            uncheckData.add(datas[position])
            datas.removeAt(position)
            this.notifyDataSetChanged()
        }

    }
}