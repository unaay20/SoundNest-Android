package com.example.soundnest_android

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.soundnest_android.network.ApiService
import com.example.soundnest_android.ui.ViewPagerAdapter
import com.example.soundnest_android.ui.player.PlayerControlFragment
import com.example.soundnest_android.ui.player.PlayerManager
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var navView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ApiService.init(this)

        PlayerManager.init(this, R.raw.mi_cancion)

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
    }
}
