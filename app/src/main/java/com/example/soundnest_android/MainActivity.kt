package com.example.soundnest_android

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.soundnest_android.databinding.ActivityMainBinding
import com.example.soundnest_android.ui.ViewPagerAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var navView: BottomNavigationView
    private lateinit var btnPlayPause: ImageButton
    private lateinit var btnBack: ImageButton
    private lateinit var btnNext: ImageButton
    private var isPlaying = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializar las vistas
        songImage = findViewById(R.id.songImage)
        songTitle = findViewById(R.id.songTitle)
        artistName = findViewById(R.id.artistName)

        // Inicializar la canción por defecto
        setDefaultSong()
        // Inicializar el ViewPager y el BottomNavigationView
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

        // Accede al layout del reproductor (este es el layout que se incluye)
        val playerControl = findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.playerControl)

        // Ahora accede a los botones dentro de ese layout
        btnPlayPause = playerControl.findViewById(R.id.btnPlayPause)
        btnBack = playerControl.findViewById(R.id.btnBack)
        btnNext = playerControl.findViewById(R.id.btnNext)

        val playIcon = findViewById<ImageView>(R.id.playIcon)
        val pauseIcon = findViewById<ImageView>(R.id.pauseIcon)

        // Acciones de los botones
        btnPlayPause.setOnClickListener {
            if (isPlaying) {
                playIcon.visibility = View.VISIBLE
                pauseIcon.visibility = View.GONE
                // Detener la canción
                isPlaying = false
            } else {
                playIcon.visibility = View.GONE
                pauseIcon.visibility = View.VISIBLE
                // Reproducir la canción
                isPlaying = true
            }
        }

        btnNext.setOnClickListener {
            // Cambiar a siguiente canción
        }

        btnBack.setOnClickListener {
            // Cambiar a canción anterior
        }
    }

    private lateinit var songImage: ImageView
    private lateinit var songTitle: TextView
    private lateinit var artistName: TextView


    // Función para configurar la canción por defecto
    private fun setDefaultSong() {
        // Configura la imagen por defecto de la canción
        songImage.setImageResource(R.drawable.img_default_song)  // Usando el icono de nota musical por defecto

        // Configura el título y el artista
        songTitle.text = "19 días y 500 noches"
        artistName.text = "Joaquín Sabina"
    }
}
