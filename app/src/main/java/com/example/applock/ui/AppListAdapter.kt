package com.example.applock.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.applock.databinding.ItemAppBinding

class AppListAdapter(
    private val onAppClick: (AppInfo, Boolean) -> Unit
) : RecyclerView.Adapter<AppListAdapter.AppViewHolder>() {

    private var appList: List<AppInfo> = emptyList()

    fun submitList(newList: List<AppInfo>) {
        appList = newList
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
            binding.tvAppName.text = appInfo.label
            binding.imgAppIcon.setImageDrawable(appInfo.icon)
            binding.switchLock.isChecked = appInfo.locked

            binding.switchLock.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked != appInfo.locked) {
                    onAppClick(appInfo, isChecked)
                }
            }
        }
    }
}
