package com.example.soundnest_android.ui.songs

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.soundnest_android.R
import com.example.soundnest_android.business_logic.Song
import com.example.soundnest_android.ui.comments.SongCommentsActivity

class PlaylistDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playlist_detail)

        val playlistName = intent.getStringExtra("EXTRA_PLAYLIST_NAME")
        findViewById<TextView>(R.id.tvPlaylistName).text = playlistName

        val songs = intent
            .getSerializableExtra("EXTRA_PLAYLIST_SONGS")
                as? ArrayList<Song>
            ?: arrayListOf()

        val rvSongs = findViewById<RecyclerView>(R.id.rvSongs)
        rvSongs.layoutManager = LinearLayoutManager(this)
        rvSongs.setHasFixedSize(true)

        val adapter = SongAdapter { song ->
            val intent = Intent(this, SongCommentsActivity::class.java)
                .putExtra("EXTRA_SONG_OBJ", song)
            startActivity(intent)
        }

        adapter.submitList(songs)

        rvSongs.adapter = adapter
    }
}
