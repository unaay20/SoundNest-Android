package com.example.soundnest_android

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.example.soundnest_android.auth.SharedPrefsTokenProvider
import com.example.soundnest_android.business_logic.Song
import com.example.soundnest_android.network.ApiService
import com.example.soundnest_android.restful.constants.RestfulRoutes
import com.example.soundnest_android.restful.services.AuthService
import com.example.soundnest_android.restful.utils.ApiResult
import com.example.soundnest_android.ui.ViewPagerAdapter
import com.example.soundnest_android.ui.player.PlayerControlFragment
import com.example.soundnest_android.ui.player.PlayerManager
import com.example.soundnest_android.ui.player.SharedPlayerViewModel
import com.example.soundnest_android.ui.player.SongInfoActivity
import com.example.soundnest_android.ui.songs.PlayerHost
import com.example.soundnest_android.ui.songs.PlaylistDetailActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : AppCompatActivity(), PlayerHost {
    private lateinit var viewPager: ViewPager2
    private lateinit var navView: BottomNavigationView
    private val sharedPlayer: SharedPlayerViewModel by viewModels()
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("FCM", "Permiso de notificaciones concedido")
        } else {
            Log.w("FCM", "Permiso de notificaciones denegado")
        }
    }

    private val playlistDetailLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data ?: return@registerForActivityResult

                @Suppress("UNCHECKED_CAST")
                val returnedPlaylist =
                    data.getSerializableExtra("EXTRA_PLAYLIST") as? ArrayList<Song>
                val returnedIdx = data.getIntExtra("EXTRA_INDEX", 0)
                val returnedPath = data.getStringExtra("EXTRA_PLAYING_PATH")

                // Primero actualiza la playlist y el índice
                returnedPlaylist?.let { playlist ->
                    sharedPlayer.setPlaylist(playlist)
                    sharedPlayer.setCurrentIndex(returnedIdx)

                    // Obtén la canción actual basada en el índice
                    val currentSong = playlist.getOrNull(returnedIdx)

                    if (currentSong != null && returnedPath != null) {
                        val file = File(returnedPath)
                        if (file.exists()) {
                            // Dispara el pendingFile para actualizar el fragment
                            sharedPlayer.playFromFile(currentSong, file)

                            // Asegúrate de que el PlayerManager también tenga el archivo correcto
                            PlayerManager.playFile(this, file)
                        }
                    }
                }
            }
        }

    private val songInfoLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data ?: return@registerForActivityResult
                val song = data.getSerializableExtra("EXTRA_PLAYING_SONG") as? Song
                val path = data.getStringExtra("EXTRA_PLAYING_PATH")
                val idx = data.getIntExtra("EXTRA_INDEX", -1)

                // actualiza índice (para que la flechita Next/Prev funcione)
                if (idx >= 0) sharedPlayer.setCurrentIndex(idx)

                // re-dispara pendingFile para que el fragment refresque UI
                if (song != null && path != null) {
                    val file = File(path)
                    if (file.exists()) sharedPlayer.playFromFile(song, file)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestNotificationPermission()
        ApiService.init(this)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_player_container, PlayerControlFragment())
            .commit()

        viewPager = findViewById(R.id.viewPager)
        navView = findViewById(R.id.nav_view)

        viewPager.adapter = ViewPagerAdapter(this)

        navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> viewPager.currentItem = 0
                R.id.navigation_search -> viewPager.currentItem = 1
                R.id.navigation_playlists -> viewPager.currentItem = 2
                R.id.navigation_profile -> viewPager.currentItem = 3
            }
            true
        }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                navView.menu.getItem(position).isChecked = true
            }
        })

        lifecycleScope.launch {
            ConfigFCM()
        }
    }

    suspend fun ConfigFCM() {
        val tokenProvider = SharedPrefsTokenProvider(this)
        var authService: AuthService = AuthService(RestfulRoutes.getBaseUrl(), tokenProvider)
        val prefs = getSharedPreferences("fcm_prefs", android.content.Context.MODE_PRIVATE)
        val fcmToken = prefs.getString("fcm_token", null)

        if (fcmToken != null) {
            Log.d("MainActivity", "FCM TOKEN: $fcmToken")

            when (val r = authService.updateFcmToken(fcmToken, "android", "1.0.0", "1.0.0")) {
                is ApiResult.Success<Unit?> -> {
                    Log.d("MainActivity", "FCM Token sent successfully.")
                }

                is ApiResult.HttpError -> {
                    Log.e("MainActivity", "FCM TOKEN HTTP ${r.code}: ${r.message}")
                }

                is ApiResult.NetworkError -> {
                    Log.e("MainActivity", "FCM TOKEN RED: ${r.exception}")
                }

                is ApiResult.UnknownError -> {
                    Log.e("MainActivity", "FCM TOKEN ERROR: ${r.exception}")
                }
            }
        } else {
            Log.w("MainActivity", "FCM TOKEN not found in prefs")
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun playSong(song: Song) {
        // Implementa la lógica similar a HomeFragment
        val cacheFile = File(cacheDir, "song_${song.id}.mp3")
        if (cacheFile.exists()) {
            sharedPlayer.playFromFile(song, cacheFile)
            return
        }

        // Si no existe en caché, podrías descargarla o mostrar un mensaje
        Log.d("MainActivity", "Song not in cache: ${song.title}")
    }

    override fun playNext() {
        val playlist = sharedPlayer.playlist.value ?: return
        val currentIndex = sharedPlayer.currentIndex.value ?: return

        if (currentIndex + 1 < playlist.size) {
            val nextSong = playlist[currentIndex + 1]
            val cacheFile = File(cacheDir, "song_${nextSong.id}.mp3")

            if (cacheFile.exists()) {
                sharedPlayer.setCurrentIndex(currentIndex + 1)
                sharedPlayer.playFromFile(nextSong, cacheFile)
                PlayerManager.playFile(this, cacheFile)
            }
        } else {
            // Final de la playlist
            PlayerManager.getPlayer()?.seekTo(PlayerManager.getPlayer()!!.duration)
        }
    }

    override fun playPrevious() {
        val playlist = sharedPlayer.playlist.value ?: return
        val currentIndex = sharedPlayer.currentIndex.value ?: return

        if (currentIndex - 1 >= 0) {
            val prevSong = playlist[currentIndex - 1]
            val cacheFile = File(cacheDir, "song_${prevSong.id}.mp3")

            if (cacheFile.exists()) {
                sharedPlayer.setCurrentIndex(currentIndex - 1)
                sharedPlayer.playFromFile(prevSong, cacheFile)
                PlayerManager.playFile(this, cacheFile)
            }
        } else {
            // Principio de la playlist, reinicia la canción actual
            PlayerManager.getPlayer()?.seekTo(0)
        }
    }

    override fun openSongInfo(
        song: Song,
        filePath: String?,
        playlist: List<Song>,
        index: Int
    ) {
        val intent = Intent(this, SongInfoActivity::class.java).apply {
            putExtra("EXTRA_SONG_OBJ", song)
            filePath?.let { putExtra("EXTRA_FILE_PATH", it) }
            putExtra("EXTRA_PLAYLIST", ArrayList(playlist) as java.io.Serializable)
            putExtra("EXTRA_INDEX", index)
        }
        songInfoLauncher.launch(intent)
    }

    fun openPlaylistDetail(
        playlist: List<Song>,
        playingSong: Song? = null,
        playingPath: String? = null,
        playingIndex: Int = 0
    ) {
        val intent = Intent(this, PlaylistDetailActivity::class.java).apply {
            putIntegerArrayListExtra(
                "EXTRA_PLAYLIST_SONG_IDS",
                ArrayList(playlist.map { it.id })
            )
            putExtra("EXTRA_PLAYLIST", ArrayList(playlist))
            playingSong?.let { putExtra("EXTRA_PLAYING_SONG", it) }
            playingPath?.let { putExtra("EXTRA_PLAYING_PATH", it) }
            putExtra("EXTRA_INDEX", playingIndex)
        }
        playlistDetailLauncher.launch(intent)
    }
}
