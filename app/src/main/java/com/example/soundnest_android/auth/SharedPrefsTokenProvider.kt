package com.example.soundnest_android.auth

import android.content.Context
import com.auth0.android.jwt.JWT
import com.example.soundnest_android.R
import android.util.Base64
import com.example.soundnest_android.restful.models.user.AdditionalInformation
import org.json.JSONObject
import com.example.soundnest_android.restful.utils.TokenProvider
import com.google.common.reflect.TypeToken
import com.google.gson.Gson

class SharedPrefsTokenProvider(private val context: Context) : TokenProvider {
    private val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    private val KEY_TOKEN = "key_token"

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



    fun saveAdditionalInformation(info: AdditionalInformation) {
        prefs.edit()
            .putString("additional_info", gson.toJson(info.info))
            .apply()
    }

    fun getAdditionalInformation(): AdditionalInformation {
        val json = prefs.getString("additional_info", null)
        if (!json.isNullOrEmpty()) {
            val type = object : TypeToken<Map<String, List<String>>>() {}.type
            val infoMap: Map<String, List<String>> = gson.fromJson(json, type)
            return AdditionalInformation(infoMap)
        }
        return AdditionalInformation(emptyMap())
    }

    fun saveClaims(claims: Map<String, Any>) {
        val headerJson = JSONObject(mapOf(
            "alg" to "none",
            "typ" to "JWT"
        )).toString()

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

    fun getUserId(): Int{
        return id ?: -1
    }
}
