package com.example.soundnest_android.restful.utils

interface TokenProvider {
    fun getToken(): String?
    fun shouldAttachToken(): Boolean
}