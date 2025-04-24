package com.example.soundnest_android.ui.playlists

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.soundnest_android.R

class PlaylistAdapter(
    private val context: Context,
    private val items: List<Playlist>
) : RecyclerView.Adapter<PlaylistAdapter.PlaylistVH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistVH {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_playlist, parent, false)
        return PlaylistVH(view)
    }

    override fun onBindViewHolder(holder: PlaylistVH, position: Int) {
        val p = items[position]
        holder.tvName.text = p.name
        holder.tvCount.text = "${p.songCount} canciones"
        holder.ivImage.setImageResource(p.imageResId)
        // Si usas Glide/Picasso para URL:
        // Glide.with(context).load(p.imageUrl).into(holder.ivImage)
    }

    override fun getItemCount(): Int = items.size

    class PlaylistVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivImage: ImageView = itemView.findViewById(R.id.iv_playlist_image)
        val tvName: TextView  = itemView.findViewById(R.id.tv_playlist_name)
        val tvCount: TextView = itemView.findViewById(R.id.tv_song_count)
    }
}