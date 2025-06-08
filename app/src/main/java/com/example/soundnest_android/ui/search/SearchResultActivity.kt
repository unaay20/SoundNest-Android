package com.example.soundnest_android.ui.search

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.soundnest_android.auth.SharedPrefsTokenProvider
import com.example.soundnest_android.business_logic.Song
import com.example.soundnest_android.databinding.ActivitySearchResultBinding
import com.example.soundnest_android.restful.constants.RestfulRoutes
import com.example.soundnest_android.restful.models.song.GetSongDetailResponse
import com.example.soundnest_android.restful.services.SongService
import com.example.soundnest_android.restful.utils.ApiResult
import com.example.soundnest_android.ui.songs.SongAdapter
import kotlinx.coroutines.launch

class SearchResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchResultBinding
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

        adapter = SongAdapter(
            onSongClick = { song -> Toast.makeText(this, song.title, Toast.LENGTH_SHORT).show() },
            isScrollingProvider = { false }
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

        // Llamada a la API
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
}

private fun GetSongDetailResponse.toBusinessSong(baseUrl: String): Song? {
    val artist = userName ?: return null
    val cover = pathImageUrl?.let { url -> baseUrl.removeSuffix("/") + url }
    return Song(idSong, songName.orEmpty(), artist, cover)
}
