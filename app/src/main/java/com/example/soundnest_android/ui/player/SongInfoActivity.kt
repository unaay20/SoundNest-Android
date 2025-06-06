package com.example.soundnest_android.ui.player

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.soundnest_android.R
import com.example.soundnest_android.business_logic.Song
import com.example.soundnest_android.ui.comments.SongCommentsActivity
import java.io.File

class SongInfoActivity : AppCompatActivity(), PlayerManager.PlayerStateListener {
    private lateinit var imgBackground: ImageView
    private lateinit var infoSongImage: ImageView
    private lateinit var infoSongTitle: TextView
    private lateinit var infoArtistName: TextView
    private lateinit var tvCurrentTime: TextView
    private lateinit var tvTotalTime: TextView
    private lateinit var seekBar: SeekBar
    private lateinit var btnPlayPause: ImageButton
    private lateinit var btnDownload: ImageButton
    private lateinit var btnComments: ImageButton

    private val player by lazy { PlayerManager.getPlayer() }
    private val handler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            player?.currentPosition?.let { current ->
                seekBar.progress = current
                tvCurrentTime.text = formatTime(current)
            }
            handler.postDelayed(this, 500)
        }
    }

    private val pickDirLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { treeUri ->
        treeUri?.let { saveToDirectory(it) }
    }

    private lateinit var song: Song
    private var localFilePath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_song_info)

        song = intent.getSerializableExtra("EXTRA_SONG_OBJ") as? Song
            ?: throw IllegalArgumentException("Se esperaba EXTRA_SONG_OBJ")
        localFilePath = intent.getStringExtra("EXTRA_FILE_PATH")

        imgBackground = findViewById(R.id.img_background_blur)
        infoSongImage = findViewById(R.id.infoSongImage)
        infoSongTitle = findViewById(R.id.infoSongTitle)
        infoArtistName = findViewById(R.id.infoArtistName)
        tvCurrentTime = findViewById(R.id.tvCurrentTime)
        tvTotalTime = findViewById(R.id.tvTotalTime)
        seekBar = findViewById(R.id.seekBar)
        btnPlayPause = findViewById(R.id.btnPlayPause)
        btnDownload = findViewById(R.id.btnDownload)
        btnComments = findViewById(R.id.btnComments)

        infoSongTitle.text = song.title
        infoArtistName.text = song.artist

        Glide.with(this).load(song.coverUrl).into(imgBackground)
        Glide.with(this).load(song.coverUrl).centerCrop().into(infoSongImage)

        player?.duration?.let { duration ->
            seekBar.max = duration
            tvTotalTime.text = formatTime(duration)
        }

        handler.post(updateRunnable)

        val options = RequestOptions()
            .placeholder(R.drawable.img_soundnest_logo_svg)
            .error(R.drawable.img_soundnest_logo_svg)
        Glide.with(this)
            .load(song.coverUrl)
            .apply(options)
            .into(imgBackground)
        Glide.with(this)
            .load(song.coverUrl)
            .apply(options.centerCrop())
            .into(infoSongImage)

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(sb: SeekBar) {
                handler.removeCallbacks(updateRunnable)
            }

            override fun onStopTrackingTouch(sb: SeekBar) {
                player?.seekTo(sb.progress)
                handler.post(updateRunnable)
            }

            override fun onProgressChanged(sb: SeekBar, p: Int, fromUser: Boolean) {
                if (fromUser) tvCurrentTime.text = formatTime(p)
            }
        })

        btnPlayPause.setOnClickListener {
            val playing = PlayerManager.togglePlayPause()
            updatePlayPauseButton(playing)
            if (playing) handler.post(updateRunnable) else handler.removeCallbacks(updateRunnable)
        }

        btnDownload.setOnClickListener { pickDirLauncher.launch(null) }

        btnComments.setOnClickListener {
            startActivity(Intent(this, SongCommentsActivity::class.java).apply {
                putExtra("EXTRA_SONG_OBJ", song)
            })
        }
    }

    override fun onResume() {
        super.onResume()
        PlayerManager.playerStateListener = this
        updatePlayPauseButton(PlayerManager.isPlaying())
    }

    override fun onPause() {
        super.onPause()
        if (PlayerManager.playerStateListener == this) {
            PlayerManager.playerStateListener = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateRunnable)
    }

    override fun onTrackStarted() = runOnUiThread { updatePlayPauseButton(true) }
    override fun onTrackPaused() = runOnUiThread { updatePlayPauseButton(false) }
    override fun onTrackResumed() = runOnUiThread { updatePlayPauseButton(true) }
    override fun onTrackCompleted() = runOnUiThread { updatePlayPauseButton(false) }
    override fun onError() = runOnUiThread { updatePlayPauseButton(false) }

    private fun updatePlayPauseButton(isPlaying: Boolean) {
        btnPlayPause.setImageResource(
            if (isPlaying) R.drawable.ic_baseline_pause else R.drawable.ic_baseline_play
        )
    }

    private fun saveToDirectory(treeUri: Uri) {
        contentResolver.takePersistableUriPermission(
            treeUri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )
        val docTree = DocumentFile.fromTreeUri(this, treeUri)
        val filename = "${song.title}.mp3"
        docTree?.findFile(filename)?.delete()
        val newFile = docTree?.createFile("audio/mpeg", filename)
        if (newFile != null) {
            try {
                contentResolver.openOutputStream(newFile.uri).use { out ->
                    File(localFilePath!!).inputStream().use { input -> input.copyTo(out!!) }
                }
                Toast.makeText(this, "Saved in ${newFile.uri}", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Error downloading: ${e.message}", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(this, "Impossible to create file.", Toast.LENGTH_LONG).show()
        }
    }

    private fun formatTime(ms: Int): String {
        val secs = ms / 1000
        return "%d:%02d".format(secs / 60, secs % 60)
    }
}
