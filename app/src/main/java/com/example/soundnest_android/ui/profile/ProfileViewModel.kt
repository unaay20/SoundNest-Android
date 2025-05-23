package com.example.soundnest_android.ui.profile

import androidx.lifecycle.*
import com.example.soundnest_android.auth.SharedPrefsTokenProvider
import com.example.soundnest_android.business_logic.UserProfile
import com.example.soundnest_android.grpc.services.UserImageGrpcService
import com.example.soundnest_android.grpc.constants.GrpcRoutes
import com.example.soundnest_android.grpc.http.GrpcResult
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val prefs: SharedPrefsTokenProvider,
    private val tokenProvider: () -> String?
) : ViewModel() {

    private val imageService by lazy {
        UserImageGrpcService(
            GrpcRoutes.getHost(),
            GrpcRoutes.getPort(),
            tokenProvider
        )
    }

    private val _profile = MutableLiveData<UserProfile>().apply {
        value = UserProfile(
            username = prefs.username.orEmpty(),
            email    = prefs.email.orEmpty(),
            role     = prefs.role,
            additionalInformation = prefs.getAdditionalInformation()
        )
    }
    val profile: LiveData<UserProfile> = _profile

    private val _photoBytes = MutableLiveData<ByteArray?>()
    val photoBytes: LiveData<ByteArray?> = _photoBytes

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadProfileImage(userId: Int) = viewModelScope.launch {
        when (val res = imageService.downloadImage(userId)) {
            is GrpcResult.Success    -> _photoBytes.value = res.data?.imageData?.toByteArray()
            is GrpcResult.GrpcError  -> _error.value = "gRPC error: ${res.message}"
            is GrpcResult.NetworkError-> _error.value = "Network error: ${res.exception.message}"
            is GrpcResult.UnknownError-> _error.value = "Unknown: ${res.exception.message}"
        }
    }

    fun uploadProfileImage(userId: Int, bytes: ByteArray, ext: String) = viewModelScope.launch {
        when (val res = imageService.uploadImage(userId, bytes, ext)) {
            is GrpcResult.Success    -> loadProfileImage(userId)
            is GrpcResult.GrpcError  -> _error.value = "gRPC error: ${res.message}"
            is GrpcResult.NetworkError-> _error.value = "Network error: ${res.exception.message}"
            is GrpcResult.UnknownError-> _error.value = "Unknown: ${res.exception.message}"
        }
    }

    override fun onCleared() {
        super.onCleared()
        imageService.close()
    }
}