package com.example.soundnest_android.ui.player

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
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
    private var isPlayerPrepared = false
    private var isUserSeeking = false
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
    private lateinit var btnRewind10: ImageButton
    private lateinit var btnForward10: ImageButton

    private lateinit var gestureDetector: GestureDetectorCompat

    private val sharedPlayer: SharedPlayerViewModel by viewModels()

    private val player by lazy { PlayerManager.getPlayer() }
    private val handler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            try {
                if (!isUserSeeking && isPlayerPrepared && player?.isPlaying == true) {
                    val pos = player!!.currentPosition
                    seekBar.progress = pos
                    tvCurrentTime.text = formatTime(pos)
                    handler.postDelayed(this, 100)
                } else {
                    handler.postDelayed(this, 500)
                }
            } catch (_: IllegalStateException) {
                handler.postDelayed(this, 500)
            }
        }
    }

    private val pickDirLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { treeUri ->
        treeUri?.let { saveToDirectory(it) }
    }

    private lateinit var song: Song
    private var localFilePath: String? = null
    private var currentSong: Song? = null
    private var isInPlaylistMode = false

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

        initializeViews()

        player?.apply {
            setOnPreparedListener { mp ->
                onTrackStarted()
            }
        }

        setupObservers()
        setupClickListeners()
        handler.removeCallbacks(updateRunnable)
        setupSeekBar()
        setupGestureDetector()

        resetPlayerUI()

        @Suppress("UNCHECKED_CAST")
        val list = intent
            .getSerializableExtra("EXTRA_PLAYLIST") as? ArrayList<Song>

        if (list != null) {
            sharedPlayer.setPlaylist(list)
            val idx = intent.getIntExtra("EXTRA_INDEX", 0)
            sharedPlayer.setCurrentIndex(idx)
            isInPlaylistMode = list.size > 1
        } else {
            isInPlaylistMode = false
        }

        sharedPlayer.playlist.value?.let { list ->
            isInPlaylistMode = list.size > 1
        }
        updateButtonVisibility()

        val initialSong = intent.getSerializableExtra("EXTRA_SONG_OBJ") as Song
        updateUI(initialSong)
    }

    private fun resetPlayerUI() {
        handler.removeCallbacks(updateRunnable)
        isPlayerPrepared = false
        seekBar.progress = 0
        tvCurrentTime.text = formatTime(0)
        tvTotalTime.text = formatTime(0)
        updatePlayPauseButton(false)
    }


    private fun initializeViews() {
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
        btnRewind10 = findViewById(R.id.btnRewind10)
        btnForward10 = findViewById(R.id.btnForward10)
    }

    private fun setupObservers() {
        sharedPlayer.pendingFile.observe(this) { (newSong, file) ->
            currentSong = newSong
            updateUI(newSong)

            player?.apply {
                setOnPreparedListener { mp ->
                    onTrackStarted()
                }
            }

            file?.let {
                PlayerManager.playFile(this, it)
                sharedPlayer.setIsPlaying(true)
            }
        }

        sharedPlayer.currentIndex.observe(this) { idx ->
            sharedPlayer.playlist.value?.getOrNull(idx)?.let { s ->
                updateUI(s)
                song = s
            }
        }

        sharedPlayer.isPlayingLive.observe(this) { playing ->
            updatePlayPauseButton(playing)
        }
    }

    private fun setupClickListeners() {
        btnRewind10.setOnClickListener {
            if (!isPlayerPrepared) return@setOnClickListener

            val mp = player ?: return@setOnClickListener

            try {
                val current = mp.currentPosition
                val newPos = (current - 10_000).coerceAtLeast(0)

                val duration = mp.duration
                if (newPos > duration) return@setOnClickListener

                mp.seekTo(newPos)
                seekBar.progress = newPos
                tvCurrentTime.text = formatTime(newPos)

            } catch (e: IllegalStateException) {
                Log.w("SongInfoActivity", "Seek ignorado, player no preparado", e)
            } catch (e: Exception) {
                Log.e("SongInfoActivity", "Error al retroceder 10s", e)
            }
        }


        btnForward10.setOnClickListener {
            if (!isPlayerPrepared) return@setOnClickListener

            val mp = player ?: return@setOnClickListener

            try {
                val current = mp.currentPosition
                val duration = mp.duration

                val newPos = (current + 10_000).coerceAtMost(duration)

                mp.seekTo(newPos)
                seekBar.progress = newPos
                tvCurrentTime.text = formatTime(newPos)

            } catch (e: IllegalStateException) {
                Log.w("SongInfoActivity", "Seek ignorado, player no preparado", e)
            } catch (e: Exception) {
                Log.e("SongInfoActivity", "Error al avanzar 10s", e)
            }
        }

        btnPrevious.setOnClickListener {
            if (isInPlaylistMode) {
                playPrevious()
            }
        }

        btnPlayPause.setOnClickListener {
            val playing = PlayerManager.togglePlayPause()
            sharedPlayer.setIsPlaying(playing)
            updatePlayPauseButton(playing)
            if (playing) handler.post(updateRunnable) else handler.removeCallbacks(updateRunnable)
        }

        btnNext.setOnClickListener {
            if (isInPlaylistMode) {
                playNext()
            }
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
    }

    private fun setupSeekBar() {
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(sb: SeekBar) {
                isUserSeeking = true
            }

            override fun onStopTrackingTouch(sb: SeekBar) {
                isUserSeeking = false
                if (isPlayerPrepared) {
                    try {
                        player?.seekTo(sb.progress)
                        tvCurrentTime.text = formatTime(sb.progress)
                    } catch (_: IllegalStateException) {
                        // ignoro si no está listo
                    }
                }
            }

            override fun onProgressChanged(sb: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) tvCurrentTime.text = formatTime(progress)
            }
        })
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun setupGestureDetector() {
        gestureDetector = GestureDetectorCompat(this,
            object : GestureDetector.SimpleOnGestureListener() {
                private val SWIPE_THRESHOLD = 100
                private val SWIPE_VELOCITY_THRESHOLD = 100

                override fun onFling(
                    e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float
                ): Boolean {
                    if (e1 != null &&
                        kotlin.math.abs(e2.y - e1.y) > SWIPE_THRESHOLD &&
                        kotlin.math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD &&
                        e2.y - e1.y > 0
                    ) {
                        finishAfterTransition()
                        overridePendingTransition(0, R.anim.slide_out_down)
                        return true
                    }
                    return false
                }
            })
    }

    private fun updateUI(newSong: Song) {
        seekBar.progress = 0
        tvCurrentTime.text = formatTime(0)
        infoSongTitle.text = newSong.title
        infoArtistName.text = newSong.artist

        val options = RequestOptions()
            .placeholder(R.drawable.img_soundnest_logo_svg)
            .error(R.drawable.img_soundnest_logo_svg)

        Glide.with(this)
            .load(newSong.coverUrl)
            .apply(options)
            .into(imgBackground)

        Glide.with(this)
            .load(newSong.coverUrl)
            .apply(options.centerCrop())
            .into(infoSongImage)

        try {
            player?.duration?.let { duration ->
                seekBar.max = duration
                tvTotalTime.text = formatTime(duration)
            }
        } catch (e: IllegalStateException) {
            // MediaPlayer aún no preparado, lo ignoramos
        }
    }

    private fun updateButtonVisibility() {
        if (isInPlaylistMode) {
            btnRewind10.visibility = View.GONE
            btnForward10.visibility = View.GONE
            btnPrevious.visibility = View.VISIBLE
            btnNext.visibility = View.VISIBLE
        } else {
            btnRewind10.visibility = View.VISIBLE
            btnForward10.visibility = View.VISIBLE
            btnPrevious.visibility = View.GONE
            btnNext.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        PlayerManager.playerStateListener = this
        updatePlayPauseButton(PlayerManager.isPlaying())

        player?.let { mp ->
            try {
                isPlayerPrepared = true

                val dur = mp.duration
                if (dur > 0) {
                    seekBar.max = dur
                    tvTotalTime.text = formatTime(dur)
                }
                val pos = mp.currentPosition
                seekBar.progress = pos
                tvCurrentTime.text = formatTime(pos)

                seekBar.isEnabled = true

                handler.post(updateRunnable)
            } catch (_: IllegalStateException) {
            }
        }

        isInPlaylistMode = (sharedPlayer.playlist.value?.size ?: 0) > 1
        updateButtonVisibility()
    }


    override fun onPause() {
        super.onPause()
        PlayerManager.playerStateListener = null
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateRunnable)
    }

    override fun onTrackStarted() = runOnUiThread {
        isPlayerPrepared = true
        seekBar.isEnabled = true
        //seekBar.progress = 0
        tvCurrentTime.text = formatTime(0)
        initDuration()
        handler.post(updateRunnable)
    }

    private fun initDuration() {
        handler.postDelayed({
            try {
                val dur = player?.duration ?: 0
                if (dur > 0) {
                    seekBar.max = dur
                    tvTotalTime.text = formatTime(dur)
                } else {
                    initDuration()
                }
            } catch (_: IllegalStateException) {
                initDuration()
            }
        }, 100)
    }

    override fun onTrackResumed() = runOnUiThread {
        handler.post(updateRunnable)
    }

    override fun onTrackPaused() = runOnUiThread {
        handler.removeCallbacks(updateRunnable)
    }

    override fun onTrackCompleted() = runOnUiThread {
        handler.removeCallbacks(updateRunnable)

        if (isInPlaylistMode) {
            playNext()
        } else {
            player?.seekTo(0)
            seekBar.progress = 0
            tvCurrentTime.text = formatTime(0)
            seekBar.isEnabled = true
            handler.postDelayed(updateRunnable, 500)
        }
    }


    override fun onError() = runOnUiThread {
        handler.removeCallbacks(updateRunnable)
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
                            "Añadido a ${playlist.name}",
                            Toast.LENGTH_SHORT
                        ).show()
                        playlistViewModel.loadPlaylists()
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
    }

    override fun playSong(song: Song) {
        downloadAndPlay(song)
    }

    override fun playNext() {
        sharedPlayer.playlist.value?.let { list ->
            val idx = sharedPlayer.currentIndex.value ?: return
            if (idx + 1 < list.size) {
                val next = list[idx + 1]
                sharedPlayer.setCurrentIndex(idx + 1)
                runOnUiThread {
                    Toast.makeText(
                        this, "Playing next: “${next.title}”", Toast.LENGTH_SHORT
                    ).show()
                }
                resetPlayerUI()
                downloadAndPlay(next)
            }
        }
    }

    override fun playPrevious() {
        sharedPlayer.playlist.value?.let { list ->
            val idx = sharedPlayer.currentIndex.value ?: return
            if (idx - 1 >= 0) {
                val prev = list[idx - 1]
                sharedPlayer.setCurrentIndex(idx - 1)
                resetPlayerUI()
                downloadAndPlay(prev)
            }
        }
    }

    private fun playFromCacheFile(song: Song, cacheFile: File) {
        resetPlayerUI()
        this.song = song
        player?.setOnPreparedListener { onTrackStarted() }
        PlayerManager.playFile(this, cacheFile)
        sharedPlayer.setIsPlaying(true)
    }

    private fun downloadAndPlay(song: Song) {
        val cacheFile = File(cacheDir, "song_${song.id}.mp3")
        if (cacheFile.exists()) {
            playFromCacheFile(song, cacheFile)
            return
        }

        sharedPlayer.setLoading(true)
        lifecycleScope.launch(Dispatchers.IO) {
            val tmp = File(cacheDir, "song_${song.id}.tmp").apply { delete() }
            val res = grpcService.downloadSongStreamTo(song.id, tmp.outputStream())
            if (res is GrpcResult.Success) tmp.renameTo(cacheFile) else tmp.delete()

            withContext(Dispatchers.Main) {
                sharedPlayer.setLoading(false)
                playFromCacheFile(song, cacheFile)

                player?.apply {
                    setOnPreparedListener { mp ->
                        onTrackStarted()
                    }
                }

                PlayerManager.playFile(this@SongInfoActivity, cacheFile)
                sharedPlayer.setIsPlaying(true)

                isPlayerPrepared = true
                seekBar.isEnabled = true
                initDuration()
                handler.post(updateRunnable)


                val newIdx = sharedPlayer.playlist.value
                    ?.indexOfFirst { it.id == song.id } ?: -1
                if (newIdx >= 0) sharedPlayer.setCurrentIndex(newIdx)
            }
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (gestureDetector.onTouchEvent(ev)) return true
        return super.dispatchTouchEvent(ev)
    }
}