package com.example.soundnest_android.ui.playlists

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.soundnest_android.R
import com.example.soundnest_android.business_logic.Playlist
import com.squareup.picasso.Picasso

class PlaylistAdapter(
    private val items: MutableList<Playlist>,
    private val onItemClick: (Playlist) -> Unit,
    private val onItemLongClick: (Playlist) -> Unit
) : RecyclerView.Adapter<PlaylistAdapter.PlaylistVH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistVH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_playlist, parent, false)
        return PlaylistVH(view)
    }

    override fun onBindViewHolder(holder: PlaylistVH, position: Int) {
        val playlist = items[position]
        holder.tvName.text = playlist.name
        holder.tvCount.text = "${playlist.songs.size} songs"
        Picasso.get()
            .load(playlist.imageUri)
            .placeholder(R.drawable.img_soundnest_logo_svg)
            .error(R.drawable.img_soundnest_logo_svg)
            .fit()
            .centerCrop()
            .into(holder.ivImage)
        holder.itemView.setOnClickListener { onItemClick(playlist) }
        holder.itemView.setOnLongClickListener {
            val popup = PopupMenu(it.context, it)
            popup.menuInflater.inflate(R.menu.playlist_menu, popup.menu)
            popup.setOnMenuItemClickListener { item ->
                if (item.itemId == R.id.menu_delete_playlist) {
                    onItemLongClick(playlist)
                    true
                } else false
            }
            popup.show()
            true
        }
    }

    override fun getItemCount(): Int = items.size

    fun removeItem(playlist: Playlist) {
        val pos = items.indexOf(playlist)
        if (pos != -1) {
            items.removeAt(pos)
            notifyItemRemoved(pos)
        }
    }

    class PlaylistVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivImage: ImageView = itemView.findViewById(R.id.iv_playlist_image)
        val tvName: TextView = itemView.findViewById(R.id.tv_playlist_name)
        val tvCount: TextView = itemView.findViewById(R.id.tv_song_count)
    }
}
