package com.example.soundnest_android.ui.songs

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.soundnest_android.R
import com.example.soundnest_android.business_logic.Song
import com.google.android.material.card.MaterialCardView

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
        private val card        = itemView.findViewById<MaterialCardView>(R.id.card_root)

        @SuppressLint("ClickableViewAccessibility")
        fun bind(song: Song, onSongClick: (Song) -> Unit) {
            tvTitle.text  = song.title
            tvArtist.text = song.artist

            if (!song.coverUrl.isNullOrEmpty()) {
                Glide.with(itemView)
                    .load(song.coverUrl)
                    .placeholder(R.drawable.img_soundnest_logo_svg)
                    .into(ivCover)
            } else {
                ivCover.setImageResource(R.drawable.img_soundnest_logo_svg)
            }

            card.setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        v.animate()
                            .scaleX(0.97f).scaleY(0.97f)
                            .translationZ(2f)
                            .setDuration(100).start()
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        v.animate()
                            .scaleX(1f).scaleY(1f)
                            .translationZ(8f)
                            .setDuration(100).withEndAction {
                                if (event.action == MotionEvent.ACTION_UP) {
                                    v.performClick()
                                }
                            }.start()
                    }
                }
                true
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
