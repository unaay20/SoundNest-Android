package com.example.soundnest_android

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.soundnest_android.databinding.ActivityMainBinding
import com.example.soundnest_android.ui.ViewPagerAdapter

class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var navView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewPager = findViewById(R.id.viewPager)
        navView = findViewById(R.id.nav_view)

        // Adapter del ViewPager
        val adapter = ViewPagerAdapter(this)
        viewPager.adapter = adapter

        // Listener para BottomNavigationView → cambia página
        navView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_home -> viewPager.currentItem = 0
                R.id.navigation_search -> viewPager.currentItem = 1
                R.id.navigation_playlists -> viewPager.currentItem = 2
                R.id.navigation_profile -> viewPager.currentItem = 3
            }
            true
        }

        // Listener del ViewPager → cambia icono seleccionado
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                navView.menu.getItem(position).isChecked = true
            }
        })
    }
}
