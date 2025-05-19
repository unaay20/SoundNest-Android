package com.example.soundnest_android.ui.player

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.example.soundnest_android.R
import com.example.soundnest_android.business_logic.Song
import com.example.soundnest_android.ui.player.SharedPlayerViewModel
import com.google.android.material.card.MaterialCardView
import java.io.File

class PlayerControlFragment : Fragment(R.layout.fragment_player_control) {

    private val shared: SharedPlayerViewModel by activityViewModels()

    private lateinit var root: MaterialCardView
    private lateinit var songImage: ImageView
    private lateinit var songTitle: TextView
    private lateinit var artistName: TextView
    private lateinit var btnPlayPause: ImageButton
    private lateinit var progressLoading: ProgressBar

    private var currentSong: Song? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        root            = view.findViewById(R.id.playerControl)
        songImage       = view.findViewById(R.id.songImage)
        songTitle       = view.findViewById(R.id.songTitle)
        artistName      = view.findViewById(R.id.artistName)
        btnPlayPause    = view.findViewById(R.id.btnPlayPause)
        progressLoading = view.findViewById(R.id.progress_loading)

        shared.isLoading.observe(viewLifecycleOwner) { loading ->
            progressLoading.visibility = if (loading) View.VISIBLE else View.GONE
            btnPlayPause.isEnabled     = !loading
        }

        shared.pending.observe(viewLifecycleOwner) { (song, data) ->
            currentSong = song

            val tmpFile = File(requireContext().cacheDir, "song_${song.id}.mp3")
            tmpFile.writeBytes(data)

            PlayerManager.initFromPath(tmpFile.absolutePath)

            songTitle.text  = song.title
            artistName.text = song.artist
            Glide.with(this)
                .load(song.coverUrl)
                .circleCrop()
                .into(songImage)

            val playing = PlayerManager.togglePlayPause()
            btnPlayPause.setImageResource(
                if (playing) R.drawable.ic_baseline_pause
                else R.drawable.ic_baseline_play
            )
        }

        btnPlayPause.setOnClickListener {
            val playing = PlayerManager.togglePlayPause()
            btnPlayPause.setImageResource(
                if (playing) R.drawable.ic_baseline_pause
                else R.drawable.ic_baseline_play
            )
        }

        root.setOnClickListener {
            currentSong?.let { song ->
                val intent = Intent(requireContext(), SongInfoActivity::class.java).apply {
                    putExtra("EXTRA_TITLE",  song.title)
                    putExtra("EXTRA_ARTIST", song.artist)
                    putExtra("EXTRA_COVER",  song.coverUrl)
                }
                startActivity(intent)
            }
        }
    }
}
