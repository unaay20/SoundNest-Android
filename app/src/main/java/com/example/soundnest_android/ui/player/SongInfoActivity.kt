package com.example.soundnest_android.ui.player

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.LinearInterpolator
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GestureDetectorCompat
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.soundnest_android.R
import com.example.soundnest_android.auth.SharedPrefsTokenProvider
import com.example.soundnest_android.business_logic.Song
import com.example.soundnest_android.grpc.constants.GrpcRoutes
import com.example.soundnest_android.grpc.http.GrpcResult
import com.example.soundnest_android.grpc.services.SongFileGrpcService
import com.example.soundnest_android.restful.constants.RestfulRoutes
import com.example.soundnest_android.ui.comments.SongCommentsActivity
import com.example.soundnest_android.ui.playlists.PlaylistPopupAdapter
import com.example.soundnest_android.ui.playlists.PlaylistsViewModel
import com.example.soundnest_android.ui.playlists.PlaylistsViewModelFactory
import com.example.soundnest_android.ui.songs.PlayerHost
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class SongInfoActivity : AppCompatActivity(), PlayerManager.PlayerStateListener, PlayerHost {
    private val grpcService by lazy {
        SongFileGrpcService(
            GrpcRoutes.getHost(),
            GrpcRoutes.getPort()
        ) { SharedPrefsTokenProvider(this).getToken() }
    }
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
    private lateinit var btnAddPlaylist: ImageButton
    private lateinit var btnNext: ImageButton
    private lateinit var btnPrevious: ImageButton

    private lateinit var gestureDetector: GestureDetectorCompat

    private val sharedPlayer: SharedPlayerViewModel by viewModels()

    private val player by lazy { PlayerManager.getPlayer() }
    private val handler = Handler(Looper.getMainLooper())
    private var lastProgress = 0
    private val updateRunnable = object : Runnable {
        override fun run() {
            player?.currentPosition?.let { current ->
                ObjectAnimator.ofInt(seekBar, "progress", lastProgress, current).apply {
                    duration = 500L
                    interpolator = LinearInterpolator()
                    start()
                }
                lastProgress = current

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

    private val playlistViewModel: PlaylistsViewModel by viewModels {
        PlaylistsViewModelFactory(
            application = application,
            baseUrl = RestfulRoutes.getBaseUrl(),
            tokenProvider = SharedPrefsTokenProvider(this),
            userId = SharedPrefsTokenProvider(this).getUserId().toString()
        )
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_song_info)

        overridePendingTransition(R.anim.slide_in_up, 0)

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
        btnAddPlaylist = findViewById(R.id.btnAddPlaylist)
        btnPrevious = findViewById(R.id.btnBack)
        btnNext = findViewById(R.id.btnNext)

        infoSongTitle.text = song.title
        infoArtistName.text = song.artist

        Glide.with(this).load(song.coverUrl).into(imgBackground)
        Glide.with(this).load(song.coverUrl).centerCrop().into(infoSongImage)

        player?.duration?.let { duration ->
            seekBar.max = duration
            tvTotalTime.text = formatTime(duration)
        }

        sharedPlayer.pendingFile.observe(this) { (song, file) ->
            infoSongTitle.text = song.title
            infoArtistName.text = song.artist
            Glide.with(this)
                .load(song.coverUrl)
                .placeholder(R.drawable.img_soundnest_logo_svg)
                .circleCrop()
                .into(infoSongImage)

            PlayerManager.playFile(this, file)
        }

        handler.post(updateRunnable)

        btnPrevious.setOnClickListener {
            playPrevious()
            handler.post(updateRunnable)
        }

        btnNext.setOnClickListener {
            playNext()
            handler.post(updateRunnable)
        }

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
            sharedPlayer.setIsPlaying(playing)
            updatePlayPauseButton(playing)
            if (playing) handler.post(updateRunnable) else handler.removeCallbacks(updateRunnable)
        }

        btnDownload.setOnClickListener { pickDirLauncher.launch(null) }

        btnComments.setOnClickListener {
            startActivity(Intent(this, SongCommentsActivity::class.java).apply {
                putExtra("EXTRA_SONG_OBJ", song)
            })
        }

        btnAddPlaylist.setOnClickListener {
            showPlaylistPopup(it as View)
        }

        gestureDetector = GestureDetectorCompat(this,
            object : GestureDetector.SimpleOnGestureListener() {
                private val SWIPE_THRESHOLD = 100
                private val SWIPE_VELOCITY_THRESHOLD = 100

                override fun onFling(
                    e1: MotionEvent?,
                    e2: MotionEvent,
                    velocityX: Float,
                    velocityY: Float
                ): Boolean {
                    if (e1 == null || e2 == null)
                        return super.onFling(e1, e2, velocityX, velocityY)

                    val diffY = e2.y - e1.y
                    val diffX = e2.x - e1.x
                    if (kotlin.math.abs(diffY) > kotlin.math.abs(diffX) &&
                        kotlin.math.abs(diffY) > SWIPE_THRESHOLD &&
                        kotlin.math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD &&
                        diffY > 0
                    ) {
                        finishAfterTransition()
                        overridePendingTransition(0, R.anim.slide_out_down)
                        return true
                    }
                    return super.onFling(e1, e2, velocityX, velocityY)
                }
            }
        )

        findViewById<View>(android.R.id.content).setOnTouchListener { _, ev ->
            gestureDetector.onTouchEvent(ev)
            true
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

    override fun onTrackStarted() = runOnUiThread {
        sharedPlayer.setIsPlaying(true)
        updatePlayPauseButton(true)
    }

    override fun onTrackPaused() = runOnUiThread {
        sharedPlayer.setIsPlaying(false)
        updatePlayPauseButton(false)
    }

    override fun onTrackResumed() = runOnUiThread {
        sharedPlayer.setIsPlaying(true)
        updatePlayPauseButton(true)
    }

    override fun onTrackCompleted() = runOnUiThread {
        sharedPlayer.setIsPlaying(false)
        updatePlayPauseButton(false)
    }

    override fun onError() = runOnUiThread {
        sharedPlayer.setIsPlaying(false)
        updatePlayPauseButton(false)
    }


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

    private fun showPlaylistPopup(anchor: View) {
        val popupView = layoutInflater.inflate(R.layout.fragment_popup_playlists, null)
        val recycler = popupView.findViewById<RecyclerView>(R.id.rvPlaylistPopup)
        recycler.layoutManager = LinearLayoutManager(this)

        val popupWindow = PopupWindow(
            popupView,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            setBackgroundDrawable(
                ContextCompat.getDrawable(
                    anchor.context,
                    R.drawable.popup_background
                )
            )
            elevation = 16f
        }

        popupWindow.showAsDropDown(anchor, 0, 0)

        playlistViewModel.playlists.observe(this) { list ->
            recycler.adapter = PlaylistPopupAdapter(list) { playlist ->
                lifecycleScope.launch {
                    try {
                        playlistViewModel.addSongToPlaylist(playlist.id, song.id)
                        Toast.makeText(
                            this@SongInfoActivity,
                            "Añadido a “${playlist.name}”",
                            Toast.LENGTH_SHORT
                        ).show()
                    } catch (e: Exception) {
                        Toast.makeText(
                            this@SongInfoActivity,
                            "Error: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    popupWindow.dismiss()
                }
            }
        }
        popupWindow.showAsDropDown(anchor, 0, 0)
    }

    override fun playSong(song: Song) {
        downloadAndPlay(song)
    }

    override fun playNext() {
        val list = sharedPlayer.playlist.value ?: return finishSong()
        val idx = sharedPlayer.currentIndex.value ?: return
        if (idx + 1 < list.size) {
            val next = list[idx + 1]
            sharedPlayer.setCurrentIndex(idx + 1)
            downloadAndPlay(next)
        } else {
            finishSong()
        }
    }

    override fun playPrevious() {
        val list = sharedPlayer.playlist.value ?: return restartSong()
        val idx = sharedPlayer.currentIndex.value ?: return
        if (idx - 1 >= 0) {
            val prev = list[idx - 1]
            sharedPlayer.setCurrentIndex(idx - 1)
            downloadAndPlay(prev)
        } else {
            restartSong()
        }
    }

    private fun downloadAndPlay(song: Song) {
        val cacheFile = File(cacheDir, "song_${song.id}.mp3")
        if (cacheFile.exists()) {
            sharedPlayer.playFromFile(song, cacheFile)
            return
        }
        sharedPlayer.setLoading(true)
        lifecycleScope.launch(Dispatchers.IO) {
            val tmp = File(cacheDir, "song_${song.id}.tmp").apply { if (exists()) delete() }
            when (val res = grpcService.downloadSongStreamTo(song.id, tmp.outputStream())) {
                is GrpcResult.Success -> tmp.renameTo(cacheFile)
                else -> tmp.delete()
            }
            withContext(Dispatchers.Main) {
                sharedPlayer.setLoading(false)
                sharedPlayer.playFromFile(song, cacheFile)
                val newIdx = sharedPlayer.playlist.value!!.indexOfFirst { it.id == song.id }
                sharedPlayer.setCurrentIndex(newIdx)
            }
        }
    }

    private fun finishSong() {
        PlayerManager.getPlayer()?.seekTo(PlayerManager.getPlayer()!!.duration)
    }

    private fun restartSong() {
        PlayerManager.getPlayer()?.seekTo(0)
    }

}