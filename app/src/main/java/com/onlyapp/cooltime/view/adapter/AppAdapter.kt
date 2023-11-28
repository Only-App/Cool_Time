package com.onlyapp.cooltime.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.onlyapp.cooltime.databinding.CheckExceptionAppItemBinding
import com.onlyapp.cooltime.model.ExceptAppModel


class AppAdapter(
    private val mListener: (item: ExceptAppModel) -> Unit
) : RecyclerView.Adapter<AppAdapter.AppListViewHolder>() {
    private val asyncDiffer = AsyncListDiffer(this, diffUtil)

    inner class AppListViewHolder(val binding: CheckExceptionAppItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(exceptAppModel: ExceptAppModel) {
            binding.appName.text = exceptAppModel.appName
            binding.appIcon.background = exceptAppModel.appIcon
            binding.exceptCheckbox.isChecked = exceptAppModel.checked

            binding.exceptCheckbox.setOnClickListener {
                mListener.invoke(exceptAppModel)
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

    fun replaceItems(items: List<ExceptAppModel>) {
        asyncDiffer.submitList(items)
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<ExceptAppModel>() {
            override fun areContentsTheSame(
                oldItem: ExceptAppModel,
                newItem: ExceptAppModel
            ): Boolean {
                return oldItem.checked == newItem.checked
            }

            override fun areItemsTheSame(oldItem: ExceptAppModel, newItem: ExceptAppModel): Boolean {
                return oldItem.packageName == newItem.packageName
            }
        }
    }
}
