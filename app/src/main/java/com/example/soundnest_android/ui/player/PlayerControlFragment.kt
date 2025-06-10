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
import com.example.soundnest_android.ui.songs.PlayerHost
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
    private lateinit var btnNext: ImageButton
    private lateinit var btnPrevious: ImageButton
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
        btnPrevious = view.findViewById(R.id.btnBack)
        btnNext = view.findViewById(R.id.btnNext)

        PlayerManager.playerStateListener = this

        shared.isLoading.observe(viewLifecycleOwner) { loading ->
            progressLoading.visibility = if (loading) View.VISIBLE else View.GONE
            btnPlayPause.isEnabled = !loading
            if (loading) {
                btnPlayPause.setImageResource(R.drawable.ic_baseline_play)
            }
        }

        shared.isPlayingLive.observe(viewLifecycleOwner) { playing ->
            btnPlayPause.setImageResource(
                if (playing) R.drawable.ic_baseline_pause else R.drawable.ic_baseline_play
            )
        }

        shared.currentIndex.observe(viewLifecycleOwner) { idx ->
            shared.playlist.value?.getOrNull(idx)?.let { song ->
                val file = File(requireContext().cacheDir, "song_${song.id}.mp3")
                shared.playFromFile(song, file)
                if (file.exists()) {
                    PlayerManager.playFile(requireContext(), file)
                }
            }
        }

        shared.pendingFile.observe(viewLifecycleOwner) { (song, file) ->
            currentSong = song
            currentFile = file
            songTitle.text = song.title
            artistName.text = song.artist
            Glide.with(this)
                .load(song.coverUrl)
                .placeholder(R.drawable.img_soundnest_logo_svg)
                .error(R.drawable.img_soundnest_logo_svg)
                .circleCrop()
                .into(songImage)


            if (isFirstPlaybackAttemptInFragment) {
                Log.d("PlayerControlFragment", "First playback attempt, delaying...")
                viewLifecycleOwner.lifecycleScope.launch {
                    delay(500)
                    shared.playFromFile(song, file)
                    PlayerManager.playFile(requireContext(), file)
                }
                isFirstPlaybackAttemptInFragment = false
            } else {
                PlayerManager.playFile(requireContext(), file)
            }
        }

        btnPlayPause.setOnClickListener {
            val playing = PlayerManager.togglePlayPause()
            shared.setIsPlaying(playing)
        }


        btnNext.setOnClickListener {
            (activity as? PlayerHost)?.playNext()
        }

        btnPrevious.setOnClickListener {
            (activity as? PlayerHost)?.playPrevious()
        }



        root.setOnClickListener {
            currentSong?.let { song ->
                val intent = Intent(requireContext(), SongInfoActivity::class.java).apply {
                    putExtra("EXTRA_SONG_OBJ", song)
                    currentFile?.absolutePath
                        ?.let { path -> putExtra("EXTRA_FILE_PATH", path) }

                    val playlist = ArrayList(shared.playlist.value ?: emptyList<Song>())
                    putExtra("EXTRA_PLAYLIST", playlist as java.io.Serializable)
                    putExtra("EXTRA_INDEX", shared.currentIndex.value ?: 0)
                }
                startActivity(intent)
                requireActivity().overridePendingTransition(
                    R.anim.slide_in_up_fadein,
                    R.anim.fade_none
                )
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