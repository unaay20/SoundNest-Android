package com.example.soundnest_android.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.soundnest_android.R

class SkeletonAdapter(private val count: Int) : RecyclerView.Adapter<SkeletonAdapter.Holder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        Holder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_playlist_skeleton, parent, false)
        )

    override fun getItemCount() = count
    override fun onBindViewHolder(holder: Holder, position: Int) { /* no-op */
    }

    class Holder(v: View) : RecyclerView.ViewHolder(v)
}