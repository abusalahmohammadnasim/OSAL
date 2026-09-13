package com.example.applock.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.applock.R

class AppListAdapter(
    private val fullAppList: List<AppInfo>,
    private val onLockChanged: (AppInfo, Boolean) -> Unit
) : RecyclerView.Adapter<AppListAdapter.AppViewHolder>() {

    private val displayedList: MutableList<AppInfo> = fullAppList.toMutableList()

    class AppViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgIcon: ImageView = view.findViewById(R.id.imgAppIcon)
        val tvName: TextView = view.findViewById(R.id.tvAppName)
        val switchLock: SwitchCompat = view.findViewById(R.id.switchLock)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false)
        return AppViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val app = displayedList[position]
        holder.tvName.text = app.appName
        holder.imgIcon.setImageDrawable(app.icon)

        holder.switchLock.setOnCheckedChangeListener(null)
        holder.switchLock.isChecked = app.isLocked

        holder.switchLock.setOnCheckedChangeListener { _, isChecked ->
            app.isLocked = isChecked
            onLockChanged(app, isChecked)
        }
    }

    override fun getItemCount(): Int = displayedList.size

    fun filter(query: String) {
        displayedList.clear()
        if (query.isEmpty()) {
            displayedList.addAll(fullAppList)
        } else {
            val lowerQuery = query.lowercase()
            for (app in fullAppList) {
                if (app.appName.lowercase().contains(lowerQuery)) {
                    displayedList.add(app)
                }
            }
        }
        notifyDataSetChanged()
    }
}
