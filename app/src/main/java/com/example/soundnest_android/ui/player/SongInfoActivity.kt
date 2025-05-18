package com.example.soundnest_android.ui.player

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.example.soundnest_android.R

class SongInfoActivity : AppCompatActivity() {
    private lateinit var imgBackground: ImageView
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

        imgBackground    = findViewById(R.id.img_background_blur)
        infoSongImage    = findViewById(R.id.infoSongImage)
        infoSongTitle    = findViewById(R.id.infoSongTitle)
        infoArtistName   = findViewById(R.id.infoArtistName)
        tvCurrentTime    = findViewById(R.id.tvCurrentTime)
        tvTotalTime      = findViewById(R.id.tvTotalTime)
        seekBar          = findViewById(R.id.seekBar)
        btnPlayPause     = findViewById(R.id.btnPlayPause)

        val title   = intent.getStringExtra("EXTRA_TITLE").orEmpty()
        val artist  = intent.getStringExtra("EXTRA_ARTIST").orEmpty()
        val cover   = intent.getStringExtra("EXTRA_COVER").orEmpty()

        infoSongTitle.text  = title
        infoArtistName.text = artist

        Glide.with(this)
            .load(cover)
            .into(imgBackground)

        Glide.with(this)
            .load(cover)
            .centerCrop()
            .into(infoSongImage)

        val duration = player.duration
        seekBar.max      = duration
        tvTotalTime.text = formatTime(duration)
        handler.post(updateRunnable)

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(sb: SeekBar) {
                handler.removeCallbacks(updateRunnable)
            }
            override fun onStopTrackingTouch(sb: SeekBar) {
                player.seekTo(sb.progress)
                handler.post(updateRunnable)
            }
            override fun onProgressChanged(sb: SeekBar, p: Int, fromUser: Boolean) {
                if (fromUser) tvCurrentTime.text = formatTime(p)
            }
        })

        btnPlayPause.setOnClickListener {
            val playing = PlayerManager.togglePlayPause()
            btnPlayPause.setImageResource(
                if (playing) R.drawable.ic_baseline_pause
                else R.drawable.ic_baseline_play
            )
            if (playing) handler.post(updateRunnable)
            else handler.removeCallbacks(updateRunnable)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateRunnable)
    }

    private fun formatTime(ms: Int): String {
        val secs = ms / 1000
        return "%d:%02d".format(secs / 60, secs % 60)
    }
}
