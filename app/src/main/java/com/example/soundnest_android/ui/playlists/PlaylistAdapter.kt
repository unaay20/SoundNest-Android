package com.example.soundnest_android.ui.playlists

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.example.soundnest_android.R

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
        holder.tvCount.text = "${playlist.songs.count()} canciones"

        val imageUri = Uri.parse(playlist.imageUri)
        holder.ivImage.setImageURI(imageUri)

        holder.itemView.setOnClickListener {
            onItemClick(playlist)
        }

        holder.itemView.setOnLongClickListener {
            showPopupMenu(holder.itemView, playlist)
            true
        }
    }

    override fun getItemCount(): Int = items.size


    fun removeItem(playlist: Playlist) {
        val position = items.indexOf(playlist)
        if (position != -1) {
            items.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    private fun showPopupMenu(view: View, playlist: Playlist) {
        val popupMenu = PopupMenu(view.context, view)
        popupMenu.menuInflater.inflate(R.menu.playlist_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_delete_playlist -> {
                    onItemLongClick(playlist)
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    class PlaylistVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivImage: ImageView = itemView.findViewById(R.id.iv_playlist_image)
        val tvName: TextView = itemView.findViewById(R.id.tv_playlist_name)
        val tvCount: TextView = itemView.findViewById(R.id.tv_song_count)
    }
}
