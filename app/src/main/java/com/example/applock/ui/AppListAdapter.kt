package com.example.applock.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.applock.databinding.ItemAppBinding

class AppListAdapter(
    private val apps: MutableList<AppInfo>,
    private val onToggle: (AppInfo, Boolean) -> Unit
) : RecyclerView.Adapter<AppListAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemAppBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAppBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = apps[position]
        holder.binding.tvName.text = app.label
        holder.binding.ivIcon.setImageDrawable(app.icon)
        holder.binding.switchLock.setOnCheckedChangeListener(null)
        holder.binding.switchLock.isChecked = app.locked
        holder.binding.switchLock.setOnCheckedChangeListener { _, isChecked ->
            app.locked = isChecked
            onToggle(app, isChecked)
        }
    }

    override fun getItemCount() = apps.size
}
