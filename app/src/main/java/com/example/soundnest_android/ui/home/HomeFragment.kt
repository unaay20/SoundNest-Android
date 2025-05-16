package com.example.soundnest_android.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.soundnest_android.R
import com.example.soundnest_android.auth.SharedPrefsTokenProvider
import com.example.soundnest_android.databinding.FragmentHomeBinding
import com.example.soundnest_android.network.ApiService
import com.example.soundnest_android.restful.constants.RestfulRoutes
import com.example.soundnest_android.restful.services.SongService
import com.example.soundnest_android.ui.notifications.NotificationsActivity
import com.example.soundnest_android.ui.songs.Song
import com.example.soundnest_android.ui.songs.SongAdapter
import com.example.soundnest_android.ui.upload_song.UploadSongActivity

class HomeFragment : Fragment(R.layout.fragment_home) {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels {
        HomeViewModelFactory(
            SongService(RestfulRoutes.getBaseUrl(), ApiService.tokenProvider),
            SharedPrefsTokenProvider(requireContext())
        )
    }
    private lateinit var tokenProvider: SharedPrefsTokenProvider

    private lateinit var popularAdapter: SongAdapter
    private lateinit var recentAdapter: SongAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentHomeBinding.bind(view)
        super.onViewCreated(view, savedInstanceState)

        tokenProvider = SharedPrefsTokenProvider(requireContext())
        val name = tokenProvider.username ?: getString(R.string.lbl_mandatory)
        binding.textHome.text = getString(R.string.hello_user, name)

        binding.btnNotifications.setOnClickListener {
            viewModel.onNotificationsClicked()
        }
        viewModel.navigateToNotifications.observe(viewLifecycleOwner) { navigate ->
            if (navigate) {
                startActivity(Intent(requireContext(), NotificationsActivity::class.java))
                viewModel.onNavigated()
            }
        }

        popularAdapter = SongAdapter { song ->
            // p.ej. reproducir o mostrar detalle
        }
        recentAdapter = SongAdapter { song ->
            // p.ej. reproducir o mostrar detalle
        }

        binding.rvPopular.adapter = popularAdapter
        binding.rvRecent.adapter  = recentAdapter

        viewModel.popular.observe(viewLifecycleOwner) { rawList ->
            val uiList = rawList.map { resp ->
                Song(
                    id        = resp.idSong,
                    title     = resp.songName.orEmpty(),
                    artist    = resp.userName.orEmpty(),
                    coverResId = R.drawable.img_default_song
                )
            }
            popularAdapter.submitList(uiList)
        }

        viewModel.recent.observe(viewLifecycleOwner) { rawList ->
            val uiList = rawList.map { resp ->
                Song(
                    id        = resp.idSong,
                    title     = resp.songName.orEmpty(),
                    artist    = resp.userName.orEmpty(),
                    coverResId = R.drawable.img_default_song
                )
            }
            recentAdapter.submitList(uiList)
        }



        viewModel.loadSongs()

        binding.fabAddSong.setOnClickListener {
            viewModel.onAddSongClicked()
        }
        viewModel.navigateToUploadSong.observe(viewLifecycleOwner) { navigate ->
            if (navigate) {
                startActivity(Intent(requireContext(), UploadSongActivity::class.java))
                viewModel.onAddSongNavigated()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
