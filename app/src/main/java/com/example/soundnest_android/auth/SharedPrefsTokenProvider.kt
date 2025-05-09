package com.example.soundnest_android.auth

import android.content.Context
import com.auth0.android.jwt.JWT
import com.example.soundnest_android.restful.utils.TokenProvider

class SharedPrefsTokenProvider(context: Context) : TokenProvider {
    private val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    private val KEY_TOKEN = "key_token"

    override fun getToken(): String? =
        prefs.getString(KEY_TOKEN, null)

    override fun shouldAttachToken(): Boolean =
        !getToken().isNullOrBlank()

    fun saveToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    fun clearSession() {
        prefs.edit().remove(KEY_TOKEN).apply()
    }

    private fun decodeJWT(): JWT? =
        getToken()?.let { JWT(it) }

    fun getAllClaims(): Map<String, Any>? {
        val decodedJWT = decodeJWT()
        return decodedJWT?.claims?.mapValues { it.value.asString().toString() }
    }

    val id: Int?
        get() = decodeJWT()?.getClaim("id")?.asInt()

    val username: String?
        get() = decodeJWT()?.getClaim("username")?.asString()

    val email: String?
        get() = decodeJWT()?.getClaim("email")?.asString()

    val role: String
        get() {
            val id = decodeJWT()?.getClaim("role")?.asInt()
            return when (id) {
                1 -> "Escucha"
                2 -> "Moderador"
                else -> "Rol desconocido"
            }
        }

    val additionalInfo: List<String>
        get() = decodeJWT()?.getClaim("info")?.asList(String::class.java) ?: emptyList()

    fun getAuthHeader(): String? =
        getToken()?.let { "Bearer $it" }
}
