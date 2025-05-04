package com.example.soundnest_android.ui.songs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.soundnest_android.R

class SongAdapter(
    private val items: List<Song>,
    private val onSongClick: (Song) -> Unit   // ← ahora recibe Song
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    inner class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTv: TextView = itemView.findViewById(R.id.tv_title)
        private val artistTv: TextView = itemView.findViewById(R.id.tv_artist)
        fun bind(song: Song) {
            titleTv.text = song.title
            artistTv.text = song.artist
            itemView.setOnClickListener {
                onSongClick(song)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_song, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size
}

