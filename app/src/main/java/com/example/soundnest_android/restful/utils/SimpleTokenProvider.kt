package com.example.soundnest_android.restful.utils

import com.example.soundnest_android.restful.utils.TokenProvider

class SimpleTokenProvider : TokenProvider {
    private var token: String? = null
    private var attach: Boolean = false

    fun updateToken(newToken: String) {
        token = newToken
    }

    fun enableToken(enable: Boolean) {
        attach = enable
    }

    override fun getToken() = token
    override fun shouldAttachToken() = attach
}