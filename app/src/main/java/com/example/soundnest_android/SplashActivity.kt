package com.example.soundnest_android

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.soundnest_android.auth.SharedPrefsTokenProvider
import com.example.soundnest_android.ui.login.LoginActivity
import com.example.soundnest_android.MainActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tokenProvider = SharedPrefsTokenProvider(this)

        if (tokenProvider.shouldAttachToken()) {
            // Ya hay sesión iniciada, vamos directo a MainActivity
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            // No hay token, mostrar pantalla de login
            startActivity(Intent(this, LoginActivity::class.java))
        }

        finish() // Cerramos el Splash
    }
}
