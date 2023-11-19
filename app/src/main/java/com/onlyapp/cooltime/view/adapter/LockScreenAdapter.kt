package com.onlyapp.cooltime.view.adapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat.startActivity
import com.onlyapp.cooltime.databinding.AppExceptionItemBinding

class AppItem(val name : String, val image : Drawable)

class LockViewHolder(val binding: AppExceptionItemBinding) :
    RecyclerView.ViewHolder(binding.root)

class LockScreenAdapter(private val activity: Context, private val appItems:MutableList<AppItem>, private val packageManager: PackageManager) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemCount(): Int {
        return appItems.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            RecyclerView.ViewHolder =
        LockViewHolder(AppExceptionItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding=(holder as LockViewHolder).binding
        binding.appIcon.background = appItems[position].image
        binding.appIcon.setOnClickListener{
            val intent = packageManager.getLaunchIntentForPackage(appItems[position].name)
            intent?.let {
                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(activity, it, null)
            }
        }
    }
}

