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
    private val service by lazy {
        SongService(RestfulRoutes.getBaseUrl(), SharedPrefsTokenProvider(this))
    }
    private lateinit var adapter: SongAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = SongAdapter(
            onSongClick = { song ->
                Toast.makeText(this, "Clicked: ${song.title}", Toast.LENGTH_SHORT).show()
            },
            isScrollingProvider = { false }
        )
        binding.rvResults.apply {
            layoutManager = LinearLayoutManager(this@SearchResultActivity)
            adapter = this@SearchResultActivity.adapter
        }

        val query = intent.getStringExtra("QUERY") ?: ""
        title = "Results for: \"$query\""
        fetchSongs(query)
    }

    private fun fetchSongs(query: String) {
        binding.progress.visibility = View.VISIBLE
        binding.tvEmpty.visibility = View.GONE
        binding.rvResults.visibility = View.GONE

        lifecycleScope.launch {
            when (val result = service.search(songName = query)) {
                is ApiResult.Success -> {
                    val list = result.data
                        ?.mapNotNull { it.toBusinessSong() }
                        .orEmpty()

                    if (list.isEmpty()) {
                        // Sin resultados
                        binding.tvEmpty.visibility = View.VISIBLE
                    } else {
                        // Mostrar lista
                        adapter.submitList(list)
                        binding.rvResults.visibility = View.VISIBLE
                    }
                }

                is ApiResult.HttpError -> {
                    Toast.makeText(
                        this@SearchResultActivity,
                        "Error communicating with server", Toast.LENGTH_SHORT
                    ).show()
                }

                is ApiResult.NetworkError -> {
                    Toast.makeText(
                        this@SearchResultActivity,
                        "Verify your internet connection", Toast.LENGTH_SHORT
                    ).show()
                }

                is ApiResult.UnknownError -> {
                    Toast.makeText(
                        this@SearchResultActivity,
                        "An unknown error occurred", Toast.LENGTH_SHORT
                    ).show()
                }
            }
            binding.progress.visibility = View.GONE
        }
    }

    private fun GetSongDetailResponse.toBusinessSong(): Song? {
        return this.userName?.let {
            Song(
                id = this.idSong,
                title = this.songName.orEmpty(),
                artist = it,
                coverUrl = this.pathImageUrl
            )
        }
    }
}
