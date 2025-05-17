package com.example.soundnest_android.ui.playlists

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.soundnest_android.databinding.FragmentPlaylistsBinding
import com.example.soundnest_android.ui.songs.PlaylistDetailActivity
import com.google.android.material.snackbar.Snackbar
class PlaylistsFragment : Fragment() {

    private var _binding: FragmentPlaylistsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PlaylistsViewModel by viewModels()
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

        viewModel.playlists.observe(viewLifecycleOwner) { playlists ->
            adapter = PlaylistAdapter(playlists.toMutableList(), { playlist ->
                val intent = Intent(requireContext(), PlaylistDetailActivity::class.java)
                    .apply {
                        putExtra("EXTRA_PLAYLIST_NAME", playlist.name)
                        putExtra("EXTRA_PLAYLIST_IMAGE", playlist.imageUri)
                        putExtra("EXTRA_PLAYLIST_SONGS", ArrayList(playlist.songs))
                    }
                startActivity(intent)
            }, { playlist ->
                viewModel.deletePlaylist(playlist)
                Snackbar.make(binding.root, "Playlist deleted", Snackbar.LENGTH_SHORT).show()
                adapter.removeItem(playlist)
            })

            binding.rvPlaylists.adapter = adapter
        }

        binding.fabAddPlaylist.setOnClickListener {
            val dialog = NewPlaylistDialogFragment()
            dialog.onPlaylistCreated = { name, description, imageUri ->
                viewModel.createPlaylist(name, description, imageUri)
            }
            dialog.show(parentFragmentManager, "NewPlaylistDialog")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
