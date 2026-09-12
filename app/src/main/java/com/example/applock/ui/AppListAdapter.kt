package com.example.applock.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.applock.databinding.ItemAppBinding

class AppListAdapter(
    private var appList: List<AppInfo>,
    private val onAppClick: (AppInfo, Boolean) -> Unit
) : RecyclerView.Adapter<AppListAdapter.AppViewHolder>() {

    fun updateApps(newApps: List<AppInfo>) {
        appList = newApps
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding = ItemAppBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AppViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        holder.bind(appList[position])
    }

    override fun getItemCount(): Int = appList.size

    inner class AppViewHolder(private val binding: ItemAppBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(appInfo: AppInfo) {
            binding.tvName.text = appInfo.appName
            binding.ivIcon.setImageDrawable(appInfo.icon)
            
            binding.switchLock.setOnCheckedChangeListener(null)
            binding.switchLock.isChecked = appInfo.isLocked

            binding.switchLock.setOnCheckedChangeListener { _, isChecked ->
                onAppClick(appInfo, isChecked)
            }
        }
    }
}
