package com.example.soundnest_android.ui.playlists

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.soundnest_android.databinding.FragmentPlaylistsBinding
import com.example.soundnest_android.R

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

        playlists = mutableListOf(
            Playlist("Rock Classics", 48, R.drawable.img_party_background),
            Playlist("Chill Vibes", 32, R.drawable.img_soundnest_logo_svg)
            // … más items …
        )

        adapter = PlaylistAdapter(requireContext(), playlists)
        binding.rvPlaylists.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}