package com.example.soundnest_android.ui.songs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.soundnest_android.R

class SongAdapter(
    private val onSongClick: (Song) -> Unit
) : ListAdapter<Song, SongAdapter.SongViewHolder>(SongDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_song, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        getItem(position)?.let { song ->
            holder.bind(song, onSongClick)
        }
    }

    class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivCover  = itemView.findViewById<ImageView>(R.id.iv_cover)
        private val tvTitle  = itemView.findViewById<TextView>(R.id.tv_title)
        private val tvArtist = itemView.findViewById<TextView>(R.id.tv_artist)

        fun bind(song: Song, onSongClick: (Song) -> Unit) {
            tvTitle.text  = song.title
            tvArtist.text = song.artist

            if (!song.coverUrl.isNullOrEmpty()) {
                Glide.with(itemView)
                    .load(song.coverUrl)
                    .placeholder(R.drawable.img_default_song)
                    .into(ivCover)
            } else {
                ivCover.setImageResource(R.drawable.img_default_song)
            }

            itemView.setOnClickListener { onSongClick(song) }
        }
    }

    class SongDiffCallback : DiffUtil.ItemCallback<Song>() {
        override fun areItemsTheSame(old: Song, new: Song) =
            old.id == new.id

        override fun areContentsTheSame(old: Song, new: Song) =
            old == new
    }
}
