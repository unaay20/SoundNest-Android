package com.example.soundnest_android.ui.songs

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.soundnest_android.R
import com.example.soundnest_android.auth.SharedPrefsTokenProvider
import com.example.soundnest_android.business_logic.Song
import com.example.soundnest_android.grpc.constants.GrpcRoutes
import com.example.soundnest_android.grpc.http.GrpcResult
import com.example.soundnest_android.grpc.services.SongFileGrpcService
import com.example.soundnest_android.restful.constants.RestfulRoutes
import com.example.soundnest_android.restful.services.SongService
import com.example.soundnest_android.restful.services.VisitService
import com.example.soundnest_android.restful.utils.ApiResult
import com.example.soundnest_android.ui.player.PlayerControlFragment
import com.example.soundnest_android.ui.player.PlayerManager
import com.example.soundnest_android.ui.player.SharedPlayerViewModel
import com.example.soundnest_android.ui.player.SongInfoActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class PlaylistDetailActivity : AppCompatActivity(), PlayerHost {

    private lateinit var songAdapter: SongAdapter
    private var playlistSongs: List<Song> = emptyList()
    private var lastFilePath: String? = null
    private var currentIdx: Int = 0

    private val sharedPlayer: SharedPlayerViewModel by viewModels()

    private val grpcService by lazy {
        SongFileGrpcService(
            GrpcRoutes.getHost(),
            GrpcRoutes.getPort()
        ) { SharedPrefsTokenProvider(this).getToken() }
    }

    private val songService by lazy {
        SongService(RestfulRoutes.getBaseUrl(), SharedPrefsTokenProvider(this))
    }

    private val visitService by lazy {
        VisitService(RestfulRoutes.getBaseUrl(), SharedPrefsTokenProvider(this))
    }

    private val songInfoLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data ?: return@registerForActivityResult
                val song = data.getSerializableExtra("EXTRA_PLAYING_SONG") as? Song
                val path = data.getStringExtra("EXTRA_PLAYING_PATH")
                val idx = data.getIntExtra("EXTRA_INDEX", -1)
                song?.let {
                    path?.let { p ->
                        val f = File(p)
                        if (f.exists()) sharedPlayer.playFromFile(it, f)
                    }
                }
                if (idx >= 0) sharedPlayer.setCurrentIndex(idx)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playlist_detail)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.player_fragment_container, PlayerControlFragment(), "PLAYER_CONTROL")
            .commit()

        intent.getSerializableExtra("EXTRA_PLAYING_SONG")?.let { serial ->
            val song = serial as Song
            intent.getStringExtra("EXTRA_PLAYING_PATH")?.let { path ->
                val file = File(path)
                if (file.exists()) {
                    sharedPlayer.playFromFile(song, file)
                }
            }
        }

        val rvSongs = findViewById<RecyclerView>(R.id.rvSongs)
        val tvEmpty = findViewById<TextView>(R.id.tvEmptySongs)
        val tvPlaylistName = findViewById<TextView>(R.id.tvPlaylistName)

        songAdapter = SongAdapter(
            showPlayIcon = false,
            onSongClick = { },
            isScrollingProvider = { false },
            isCompact = false
        )
        rvSongs.apply {
            layoutManager = LinearLayoutManager(this@PlaylistDetailActivity)
            adapter = songAdapter
        }

        tvPlaylistName.text =
            intent.getStringExtra("EXTRA_PLAYLIST_NAME") ?: "Playlist"

        val songIds = intent.getIntegerArrayListExtra("EXTRA_PLAYLIST_SONG_IDS")
            ?: arrayListOf()

        if (songIds.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            rvSongs.visibility = View.GONE
            return
        } else {
            tvEmpty.visibility = View.GONE
            rvSongs.visibility = View.VISIBLE
        }

        sharedPlayer.currentIndex.observe(this) { idx ->
            currentIdx = idx
        }

        val fabPlay = findViewById<AppCompatImageButton>(R.id.btn_play_playlist)
        fabPlay.setOnClickListener {
            playPlaylist()
        }

        sharedPlayer.pendingFile.observe(this) { (song, file) ->
            lastFilePath = file?.absolutePath
        }

        lifecycleScope.launch {
            when (val r = songService.getByIds(songIds)) {
                is ApiResult.Success -> {
                    val base = RestfulRoutes.getBaseUrl().removeSuffix("/")
                    val fullSongs = r.data.orEmpty().map { dto ->
                        Song(
                            id = dto.idSong,
                            title = dto.songName,
                            artist = dto.userName ?: "Desconocido",
                            coverUrl = dto.pathImageUrl?.let { "$base$it" }
                        )
                    }
                    playlistSongs = fullSongs
                    songAdapter.submitList(fullSongs)
                    sharedPlayer.setPlaylist(playlistSongs)

                    sharedPlayer.setPlaylist(playlistSongs)
                    fullSongs.firstOrNull()?.let { first ->
                        val f = File(cacheDir, "song_${first.id}.mp3")
                        if (f.exists()) {
                            sharedPlayer.playFromFile(first, f)
                            val frag = supportFragmentManager
                                .findFragmentByTag("PLAYER_CONTROL") as? PlayerControlFragment
                            frag?.skipNextPendingFile()
                        }
                    }
                }

                is ApiResult.HttpError -> toast("HTTP ${r.code}: ${r.message}")
                is ApiResult.NetworkError -> toast("Red: ${r.exception.message}")
                is ApiResult.UnknownError -> toast("Error: ${r.exception.message}")
            }
        }
    }

    private fun playPlaylist() {
        if (playlistSongs.isEmpty()) return

        sharedPlayer.setPlaylist(playlistSongs)

        downloadAndPlay(playlistSongs[0])

        lifecycleScope.launch(Dispatchers.IO) {
            for (i in 1..minOf(2, playlistSongs.lastIndex)) {
                val tmpFile = File(cacheDir, "song_${playlistSongs[i].id}.tmp")
                if (tmpFile.exists()) tmpFile.delete()

                val res = grpcService.downloadSongStreamTo(
                    playlistSongs[i].id,
                    tmpFile.outputStream()
                )

                if (res is GrpcResult.Success) {
                    val finalFile = File(cacheDir, "song_${playlistSongs[i].id}.mp3")
                    tmpFile.renameTo(finalFile)
                }
            }
        }

    }

    override fun playSong(song: Song) {
        lifecycleScope.launch {
            visitService.incrementVisit(song.id)
        }
        downloadAndPlay(song)
    }

    override fun playNext() {
        val list = playlistSongs
        val idx = sharedPlayer.currentIndex.value ?: return
        if (idx + 1 < list.size) {
            downloadAndPlay(list[idx + 1])
            sharedPlayer.setCurrentIndex(idx + 1)
        } else {
            PlayerManager.getPlayer()?.seekTo(PlayerManager.getPlayer()!!.duration)
        }
    }

    override fun playPrevious() {
        val list = playlistSongs
        val idx = sharedPlayer.currentIndex.value ?: return
        if (idx - 1 >= 0) {
            downloadAndPlay(list[idx - 1])
            sharedPlayer.setCurrentIndex(idx - 1)
        } else {
            PlayerManager.getPlayer()?.seekTo(0)
        }
    }


    private fun downloadAndPlay(song: Song) {
        val cacheFile = File(cacheDir, "song_${'$'}{song.id}.mp3")
        if (cacheFile.exists()) {
            sharedPlayer.playFromFile(song, cacheFile)
            sharedPlayer.setCurrentIndex(playlistSongs.indexOf(song))
            return
        }

        sharedPlayer.setLoading(true)
        lifecycleScope.launch(Dispatchers.IO) {
            val tmp = File(cacheDir, "song_${'$'}{song.id}.tmp").apply { if (exists()) delete() }
            when (val res = grpcService.downloadSongStreamTo(song.id, tmp.outputStream())) {
                is GrpcResult.Success -> tmp.renameTo(cacheFile)
                else -> tmp.delete()
            }
            withContext(Dispatchers.Main) {
                sharedPlayer.setLoading(false)
                sharedPlayer.playFromFile(song, cacheFile)
                sharedPlayer.setCurrentIndex(playlistSongs.indexOfFirst { it.id == song.id })

                val currentIndex = playlistSongs.indexOfFirst { it.id == song.id }
                val nextIndex = currentIndex + 3
                if (nextIndex < playlistSongs.size) {
                    preloadSong(playlistSongs[nextIndex])
                }
            }
        }
    }

    private fun preloadSong(song: Song) {
        val cacheFile = File(cacheDir, "song_${'$'}{song.id}.mp3")
        if (cacheFile.exists()) return
        lifecycleScope.launch(Dispatchers.IO) {
            val tmp = File(cacheDir, "song_${'$'}{song.id}.tmp").apply { if (exists()) delete() }
            when (val res = grpcService.downloadSongStreamTo(song.id, tmp.outputStream())) {
                is GrpcResult.Success -> tmp.renameTo(cacheFile)
                else -> tmp.delete()
            }
        }
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    override fun openSongInfo(
        song: Song,
        filePath: String?,
        playlist: List<Song>,
        index: Int
    ) {
        val intent = Intent(this, SongInfoActivity::class.java).apply {
            putExtra("EXTRA_SONG_OBJ", song)
            filePath?.let { putExtra("EXTRA_FILE_PATH", it) }
            putExtra("EXTRA_PLAYLIST", ArrayList(playlist) as java.io.Serializable)
            putExtra("EXTRA_INDEX", index)
        }
        songInfoLauncher.launch(intent)
    }

    private fun finishWithResult() {
        val currentIndex = sharedPlayer.currentIndex.value ?: 0
        val currentSong = playlistSongs.getOrNull(currentIndex)

        val data = Intent().apply {
            putExtra("EXTRA_PLAYLIST", ArrayList(playlistSongs))
            putExtra("EXTRA_INDEX", currentIndex)

            currentSong?.let { song ->
                val cacheFile = File(cacheDir, "song_${song.id}.mp3")
                if (cacheFile.exists()) {
                    putExtra("EXTRA_PLAYING_PATH", cacheFile.absolutePath)
                }
            }
        }
        setResult(RESULT_OK, data)
        finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishWithResult()
    }
}
