package com.example.soundnest_android.ui.songs

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.soundnest_android.R
import com.example.soundnest_android.auth.SharedPrefsTokenProvider
import com.example.soundnest_android.business_logic.Song
import com.example.soundnest_android.restful.constants.RestfulRoutes
import com.example.soundnest_android.restful.services.SongService
import com.example.soundnest_android.restful.utils.ApiResult
import com.example.soundnest_android.restful.utils.TokenProvider
import com.example.soundnest_android.ui.comments.SongCommentsActivity
import kotlinx.coroutines.launch


class PlaylistDetailActivity : AppCompatActivity() {

    private lateinit var adapter: SongAdapter
    private val songService by lazy {
        SongService(RestfulRoutes.getBaseUrl(), SharedPrefsTokenProvider(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playlist_detail)

        val rvSongs = findViewById<RecyclerView>(R.id.rvSongs).apply {
            layoutManager = LinearLayoutManager(this@PlaylistDetailActivity)
            adapter = SongAdapter { song ->
                startActivity(
                    Intent(this@PlaylistDetailActivity, SongCommentsActivity::class.java)
                        .putExtra("EXTRA_SONG_OBJ", song)
                )
            }.also { adapter = it }
        }

        findViewById<TextView>(R.id.tvPlaylistName).text =
            intent.getStringExtra("EXTRA_PLAYLIST_NAME") ?: "Playlist"

        val songIds = intent.getIntegerArrayListExtra("EXTRA_PLAYLIST_SONG_IDS")
            ?: arrayListOf()

        lifecycleScope.launch {
            when (val r = songService.getByIds(songIds)) {
                is ApiResult.Success<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    val dtoList = (r.data as? List<com.example.soundnest_android.restful.models.song.GetSongDetailResponse>)
                        .orEmpty()

                    val fullSongs = dtoList.map { dto ->
                        dto.userName?.let {
                            Song(
                                id       = dto.idSong,
                                title    = dto.songName,
                                artist   = it,
                                coverUrl = dto.pathImageUrl
                            )
                        }
                    }
                    adapter.submitList(fullSongs)
                }

                is ApiResult.HttpError -> {
                    Toast.makeText(
                        this@PlaylistDetailActivity,
                        "HTTP ${r.code}: ${r.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }

                is ApiResult.NetworkError -> {
                    Toast.makeText(
                        this@PlaylistDetailActivity,
                        "Error de red: ${r.exception.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }

                is ApiResult.UnknownError -> {
                    Toast.makeText(
                        this@PlaylistDetailActivity,
                        "Error inesperado: ${r.exception.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}
