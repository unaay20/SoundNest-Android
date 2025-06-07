package com.example.soundnest_android.ui.songs

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
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
import com.example.soundnest_android.restful.utils.ApiResult
import com.example.soundnest_android.ui.player.SharedPlayerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class PlaylistDetailActivity : AppCompatActivity(), PlayerHost {

    private lateinit var songAdapter: SongAdapter

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playlist_detail)

        songAdapter = SongAdapter(
            onSongClick = { song ->
                SongDialogFragment.newInstance(song)
                    .show(supportFragmentManager, "dlgSong")
            },
            isScrollingProvider = { false }
        )

        findViewById<RecyclerView>(R.id.rvSongs).apply {
            layoutManager = LinearLayoutManager(this@PlaylistDetailActivity)
            adapter = songAdapter
        }

        findViewById<TextView>(R.id.tvPlaylistName).text =
            intent.getStringExtra("EXTRA_PLAYLIST_NAME") ?: "Playlist"

        val songIds = intent.getIntegerArrayListExtra("EXTRA_PLAYLIST_SONG_IDS")
            ?: arrayListOf()
        Log.d("PlaylistDetail", "songIds recibidos: $songIds")

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
                    songAdapter.submitList(fullSongs)
                }

                is ApiResult.HttpError -> toast("HTTP ${r.code}: ${r.message}")
                is ApiResult.NetworkError -> toast("Red: ${r.exception.message}")
                is ApiResult.UnknownError -> toast("Error: ${r.exception.message}")
            }
        }
    }

    override fun playSong(song: Song) {
        downloadAndPlay(song)
    }

    private fun downloadAndPlay(song: Song) {
        val cacheFile = File(cacheDir, "song_${song.id}.mp3")
        if (cacheFile.exists()) {
            sharedPlayer.playFromFile(song, cacheFile)
            return
        }

        sharedPlayer.setLoading(true)
        lifecycleScope.launch(Dispatchers.IO) {
            val tmp = File(cacheDir, "song_${song.id}.tmp").also { if (it.exists()) it.delete() }
            when (val res = grpcService.downloadSongStreamTo(song.id, tmp.outputStream())) {
                is GrpcResult.Success -> tmp.renameTo(cacheFile)
                else -> tmp.delete()
            }
            withContext(Dispatchers.Main) {
                sharedPlayer.setLoading(false)
                sharedPlayer.playFromFile(song, cacheFile)
            }
        }
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }
}
