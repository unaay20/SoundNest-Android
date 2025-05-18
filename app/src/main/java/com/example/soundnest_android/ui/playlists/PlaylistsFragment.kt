package com.example.soundnest_android.ui.playlists

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.soundnest_android.auth.SharedPrefsTokenProvider
import com.example.soundnest_android.databinding.FragmentPlaylistsBinding
import com.example.soundnest_android.restful.constants.RestfulRoutes
import com.example.soundnest_android.ui.songs.PlaylistDetailActivity
import com.example.soundnest_android.utils.UriToFileUtil
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.io.File

class PlaylistsFragment : Fragment() {

    private var _binding: FragmentPlaylistsBinding? = null
    private val binding get() = _binding!!
    private val sharedPrefs by lazy { SharedPrefsTokenProvider(requireContext()) }
    private val userIdString get() = sharedPrefs.getUserId().toString()

    private val viewModel: PlaylistsViewModel by viewModels {
        PlaylistsViewModelFactory(
            application = requireActivity().application,
            baseUrl = RestfulRoutes.getBaseUrl(),
            tokenProvider = sharedPrefs,
            userId = userIdString
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPlaylistsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.rvPlaylists.layoutManager = GridLayoutManager(requireContext(), 2)

        viewModel.playlists.observe(viewLifecycleOwner) { playlists ->
            binding.rvPlaylists.adapter = PlaylistAdapter(
                playlists,
                { p ->
                    startActivity(Intent(requireContext(), PlaylistDetailActivity::class.java).apply {
                        putExtra("EXTRA_PLAYLIST_NAME", p.name)
                        putExtra("EXTRA_PLAYLIST_IMAGE", p.imageUri)
                        putExtra("EXTRA_PLAYLIST_SONGS", ArrayList(p.songs))
                    })
                },
                { p ->
                    viewModel.deletePlaylist(p)
                    Snackbar.make(binding.root, "Playlist deleted", Snackbar.LENGTH_SHORT).show()
                }
            )
        }

        binding.fabAddPlaylist.setOnClickListener {
            val dialog = NewPlaylistDialogFragment()
            dialog.onPlaylistCreated = { name, description, imageUriStr ->
                lifecycleScope.launch {
                    val imageFile: File? = imageUriStr
                        ?.let(Uri::parse)
                        ?.let { UriToFileUtil.getFileFromUri(requireContext(), it) }
                    viewModel.createPlaylist(name, description ?: "", imageFile)
                }
            }
            dialog.show(parentFragmentManager, "NewPlaylistDialog")
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
