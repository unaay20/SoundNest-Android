package com.example.soundnest_android.ui.home

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.soundnest_android.R
import com.example.soundnest_android.auth.SharedPrefsTokenProvider
import com.example.soundnest_android.databinding.FragmentHomeBinding
import com.example.soundnest_android.restful.constants.RestfulRoutes
import com.example.soundnest_android.restful.services.SongService
import com.example.soundnest_android.ui.notifications.NotificationsActivity
import com.example.soundnest_android.business_logic.Song
import com.example.soundnest_android.ui.songs.SongAdapter
import com.example.soundnest_android.ui.upload_song.UploadSongActivity
import com.example.soundnest_android.utils.SingleLiveEvent

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels {
        HomeViewModelFactory(
            SongService(
                RestfulRoutes.getBaseUrl(),
                SharedPrefsTokenProvider(requireContext())
            ),
            SharedPrefsTokenProvider(requireContext())
        )
    }

    private lateinit var popularAdapter: SongAdapter
    private lateinit var recentAdapter: SongAdapter

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentHomeBinding.bind(view)
        super.onViewCreated(view, savedInstanceState)

        val tokenProvider = SharedPrefsTokenProvider(requireContext())
        val name = tokenProvider.username ?: getString(R.string.lbl_mandatory)
        binding.textHome.text = getString(R.string.hello_user, name)

        binding.btnNotifications.setOnClickListener {
            startActivity(Intent(requireContext(), NotificationsActivity::class.java))
        }

        popularAdapter = SongAdapter { song ->
            // TODO: manejar click en canción popular
        }
        recentAdapter = SongAdapter { song ->
            // TODO: manejar click en canción reciente
        }

        binding.rvPopular.apply {
            adapter = popularAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            isNestedScrollingEnabled = false
        }
        binding.rvRecent.apply {
            adapter = recentAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            isNestedScrollingEnabled = false
        }

        viewModel.popular.observe(viewLifecycleOwner) { rawList ->
            val baseUrl = RestfulRoutes.getBaseUrl().removeSuffix("/")
            val uiList = rawList.map { resp ->
                Song(
                    id       = resp.idSong,
                    title    = resp.songName.orEmpty(),
                    artist   = resp.userName.orEmpty(),
                    coverUrl = resp.pathImageUrl
                        ?.let { "$baseUrl$it" }
                )
            }
            popularAdapter.submitList(uiList)
        }
        viewModel.recent.observe(viewLifecycleOwner) { rawList ->
            val baseUrl = RestfulRoutes.getBaseUrl().removeSuffix("/")
            val uiList = rawList.map { resp ->
                Song(
                    id       = resp.idSong,
                    title    = resp.songName.orEmpty(),
                    artist   = resp.userName.orEmpty(),
                    coverUrl = resp.pathImageUrl
                        ?.let { "$baseUrl$it" }
                )
            }
            recentAdapter.submitList(uiList)
        }

        viewModel.error.observe(viewLifecycleOwner) { msg ->
            SingleLiveEvent<String>().apply {
                value = msg
                observe(viewLifecycleOwner) { text ->
                    android.widget.Toast.makeText(requireContext(), text, android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.fabAddSong.setOnClickListener {
            startActivity(Intent(requireContext(), UploadSongActivity::class.java))
        }

        viewModel.loadSongs()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
