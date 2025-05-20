package com.example.soundnest_android

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.example.soundnest_android.auth.SharedPrefsTokenProvider
import com.example.soundnest_android.network.ApiService
import com.example.soundnest_android.restful.constants.RestfulRoutes
import com.example.soundnest_android.restful.services.AuthService
import android.Manifest
import com.example.soundnest_android.restful.utils.ApiResult
import com.example.soundnest_android.ui.ViewPagerAdapter
import com.example.soundnest_android.ui.player.PlayerControlFragment
import com.example.soundnest_android.ui.player.PlayerManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var navView: BottomNavigationView
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("FCM", "Permiso de notificaciones concedido")
        } else {
            Log.w("FCM", "Permiso de notificaciones denegado")
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

    suspend fun ConfigFCM(){
        val tokenProvider = SharedPrefsTokenProvider(this)
        var authService : AuthService = AuthService(RestfulRoutes.getBaseUrl(), tokenProvider)
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
}
