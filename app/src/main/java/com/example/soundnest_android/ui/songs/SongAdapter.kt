package com.example.soundnest_android.ui.songs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.soundnest_android.R

class SongAdapter(
    private val items: List<Song>
) : RecyclerView.Adapter<SongAdapter.SongVH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongVH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_song, parent, false)
        return SongVH(view)
    }

    override fun onBindViewHolder(holder: SongVH, position: Int) {
        val s = items[position]
        holder.tvTitle.text  = s.title
        holder.tvArtist.text = s.artist
        holder.ivCover.setImageResource(s.coverResId)
    }

    override fun getItemCount(): Int = items.size

    class SongVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivCover: ImageView = itemView.findViewById(R.id.iv_cover)
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvArtist: TextView  = itemView.findViewById(R.id.tv_artist)
    }
}
