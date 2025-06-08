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
    private var items: List<Playlist>,
    private val onItemClick: (Playlist) -> Unit,
    private val onItemEdit: (Playlist) -> Unit,
    private val onItemDelete: (Playlist) -> Unit
) : RecyclerView.Adapter<PlaylistAdapter.PlaylistVH>() {

    fun setItems(newItems: List<Playlist>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistVH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_playlist, parent, false)
        return PlaylistVH(view)
    }

    override fun getItemCount(): Int = items.size

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
            PopupMenu(it.context, it).apply {
                menuInflater.inflate(R.menu.playlist_menu, menu)
                setOnMenuItemClickListener { mi ->
                    when (mi.itemId) {
                        R.id.menu_edit_playlist -> onItemEdit(playlist).let { true }
                        R.id.menu_delete_playlist -> onItemDelete(playlist).let { true }
                        else -> false
                    }
                }
                show()
            }
            true
        }
    }

    class PlaylistVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivImage: ImageView = itemView.findViewById(R.id.iv_playlist_image)
        val tvName: TextView = itemView.findViewById(R.id.tv_playlist_name)
        val tvCount: TextView = itemView.findViewById(R.id.tv_song_count)
    }
}
