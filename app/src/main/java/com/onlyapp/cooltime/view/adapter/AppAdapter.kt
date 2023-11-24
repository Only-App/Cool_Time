package com.onlyapp.cooltime.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.onlyapp.cooltime.databinding.CheckExceptionAppItemBinding
import com.onlyapp.cooltime.model.ExceptAppItem


class AppAdapter(
    private val mListener: (item: ExceptAppItem) -> Unit
) : RecyclerView.Adapter<AppAdapter.AppListViewHolder>() {
    private val asyncDiffer = AsyncListDiffer(this, diffUtil)

    inner class AppListViewHolder(val binding: CheckExceptionAppItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(exceptAppItem: ExceptAppItem) {
            binding.appName.text = exceptAppItem.appName
            binding.appIcon.background = exceptAppItem.appIcon
            binding.exceptCheckbox.isChecked = exceptAppItem.checked

            binding.exceptCheckbox.setOnClickListener {
                mListener.invoke(exceptAppItem)
            }
        }
    }

    override fun getItemCount(): Int {
        return asyncDiffer.currentList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppListViewHolder {
        return AppListViewHolder(
            CheckExceptionAppItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: AppListViewHolder, position: Int) {
        holder.bind(asyncDiffer.currentList[position])
    }

    fun replaceItems(items: List<ExceptAppItem>) {
        asyncDiffer.submitList(items)
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<ExceptAppItem>() {
            override fun areContentsTheSame(
                oldItem: ExceptAppItem,
                newItem: ExceptAppItem
            ): Boolean {
                return oldItem.checked == newItem.checked
            }

            override fun areItemsTheSame(oldItem: ExceptAppItem, newItem: ExceptAppItem): Boolean {
                return oldItem.packageName == newItem.packageName
            }
        }
    }
}
