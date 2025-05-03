package com.example.soundnest_android.ui.player

import android.os.Bundle
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
    private lateinit var sizeSeekBar: SeekBar
    private lateinit var btnPlayPause: ImageButton
    val player = PlayerManager.getPlayer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_song_info)

        btnPlayPause = findViewById(R.id.btnPlayPause)
        infoSongImage  = findViewById(R.id.infoSongImage)
        infoSongTitle  = findViewById(R.id.infoSongTitle)
        infoArtistName = findViewById(R.id.infoArtistName)
        sizeSeekBar    = findViewById(R.id.sizeSeekBar)

        // Extras que recibimos
        val title     = intent.getStringExtra("EXTRA_TITLE") ?: ""
        val artist    = intent.getStringExtra("EXTRA_ARTIST") ?: ""
        val imageRes  = intent.getIntExtra("EXTRA_IMAGE_RES", R.drawable.img_default_song)

        // Inicializar datos
        infoSongTitle.text  = title
        infoArtistName.text = artist
        infoSongImage.setImageResource(imageRes)

        // Configurar SeekBar para escalar la imagen
        sizeSeekBar.max = 120        // 120% del tamaño original
        sizeSeekBar.progress = 100   // inicia al 100%

        sizeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val scale = progress / 100f
                infoSongImage.scaleX = scale
                infoSongImage.scaleY = scale
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        btnPlayPause.setOnClickListener {
            val isPlaying = PlayerManager.togglePlayPause()
            btnPlayPause.setImageResource(
                if (isPlaying) R.drawable.ic_baseline_pause else R.drawable.ic_baseline_play
            )
        }
    }
}
