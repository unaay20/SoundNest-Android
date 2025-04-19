package com.example.soundnest_android.ui.login

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.soundnest_android.MainActivity
import com.example.soundnest_android.R

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)  // ver más abajo

        // Suponiendo que tu fragmento de login está en R.id.login_container
        supportFragmentManager.beginTransaction()
            .replace(R.id.login_root, LoginFragment())
            .commit()
    }

    /** Llamarás a esto desde tu LoginFragment cuando el login sea exitoso */
    fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()  // quitas esta actividad de la pila
    }
}
