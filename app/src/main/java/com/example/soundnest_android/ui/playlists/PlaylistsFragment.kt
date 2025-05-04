package com.example.soundnest_android.ui.playlists

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.soundnest_android.databinding.FragmentPlaylistsBinding
import com.example.soundnest_android.R
import com.example.soundnest_android.ui.songs.PlaylistDetailActivity
import com.example.soundnest_android.ui.songs.Song

class PlaylistsFragment : Fragment() {

    private var _binding: FragmentPlaylistsBinding? = null
    private val binding get() = _binding!!

    private lateinit var playlists: MutableList<Playlist>
    private lateinit var adapter: PlaylistAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvPlaylists.layoutManager = GridLayoutManager(requireContext(), 2)

        // 1. Definimos las canciones de cada playlist
        val rockSongs = listOf(
            Song("Bohemian Rhapsody", "Queen",        R.drawable.im_cover_bohemian),
            Song("Stairway to Heaven", "Led Zeppelin",R.drawable.img_cover_imagine),
            Song("Hotel California", "Eagles",        R.drawable.img_cover_imagine),
        )

        val chillSongs = listOf(
            Song("Weightless", "Marconi Union",       R.drawable.img_soundnest_pure_logo_white),
            Song("Sunset Lover", "Petit Biscuit",     R.drawable.img_soundnest_logo),
            Song("Night Owl", "Gerry Rafferty",       R.drawable.img_default_song),
        )

        playlists = listOf(
            Playlist("Rock Classics", rockSongs, R.drawable.img_party_background),
            Playlist("Chill Vibes",    chillSongs, R.drawable.img_soundnest_logo_svg)
        ).toMutableList()

        adapter = PlaylistAdapter(playlists) { playlist ->
            val intent = Intent(requireContext(), PlaylistDetailActivity::class.java)
                .apply {
                    putExtra("EXTRA_PLAYLIST_NAME",  playlist.name)
                    putExtra("EXTRA_PLAYLIST_IMAGE", playlist.imageResId)
                    // aquí convertimos la List<Song> a un ArrayList<Song>
                    putExtra(
                        "EXTRA_PLAYLIST_SONGS",
                        ArrayList(playlist.songs)        // <- la magia
                    )
                }
            startActivity(intent)
        }
        binding.rvPlaylists.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}