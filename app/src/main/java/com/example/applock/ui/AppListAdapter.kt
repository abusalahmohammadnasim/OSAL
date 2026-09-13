package com.example.applock.ui

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.applock.R

data class AppInfo(
    val appName: String,
    val packageName: String,
    val icon: Drawable,
    var isLocked: Boolean
)

class AppListAdapter(
    private var apps: List<AppInfo>,
    private val onLockChanged: (AppInfo, Boolean) -> Unit
) : RecyclerView.Adapter<AppListAdapter.AppViewHolder>() {

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
        val app = apps[position]
        holder.tvName.text = app.appName
        holder.imgIcon.setImageDrawable(app.icon)
        
        holder.switchLock.setOnCheckedChangeListener(null)
        holder.switchLock.isChecked = app.isLocked

        holder.switchLock.setOnCheckedChangeListener { _, isChecked ->
            onLockChanged(app, isChecked)
        }
    }

    override fun getItemCount(): Int = apps.size

    fun updateApps(newApps: List<AppInfo>) {
        this.apps = newApps
        notifyDataSetChanged()
    }
}
