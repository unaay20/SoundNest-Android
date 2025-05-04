package com.example.soundnest_android.ui.songs

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.soundnest_android.R

class PlaylistDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playlist_detail)

        val songs = intent.getSerializableExtra("EXTRA_PLAYLIST_SONGS")
                as? ArrayList<Song> ?: arrayListOf()

        val rvSongs: RecyclerView = findViewById(R.id.rvSongs)
        rvSongs.layoutManager = LinearLayoutManager(this)
        rvSongs.setHasFixedSize(true)

        rvSongs.adapter = SongAdapter(songs)
    }
}