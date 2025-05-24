package com.example.soundnest_android.business_logic

import com.example.soundnest_android.restful.models.user.AdditionalInformation

data class UserProfile(
    val username: String,
    val email: String,
    val role: String,
    val additionalInformation: String
)