package com.example.soundnest_android.ui.home

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.soundnest_android.MainActivity
import com.example.soundnest_android.R
import com.example.soundnest_android.auth.SharedPrefsTokenProvider
import com.example.soundnest_android.business_logic.Song
import com.example.soundnest_android.databinding.FragmentHomeBinding
import com.example.soundnest_android.grpc.constants.GrpcRoutes
import com.example.soundnest_android.grpc.http.GrpcResult
import com.example.soundnest_android.grpc.services.SongFileGrpcService
import com.example.soundnest_android.restful.constants.RestfulRoutes
import com.example.soundnest_android.restful.services.SongService
import com.example.soundnest_android.restful.services.VisitService
import com.example.soundnest_android.ui.notifications.NotificationsActivity
import com.example.soundnest_android.ui.player.PlayerManager
import com.example.soundnest_android.ui.player.SharedPlayerViewModel
import com.example.soundnest_android.ui.songs.PlayerHost
import com.example.soundnest_android.ui.songs.SongAdapter
import com.example.soundnest_android.ui.songs.SongDialogFragment
import com.example.soundnest_android.ui.upload_song.UploadSongActivity
import com.example.soundnest_android.utils.GridSpacingItemDecoration
import com.example.soundnest_android.utils.SingleLiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class HomeFragment : Fragment(R.layout.fragment_home), PlayerHost {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val sharedPlayer: SharedPlayerViewModel by activityViewModels()
    private var isFirstSongEverPlayed: Boolean = true

    @RequiresApi(Build.VERSION_CODES.O)
    private val uploadLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.loadSongs()
        }
    }

    private val songGrpc by lazy {
        SongFileGrpcService(
            GrpcRoutes.getHost(),
            GrpcRoutes.getPort()
        ) { SharedPrefsTokenProvider(requireContext()).getToken() }
    }
    private val songService: SongService by lazy {
        SongService(
            RestfulRoutes.getBaseUrl(),
            SharedPrefsTokenProvider(requireContext())
        )
    }
    private val visitService by lazy {
        VisitService(RestfulRoutes.getBaseUrl(), SharedPrefsTokenProvider(requireContext()))
    }
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
        val name =
            SharedPrefsTokenProvider(requireContext()).username ?: getString(R.string.lbl_mandatory)
        binding.textHome.text = getString(R.string.hello_user, name)
        binding.btnNotifications.setOnClickListener {
            startActivity(Intent(requireContext(), NotificationsActivity::class.java))
        }

        var isScrollingPopular = false
        var isScrollingRecent = false

        popularAdapter = SongAdapter(
            showPlayIcon = true,
            onSongClick = { song -> showSongFragment(song) },
            isScrollingProvider = { isScrollingPopular },
            isCompact = true
        )
        recentAdapter = SongAdapter(
            showPlayIcon = true,
            onSongClick = { song -> showSongFragment(song) },
            isScrollingProvider = { isScrollingRecent },
            isCompact = true
        )

        binding.rvPopular.apply {
            layoutManager = GridLayoutManager(requireContext(), 2, RecyclerView.VERTICAL, false)
            adapter = popularAdapter

            val spacing = resources.getDimensionPixelSize(R.dimen.grid_spacing)
            addItemDecoration(GridSpacingItemDecoration(2, spacing, true))
        }
        binding.rvRecent.apply {
            layoutManager = GridLayoutManager(requireContext(), 2, RecyclerView.VERTICAL, false)
            adapter = recentAdapter

            val spacing = resources.getDimensionPixelSize(R.dimen.grid_spacing)
            addItemDecoration(GridSpacingItemDecoration(2, spacing, true))
        }
        viewModel.popular.observe(viewLifecycleOwner) { rawList ->
            val baseUrl = RestfulRoutes.getBaseUrl().removeSuffix("/")
            val uiList = rawList.map { resp ->
                Song(
                    id = resp.idSong,
                    title = resp.songName,
                    artist = resp.userName.orEmpty(),
                    coverUrl = resp.pathImageUrl?.let { "$baseUrl$it" }
                )
            }
            popularAdapter.submitList(uiList)
        }
        viewModel.recent.observe(viewLifecycleOwner) { rawList ->
            val baseUrl = RestfulRoutes.getBaseUrl().removeSuffix("/")
            val uiList = rawList.map { resp ->
                Song(
                    id = resp.idSong,
                    title = resp.songName.orEmpty(),
                    artist = resp.userName.orEmpty(),
                    coverUrl = resp.pathImageUrl?.let { "$baseUrl$it" }
                )
            }
            recentAdapter.submitList(uiList)
        }
        viewModel.error.observe(viewLifecycleOwner) { msg ->
            SingleLiveEvent<String>().apply {
                value = msg
                observe(viewLifecycleOwner) { text ->
                    Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.rvPopular.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                isScrollingPopular = newState != RecyclerView.SCROLL_STATE_IDLE
            }
        })

        binding.rvRecent.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                isScrollingRecent = newState != RecyclerView.SCROLL_STATE_IDLE
            }
        })

        binding.fabAddSong.setOnClickListener {
            val intent = Intent(requireContext(), UploadSongActivity::class.java)
            uploadLauncher.launch(intent)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        viewModel.loadSongs()
    }

    private fun showSongFragment(song: Song) {
        SongDialogFragment.newInstance(song)
            .show(childFragmentManager, "dlgSong")
    }

    override fun playSong(song: Song) {
        lifecycleScope.launch {
            visitService.incrementVisit(song.id)
        }
        downloadAndQueue(song)
    }

    override fun playNext() {
        PlayerManager.getPlayer()?.apply {
            pause()
            seekTo(duration)
            seekTo(0)
            start()
        }
    }

    override fun playPrevious() {
        PlayerManager.getPlayer()?.apply {
            pause()
            seekTo(0)
            start()
        }
    }

    override fun openSongInfo(
        song: Song,
        filePath: String?,
        playlist: List<Song>,
        index: Int
    ) {
        (requireActivity() as? MainActivity)
            ?.openPlaylistDetail(
                playlist = playlist,
                playingSong = song,
                playingPath = filePath,
                playingIndex = index
            )
    }

    private fun downloadAndQueue(song: Song) {
        val cacheFile = File(requireContext().cacheDir, "song_${song.id}.mp3")
        if (cacheFile.exists()) {
            sharedPlayer.playFromFile(song, cacheFile)
            return
        }
        sharedPlayer.setLoading(true)
        lifecycleScope.launch(Dispatchers.IO) {
            val tmpFile = File(requireContext().cacheDir, "song_${song.id}.tmp")
            if (tmpFile.exists()) tmpFile.delete()
            when (val res = songGrpc.downloadSongStreamTo(song.id, tmpFile.outputStream())) {
                is GrpcResult.Success -> tmpFile.renameTo(cacheFile)
                else -> tmpFile.delete()
            }
            withContext(Dispatchers.Main) {
                sharedPlayer.setLoading(false)
                if (isFirstSongEverPlayed) {
                    Log.d(
                        "HomeFragment",
                        "Attempting to play FIRST song directly: ${cacheFile.name}"
                    )
                    sharedPlayer.playFromFile(song, cacheFile)
                    isFirstSongEverPlayed = false
                } else {
                    sharedPlayer.playFromFile(song, cacheFile)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
