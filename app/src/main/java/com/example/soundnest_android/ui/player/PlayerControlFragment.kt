package com.example.soundnest_android.ui.player

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.soundnest_android.R
import com.example.soundnest_android.business_logic.Song
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

class PlayerControlFragment : Fragment(R.layout.fragment_player_control),
    PlayerManager.PlayerStateListener {
    private val shared: SharedPlayerViewModel by activityViewModels()
    private lateinit var root: MaterialCardView
    private lateinit var songImage: ImageView
    private lateinit var songTitle: TextView
    private lateinit var artistName: TextView
    private lateinit var btnPlayPause: ImageButton
    private lateinit var progressLoading: ProgressBar
    private var currentSong: Song? = null
    private var currentFile: File? = null
    private var isFirstPlaybackAttemptInFragment: Boolean = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        root = view.findViewById(R.id.playerControl)
        songImage = view.findViewById(R.id.songImage)
        songTitle = view.findViewById(R.id.songTitle)
        artistName = view.findViewById(R.id.artistName)
        btnPlayPause = view.findViewById(R.id.btnPlayPause)
        progressLoading = view.findViewById(R.id.progress_loading)

        PlayerManager.playerStateListener = this

        shared.isLoading.observe(viewLifecycleOwner) { loading ->
            progressLoading.visibility = if (loading) View.VISIBLE else View.GONE
            btnPlayPause.isEnabled = !loading
            if (loading) {
                btnPlayPause.setImageResource(R.drawable.ic_baseline_play)
            }
        }

        shared.pendingFile.observe(viewLifecycleOwner) { (song, file) ->
            currentSong = song
            currentFile = file
            songTitle.text = song.title
            artistName.text = song.artist
            Glide.with(this).load(song.coverUrl).circleCrop().into(songImage)

            if (isFirstPlaybackAttemptInFragment) {
                Log.d("PlayerControlFragment", "First playback attempt, delaying...")
                viewLifecycleOwner.lifecycleScope.launch {
                    delay(500) // Delay for 500ms
                    shared.playFromFile(song, file)
                    PlayerManager.playFile(requireContext(), file)
                }
                isFirstPlaybackAttemptInFragment = false
            } else {
                PlayerManager.playFile(requireContext(), file)
            }
        }

        btnPlayPause.setOnClickListener {
            PlayerManager.togglePlayPause()
        }

        root.setOnClickListener {
            currentSong?.let { song ->
                val intent = Intent(requireContext(), SongInfoActivity::class.java).apply {
                    putExtra("EXTRA_TITLE", song.title)
                    putExtra("EXTRA_ARTIST", song.artist)
                    putExtra("EXTRA_COVER", song.coverUrl)
                    putExtra("EXTRA_SONG_OBJ", song)
                    currentFile?.absolutePath
                        ?.let { path -> putExtra("EXTRA_FILE_PATH", path) }
                }
                startActivity(intent)
            }
        }

        updatePlayPauseButton()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (PlayerManager.playerStateListener == this) {
            PlayerManager.playerStateListener = null
        }
    }


    override fun onTrackStarted() {
        Log.d("PlayerControlFragment", "onTrackStarted called")
        activity?.runOnUiThread {
            btnPlayPause.setImageResource(R.drawable.ic_baseline_pause)
            btnPlayPause.isEnabled = true
            progressLoading.visibility = View.GONE
        }
    }

    override fun onTrackPaused() {
        Log.d("PlayerControlFragment", "onTrackPaused called")
        activity?.runOnUiThread {
            btnPlayPause.setImageResource(R.drawable.ic_baseline_play)
        }
    }

    override fun onTrackResumed() {
        Log.d("PlayerControlFragment", "onTrackResumed called")
        activity?.runOnUiThread {
            btnPlayPause.setImageResource(R.drawable.ic_baseline_pause)
        }
    }

    override fun onTrackCompleted() {
        Log.d("PlayerControlFragment", "onTrackCompleted called")
        activity?.runOnUiThread {
            btnPlayPause.setImageResource(R.drawable.ic_baseline_play)
        }
    }

    override fun onError() {
        Log.e("PlayerControlFragment", "onError called from PlayerManager")
        activity?.runOnUiThread {
            btnPlayPause.setImageResource(R.drawable.ic_baseline_play)
            btnPlayPause.isEnabled = true
            progressLoading.visibility = View.GONE
        }
    }

    private fun updatePlayPauseButton() {
        if (PlayerManager.isPlaying()) {
            btnPlayPause.setImageResource(R.drawable.ic_baseline_pause)
        } else {
            btnPlayPause.setImageResource(R.drawable.ic_baseline_play)
        }
    }
}