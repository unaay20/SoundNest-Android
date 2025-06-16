package com.example.soundnest_android.ui.search

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.soundnest_android.R
import com.example.soundnest_android.auth.SharedPrefsTokenProvider
import com.example.soundnest_android.business_logic.Song
import com.example.soundnest_android.databinding.ActivitySearchResultBinding
import com.example.soundnest_android.grpc.constants.GrpcRoutes
import com.example.soundnest_android.grpc.http.GrpcResult
import com.example.soundnest_android.grpc.services.SongFileGrpcService
import com.example.soundnest_android.restful.constants.RestfulRoutes
import com.example.soundnest_android.restful.models.song.GetSongDetailResponse
import com.example.soundnest_android.restful.services.SongService
import com.example.soundnest_android.restful.services.VisitService
import com.example.soundnest_android.restful.utils.ApiResult
import com.example.soundnest_android.ui.player.PlayerManager
import com.example.soundnest_android.ui.player.SharedPlayerViewModel
import com.example.soundnest_android.ui.songs.PlayerHost
import com.example.soundnest_android.ui.songs.SongAdapter
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class SearchResultActivity : AppCompatActivity(), PlayerHost {
    private lateinit var binding: ActivitySearchResultBinding
    private val sharedPlayer: SharedPlayerViewModel by viewModels()

    private lateinit var sharedPrefs: SharedPrefsTokenProvider
    private lateinit var role: String
    private lateinit var username: String

    private val songGrpc by lazy {
        SongFileGrpcService(
            GrpcRoutes.getHost(),
            GrpcRoutes.getPort()
        ) { SharedPrefsTokenProvider(this).getToken() }
    }

    private val visitService by lazy {
        VisitService(
            RestfulRoutes.getBaseUrl(),
            SharedPrefsTokenProvider(this)
        )
    }

    private val songService by lazy {
        SongService(
            RestfulRoutes.getBaseUrl(),
            SharedPrefsTokenProvider(this)
        )
    }
    private lateinit var adapter: SongAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPrefs = SharedPrefsTokenProvider(this)
        role = sharedPrefs.role
        username = sharedPrefs.username.toString()

        adapter = SongAdapter(
            showPlayIcon = true,
            onSongClick = { song -> playSong(song) },
            onItemDelete = { song -> confirmDelete(song) },
            isScrollingProvider = { false },
            isCompact = false,
            currentRole = role,
            currentUsername = username
        )

        binding.rvResults.apply {
            layoutManager = LinearLayoutManager(this@SearchResultActivity)
            adapter = this@SearchResultActivity.adapter
        }

        val query = intent.getStringExtra("QUERY")?.takeIf { it.isNotBlank() }
        val filterSong = intent.getStringExtra("FILTER_SONG")?.takeIf { it.isNotBlank() }
        val filterArtist = intent.getStringExtra("FILTER_ARTIST")?.takeIf { it.isNotBlank() }
        val filterGenre = intent.getIntExtra("FILTER_GENRE", -1).takeIf { it >= 0 }
        if (query == null && filterSong == null && filterArtist == null && filterGenre == null) {
            Toast.makeText(this, "Introduce texto o selecciona un filtro", Toast.LENGTH_SHORT)
                .show()
            finish()
            return
        }

        val parts = mutableListOf<String>().apply {
            query?.let { add(it) }
            filterSong?.let { add(it) }
            filterArtist?.let { add("Artista: $it") }
            filterGenre?.let { add("Género ID: $it") }
        }
        supportActionBar?.title =
            if (parts.isEmpty()) "Resultados" else "Resultados para: \"${parts.joinToString(", ")}\""

        binding.progress.visibility = View.VISIBLE
        binding.tvEmpty.visibility = View.GONE
        binding.rvResults.visibility = View.GONE

        lifecycleScope.launch {
            val songNameParam = listOfNotNull(query, filterSong).joinToString(" ")
            when (val res = songService.search(
                songName = songNameParam.ifBlank { null },
                artistName = filterArtist,
                genreId = filterGenre,
                limit = 20,
                offset = 0
            )) {
                is ApiResult.Success -> {
                    val list = res.data.orEmpty().mapNotNull {
                        it.toBusinessSong(RestfulRoutes.getBaseUrl())
                    }
                    if (list.isEmpty()) {
                        binding.tvEmpty.visibility = View.VISIBLE
                    } else {
                        adapter.submitList(list)
                        binding.rvResults.visibility = View.VISIBLE
                    }
                }

                is ApiResult.HttpError -> showToast("Error HTTP ${res.code}")
                is ApiResult.NetworkError -> showToast("Error de red: ${res.exception?.message}")
                is ApiResult.UnknownError -> showToast("Error: ${res.exception?.message}")
            }
            binding.progress.visibility = View.GONE
        }
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    override fun playSong(song: Song) {
        lifecycleScope.launch { visitService.incrementVisit(song.id) }
        showLoading()
        downloadAndQueue(song) { cacheFile ->
            sharedPlayer.playFromFile(song, cacheFile)
            val data = Intent().apply {
                putExtra("EXTRA_SONG_OBJ", song)
                putExtra("EXTRA_FILE_PATH", cacheFile.absolutePath)
            }
            setResult(RESULT_OK, data)
            hideLoading()
            finish()
        }
    }

    private fun downloadAndQueue(song: Song, onReady: (File) -> Unit) {
        val cacheFile = File(cacheDir, "song_${song.id}.mp3")
        if (cacheFile.exists()) return onReady(cacheFile)
        sharedPlayer.setLoading(true)
        lifecycleScope.launch(Dispatchers.IO) {
            val tmp = File(cacheDir, "song_${song.id}.tmp")
            if (tmp.exists()) tmp.delete()
            when (val res = songGrpc.downloadSongStreamTo(song.id, tmp.outputStream())) {
                is GrpcResult.Success -> tmp.renameTo(cacheFile)
                else -> tmp.delete()
            }
            withContext(Dispatchers.Main) {
                sharedPlayer.setLoading(false)
                onReady(cacheFile)
            }
        }
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

    override fun openSongInfo(song: Song, filePath: String?, playlist: List<Song>, index: Int) {
        TODO("Not yet implemented")
    }

    private fun confirmDelete(s: Song) {
        AlertDialog.Builder(this)
            .setTitle(R.string.hint_delete_song)
            .setMessage("Delete song “${s.title}”?")
            .setPositiveButton(R.string.btn_delete) { _, _ ->
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        songService.deleteSong(s.id.toInt())
                    }
                    Snackbar.make(binding.root, "Song deleted", Snackbar.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(R.string.btn_cancel, null)
            .show()
    }

    private fun showLoading() {
        binding.blockingOverlay.visibility = View.VISIBLE
        binding.fullscreenLoader.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.blockingOverlay.visibility = View.GONE
        binding.fullscreenLoader.visibility = View.GONE
    }
}

private fun GetSongDetailResponse.toBusinessSong(baseUrl: String): Song? {
    val artist = userName ?: return null
    val cover = pathImageUrl?.let { url -> baseUrl.removeSuffix("/") + url }
    return Song(idSong, songName.orEmpty(), artist, cover)
}
