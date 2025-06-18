package com.example.soundnest_android.ui.playlists

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.soundnest_android.R
import com.example.soundnest_android.auth.SharedPrefsTokenProvider
import com.example.soundnest_android.business_logic.Playlist
import com.example.soundnest_android.databinding.FragmentPlaylistsBinding
import com.example.soundnest_android.restful.constants.RestfulRoutes
import com.example.soundnest_android.ui.edit_playlist.EditPlaylistDialogFragment
import com.example.soundnest_android.ui.player.SharedPlayerViewModel
import com.example.soundnest_android.ui.songs.PlaylistDetailActivity
import com.example.soundnest_android.utils.SkeletonAdapter
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class PlaylistsFragment : Fragment(R.layout.fragment_playlists) {

    private var _binding: FragmentPlaylistsBinding? = null
    private val binding get() = _binding!!

    private val sharedPrefs by lazy { SharedPrefsTokenProvider(requireContext()) }
    private val userIdString get() = sharedPrefs.getUserId().toString()
    private val sharedPlayer: SharedPlayerViewModel by activityViewModels()

    private val viewModel: PlaylistsViewModel by viewModels {
        PlaylistsViewModelFactory(
            application = requireActivity().application,
            baseUrl = RestfulRoutes.getBaseUrl(),
            tokenProvider = sharedPrefs,
            userId = userIdString
        )
    }

    private lateinit var adapter: PlaylistAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentPlaylistsBinding.bind(view)

        val shimmerLayout = binding.shimmerContainer
        val rvSkeletons = binding.rvSkeletons.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = SkeletonAdapter(count = 4)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            if (loading) {
                shimmerLayout.startShimmer()
                shimmerLayout.visibility = View.VISIBLE
                binding.rvPlaylists.visibility = View.GONE
            } else {
                shimmerLayout.stopShimmer()
                shimmerLayout.visibility = View.GONE
                binding.rvPlaylists.visibility = View.VISIBLE
            }
        }

        adapter = PlaylistAdapter(
            items = emptyList(),
            onItemClick = { p -> openDetail(p) },
            onItemEdit = { p -> showEditDialog(p) },
            onItemDelete = { p -> confirmDelete(p) }
        )

        binding.rvPlaylists.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = this@PlaylistsFragment.adapter
        }

        viewModel.playlists.observe(viewLifecycleOwner) { playlists ->
            adapter.setItems(playlists)
            binding.tvNoPlaylistsHint.visibility =
                if (playlists.isEmpty()) View.VISIBLE else View.GONE
            binding.imgArrowCreatePlaylist.visibility = binding.tvNoPlaylistsHint.visibility
        }

        viewModel.error.observe(viewLifecycleOwner) { errMsg ->
            errMsg?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }

        binding.fabAddPlaylist.setOnClickListener {
            NewPlaylistDialogFragment().apply {
                onPlaylistCreated = { name, desc, uriStr ->
                    lifecycleScope.launch {
                        try {
                            // Validación previa
                            if (name.isBlank()) {
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.msg_name_required),
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@launch
                            }

                            if (desc.isBlank()) {
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.msg_description_required),
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@launch
                            }

                            if (uriStr.isNullOrBlank()) {
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.msg_image_required),
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@launch
                            }

                            viewModel.createPlaylist(name, desc, uriStr)

                        } catch (e: Exception) {
                            Log.e("PlaylistFragment", "Error creating playlist", e)
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.msg_error_creating_playlist, e.message ?: ""),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }.show(parentFragmentManager, "NewPlaylistDialog")
        }
    }


    private fun openDetail(p: Playlist) {
        val songIds = ArrayList(p.songs.map { it.id })
        val intent = Intent(requireContext(), PlaylistDetailActivity::class.java).apply {
            putExtra("EXTRA_PLAYLIST_ID", p.id)
            putExtra("EXTRA_PLAYLIST_NAME", p.name)
            putExtra("EXTRA_PLAYLIST_IMAGE", p.imageUri)
            putIntegerArrayListExtra("EXTRA_PLAYLIST_SONG_IDS", songIds)
            sharedPlayer.pendingFile.value?.let { (song, file) ->
                putExtra("EXTRA_PLAYING_SONG", song)
                putExtra("EXTRA_PLAYING_PATH", file.absolutePath)
            }
        }
        startActivity(intent)
    }

    private fun showEditDialog(p: Playlist) {
        EditPlaylistDialogFragment.newInstance(p.id, p.name, p.description)
            .apply {
                onPlaylistEdited = { id, newName, newDesc ->
                    viewModel.editPlaylist(id, newName, newDesc)
                }
            }
            .show(parentFragmentManager, "EditPlaylistDialog")
    }

    private fun confirmDelete(p: Playlist) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.dialog_title_confirm_delete))
            .setMessage(getString(R.string.dialog_msg_confirm_delete, p.name))
            .setPositiveButton(getString(R.string.action_delete)) { _, _ ->
                viewModel.deletePlaylist(p)
                Snackbar.make(binding.root, getString(R.string.msg_playlist_deleted), Snackbar.LENGTH_SHORT).show()
            }
            .setNegativeButton(getString(R.string.action_cancel), null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadPlaylists()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
