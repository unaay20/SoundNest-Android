package com.example.soundnest_android.ui.songs

import android.annotation.SuppressLint
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
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
    private val showPlayIcon: Boolean,
    private val onSongClick: (Song) -> Unit,
    private val isScrollingProvider: () -> Boolean,
    private val isCompact: Boolean
) : ListAdapter<Song, SongAdapter.SongViewHolder>(SongDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_song, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.bind(getItem(position), showPlayIcon, onSongClick, isScrollingProvider, isCompact)
    }

    class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivCover = itemView.findViewById<ImageView>(R.id.iv_cover)
        private val tvTitle = itemView.findViewById<TextView>(R.id.tv_title)
        private val tvArtist = itemView.findViewById<TextView>(R.id.tv_artist)
        private val ivPlay = itemView.findViewById<ImageView>(R.id.iv_play)
        private val card = itemView.findViewById<MaterialCardView>(R.id.card_root)

        @SuppressLint("ClickableViewAccessibility")
        fun bind(
            song: Song,
            showPlayIcon: Boolean,
            onSongClick: (Song) -> Unit,
            isScrollingProvider: () -> Boolean,
            isCompact: Boolean
        ) {
            tvTitle.text = song.title
            tvArtist.text = song.artist
            if (!song.coverUrl.isNullOrEmpty()) {
                Glide.with(itemView)
                    .load(song.coverUrl)
                    .placeholder(R.drawable.img_soundnest_logo_svg)
                    .into(ivCover)
            } else {
                ivCover.setImageResource(R.drawable.img_soundnest_logo_svg)
            }

            ivPlay.visibility = if (showPlayIcon) View.VISIBLE else View.GONE

            var startX = 0f
            var startY = 0f
            val touchSlop = ViewConfiguration.get(itemView.context).scaledTouchSlop

            card.setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        startX = event.x
                        startY = event.y
                        v.animate()
                            .scaleX(0.97f)
                            .scaleY(0.97f)
                            .translationZ(2f)
                            .setDuration(100)
                            .start()
                    }

                    MotionEvent.ACTION_UP -> {
                        v.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .translationZ(8f)
                            .setDuration(100)
                            .withEndAction {
                                val dx = Math.abs(event.x - startX)
                                val dy = Math.abs(event.y - startY)
                                if (dx < touchSlop && dy < touchSlop && !isScrollingProvider()) {
                                    onSongClick(song)
                                }
                            }
                            .start()
                    }

                    MotionEvent.ACTION_CANCEL -> {
                        v.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .translationZ(8f)
                            .setDuration(100)
                            .start()
                    }
                }
                true
            }
            val ctx = itemView.context
            val coverSize = if (isCompact)
                ctx.resources.getDimensionPixelSize(R.dimen.song_cover_size_compact)
            else
                ctx.resources.getDimensionPixelSize(R.dimen.song_cover_size_normal)

            val playSize = if (isCompact)
                ctx.resources.getDimensionPixelSize(R.dimen.song_play_icon_size_compact)
            else
                ctx.resources.getDimensionPixelSize(R.dimen.song_play_icon_size_normal)

            val padding = if (isCompact)
                ctx.resources.getDimensionPixelSize(R.dimen.song_padding_compact)
            else
                ctx.resources.getDimensionPixelSize(R.dimen.song_padding_normal)

            val titleTextSize = if (isCompact)
                ctx.resources.getDimension(R.dimen.song_title_text_size_compact)
            else
                ctx.resources.getDimension(R.dimen.song_title_text_size_normal)

            val artistTextSize = if (isCompact)
                ctx.resources.getDimension(R.dimen.song_artist_text_size_compact)
            else
                ctx.resources.getDimension(R.dimen.song_artist_text_size_normal)

            val cardElevation = if (isCompact)
                ctx.resources.getDimension(R.dimen.song_card_elevation_compact)
            else
                ctx.resources.getDimension(R.dimen.song_card_elevation_normal)

            val cardRadius = if (isCompact)
                ctx.resources.getDimension(R.dimen.song_card_radius_compact)
            else
                ctx.resources.getDimension(R.dimen.song_card_radius_normal)

            // 1) Ajustar portada
            ivCover.layoutParams = ivCover.layoutParams.apply {
                width = coverSize
                height = coverSize
            }
            ivCover.requestLayout()

            // 2) Ajustar play icon
            ivPlay.layoutParams = ivPlay.layoutParams.apply {
                width = playSize
                height = playSize
            }
            ivPlay.setPadding(padding / 2, padding / 2, padding / 2, padding / 2)
            ivPlay.requestLayout()

            // 3) Ajustar card
            card.cardElevation = cardElevation
            card.radius = cardRadius.toFloat()

            // 4) Ajustar textos
            tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleTextSize)
            tvArtist.setTextSize(TypedValue.COMPLEX_UNIT_PX, artistTextSize)

            // 5) Ajustar padding interno
            (itemView as ViewGroup).setPadding(padding, padding, padding, padding)
        }
    }

    class SongDiffCallback : DiffUtil.ItemCallback<Song>() {
        override fun areItemsTheSame(old: Song, new: Song) = old.id == new.id
        override fun areContentsTheSame(old: Song, new: Song) = old == new
    }
}
