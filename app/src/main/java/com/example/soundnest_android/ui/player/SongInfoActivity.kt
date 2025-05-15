package com.example.soundnest_android.ui.player

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.soundnest_android.R

class SongInfoActivity : AppCompatActivity() {
    private lateinit var infoSongImage: ImageView
    private lateinit var infoSongTitle: TextView
    private lateinit var infoArtistName: TextView

    private lateinit var tvCurrentTime: TextView
    private lateinit var tvTotalTime: TextView
    private lateinit var seekBar: SeekBar
    private lateinit var btnPlayPause: ImageButton

    private val player by lazy { PlayerManager.getPlayer() }
    private val handler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            val current = player.currentPosition
            seekBar.progress = current
            tvCurrentTime.text = formatTime(current)
            handler.postDelayed(this, 500)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_song_info)

        infoSongImage   = findViewById(R.id.infoSongImage)
        infoSongTitle   = findViewById(R.id.infoSongTitle)
        infoArtistName  = findViewById(R.id.infoArtistName)
        tvCurrentTime   = findViewById(R.id.tvCurrentTime)
        tvTotalTime     = findViewById(R.id.tvTotalTime)
        seekBar         = findViewById(R.id.seekBar)
        btnPlayPause    = findViewById(R.id.btnPlayPause)

        val title    = intent.getStringExtra("EXTRA_TITLE") ?: ""
        val artist   = intent.getStringExtra("EXTRA_ARTIST") ?: ""
        val imageRes = intent.getIntExtra("EXTRA_IMAGE_RES", R.drawable.img_default_song)

        infoSongTitle.text  = title
        infoArtistName.text = artist
        infoSongImage.setImageResource(imageRes)

        val duration = player.duration
        seekBar.max       = duration
        tvTotalTime.text  = formatTime(duration)

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(sb: SeekBar) {
                handler.removeCallbacks(updateRunnable)
            }
            override fun onStopTrackingTouch(sb: SeekBar) {
                val pos = sb.progress
                player.seekTo(pos)
                tvCurrentTime.text = formatTime(pos)
                handler.post(updateRunnable)
            }
            override fun onProgressChanged(sb: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    tvCurrentTime.text = formatTime(progress)
                }
            }
        })

        btnPlayPause.setOnClickListener {
            val playing = PlayerManager.togglePlayPause()
            btnPlayPause.setImageResource(
                if (playing) R.drawable.ic_baseline_pause else R.drawable.ic_baseline_play
            )
            if (playing) {
                handler.post(updateRunnable)
            } else {
                handler.removeCallbacks(updateRunnable)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateRunnable)
    }

    private fun formatTime(ms: Int): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%d:%02d".format(minutes, seconds)
    }
}

