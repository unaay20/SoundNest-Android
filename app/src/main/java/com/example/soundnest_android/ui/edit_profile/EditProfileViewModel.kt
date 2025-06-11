package com.example.soundnest_android.ui.edit_profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.soundnest_android.auth.SharedPrefsTokenProvider
import com.example.soundnest_android.business_logic.UserProfile
import com.example.soundnest_android.grpc.constants.GrpcRoutes
import com.example.soundnest_android.grpc.http.GrpcResult
import com.example.soundnest_android.grpc.services.UserImageGrpcService
import com.example.soundnest_android.restful.models.user.AdditionalInformation
import com.example.soundnest_android.restful.services.UserService
import com.example.soundnest_android.restful.utils.ApiResult
import com.example.soundnest_android.utils.SingleLiveEvent
import com.example.soundnest_android.utils.toDisplayMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditProfileViewModel(
    application: Application,
    private val userService: UserService,
    private val tokenProvider: SharedPrefsTokenProvider
) : AndroidViewModel(application) {

    private val imageService by lazy {
        UserImageGrpcService(
            GrpcRoutes.getHost(),
            GrpcRoutes.getPort(),
            { tokenProvider.getToken() }
        )
    }

    private val _profile = MutableLiveData<UserProfile?>()
    val profile: LiveData<UserProfile?> = _profile

    private val _photoBytes = MutableLiveData<ByteArray?>()
    val photoBytes: LiveData<ByteArray?> = _photoBytes

    private val _saveResult = SingleLiveEvent<Boolean>()
    val saveResult: LiveData<Boolean> = _saveResult

    private val _errorEvent = SingleLiveEvent<String>()
    val errorEvent: LiveData<String> = _errorEvent

    init {
        loadProfile()
        tokenProvider.id?.takeIf { it != -1 }?.let { loadProfileImage(it) }
    }

    private fun loadProfile() {
        viewModelScope.launch(Dispatchers.IO) {
            val username = tokenProvider.username
                ?: throw IllegalStateException("There is no username")
            val email = tokenProvider.email
                ?: throw IllegalStateException("There is no email")

            val additionalInfoString = when (val r = userService.getAdditionalInfo()) {
                is ApiResult.Success ->
                    r.data?.info?.takeIf(String::isNotBlank)
                        ?.also { tokenProvider.saveAdditionalInformation(it) }
                        ?: tokenProvider.getAdditionalInformation()

                else ->
                    tokenProvider.getAdditionalInformation()
            }

            val uiModel = UserProfile(
                username = username,
                email = email,
                role = tokenProvider.role,
                additionalInformation = additionalInfoString
            )

            withContext(Dispatchers.Main) {
                _profile.value = uiModel
            }
        }
    }


    fun saveProfile(newUsername: String, info: AdditionalInformation) {
        viewModelScope.launch(Dispatchers.IO) {
            val email = tokenProvider.email.orEmpty()

            when (val resp = userService.editUser(newUsername, email, info)) {
                is ApiResult.Success -> {
                    tokenProvider.setUserName(newUsername)
                    withContext(Dispatchers.Main) {
                        _saveResult.value = true
                    }
                }

                else -> {
                    val msg = resp.toDisplayMessage(getApplication())
                    withContext(Dispatchers.Main) {
                        _errorEvent.value = msg
                    }
                }
            }
        }
    }

    fun loadProfileImage(userId: Int) = viewModelScope.launch {
        when (val res = imageService.downloadImage(userId)) {
            is GrpcResult.Success -> _photoBytes.postValue(res.data?.imageData?.toByteArray())
            is GrpcResult.GrpcError -> _errorEvent.postValue("gRPC error: ${res.message}")
            is GrpcResult.NetworkError -> _errorEvent.postValue("Network error: ${res.exception.message}")
            is GrpcResult.UnknownError -> _errorEvent.postValue("Unknown: ${res.exception.message}")
        }
    }

    override fun onCleared() {
        super.onCleared()
        imageService.close()
    }
}
