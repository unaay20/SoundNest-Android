package com.example.soundnest_android.ui.playlists

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.soundnest_android.R
import com.example.soundnest_android.business_logic.Playlist

class PlaylistPopupAdapter(
    private val items: List<Playlist>,
    private val onClick: (Playlist) -> Unit
) : RecyclerView.Adapter<PlaylistPopupAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        private val img = view.findViewById<ImageView>(R.id.imgPlaylistIcon)
        private val tv = view.findViewById<TextView>(R.id.tvPlaylistName)
        fun bind(p: Playlist) {
            tv.text = p.name
            Glide.with(img.context)
                .load(p.imageUri)
                .placeholder(R.drawable.img_soundnest_logo_svg)
                .error(R.drawable.img_soundnest_logo_svg)
                .centerCrop()
                .into(img)
            itemView.setOnClickListener { onClick(p) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_playlist_popup, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size
}
