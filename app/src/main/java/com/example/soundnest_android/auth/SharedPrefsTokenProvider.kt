package com.example.soundnest_android.auth

import android.content.Context
import android.util.Base64
import com.auth0.android.jwt.JWT
import com.example.soundnest_android.R
import com.example.soundnest_android.restful.utils.TokenProvider
import com.google.gson.Gson
import org.json.JSONObject

class SharedPrefsTokenProvider(private val context: Context) : TokenProvider {
    private val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    private val KEY_TOKEN = "key_token"
    private val KEY_ADDITIONAL_INFO = "additional_info"

    private val gson = Gson()

    override fun getToken(): String? =
        prefs.getString(KEY_TOKEN, null)

    override fun shouldAttachToken(): Boolean {
        val token = getToken().takeUnless { it.isNullOrBlank() } ?: return false
        val jwt = try {
            JWT(token)
        } catch (e: Exception) {
            return false
        }
        return !jwt.isExpired(10)
    }

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

    fun setUserName(username: String) {
        val claims = getAllClaims()?.toMutableMap() ?: mutableMapOf()
        claims["username"] = username
        saveClaims(claims)
    }

    val email: String?
        get() = decodeJWT()?.getClaim("email")?.asString()

    val role: String
        get() {
            val id = decodeJWT()?.getClaim("role")?.asInt()
            val rolListener = context.getString(R.string.lbl_rol_listener)
            val rolModerator = context.getString(R.string.lbl_rol_moderator)
            val rolUnknown = context.getString(R.string.lbl_rol_unknown)

            return when (id) {
                1 -> rolListener
                2 -> rolModerator
                else -> rolUnknown
            }
        }

    fun saveAdditionalInformation(infoJson: String) {
        prefs.edit()
            .putString(KEY_ADDITIONAL_INFO, infoJson)
            .apply()
    }

    fun getAdditionalInformation(): String {
        return prefs
            .getString(KEY_ADDITIONAL_INFO, "{}")
            .orEmpty()
    }

    fun saveClaims(claims: Map<String, Any>) {
        val headerJson = JSONObject(
            mapOf(
                "alg" to "none",
                "typ" to "JWT"
            )
        ).toString()

        val payloadJson = JSONObject(claims).toString()

        val headerB64 = Base64.encodeToString(
            headerJson.toByteArray(Charsets.UTF_8),
            Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
        )
        val payloadB64 = Base64.encodeToString(
            payloadJson.toByteArray(Charsets.UTF_8),
            Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
        )

        val newToken = "$headerB64.$payloadB64."

        prefs.edit()
            .putString(KEY_TOKEN, newToken)
            .apply()
    }

    fun getAuthHeader(): String? =
        getToken()?.let { "Bearer $it" }

    fun getUserId(): Int {
        return id ?: -1
    }
}
