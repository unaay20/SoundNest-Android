package com.example.soundnest_android.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soundnest_android.auth.SharedPrefsTokenProvider
import com.example.soundnest_android.grpc.constants.GrpcRoutes
import com.example.soundnest_android.grpc.http.GrpcResult
import com.example.soundnest_android.grpc.services.UserImageGrpcService
import com.example.soundnest_android.restful.models.user.AdditionalInformation
import com.example.soundnest_android.restful.models.user.EditUserRequest
import com.example.soundnest_android.restful.services.UserService
import com.example.soundnest_android.restful.utils.ApiResult
import com.example.soundnest_android.utils.SingleLiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditProfileViewModel(
    private val userService: UserService,
    private val tokenProvider: SharedPrefsTokenProvider
) : ViewModel() {

    private val imageService by lazy {
        UserImageGrpcService(
            GrpcRoutes.getHost(),
            GrpcRoutes.getPort(),
            { tokenProvider.getToken() }
        )
    }

    private val _profile = MutableLiveData<UserProfile>()
    val profile: LiveData<UserProfile> = _profile

    private val _photoBytes = MutableLiveData<ByteArray?>()
    val photoBytes: LiveData<ByteArray?> = _photoBytes

    private val _saveResult = SingleLiveEvent<Boolean>()
    val saveResult: LiveData<Boolean> = _saveResult

    private val _errorEvent = SingleLiveEvent<String>()
    val errorEvent: LiveData<String> = _errorEvent

    init {
        loadProfile()
        val id = tokenProvider.id
        if (id != -1) id?.let { loadProfileImage(it) }
    }

    private fun loadProfile() {
        viewModelScope.launch(Dispatchers.IO) {
            val infoList = tokenProvider.additionalInfo
            val username = tokenProvider.username
                ?: throw IllegalStateException("No se encontró el nombre de usuario")
            val email = tokenProvider.email
                ?: throw IllegalStateException("No se encontró el correo del usuario")

            val uiModel = UserProfile(
                username = username,
                email = email,
                role = tokenProvider.role,
                additionalInformation = infoList
            )
            withContext(Dispatchers.Main) { _profile.value = uiModel }
        }
    }

    fun saveProfile(newUsername: String, infoList: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            val nameUser = newUsername
            val email = tokenProvider.email.orEmpty()
            val password = ""
            val additionalInformation = AdditionalInformation(info = infoList)

            when (val resp = userService.editUser(nameUser, email, password, additionalInformation)) {
                is ApiResult.Success -> {
                    tokenProvider.setUserName(newUsername)
                    withContext(Dispatchers.Main) {
                        _saveResult.value = true
                    }
                }

                is ApiResult.HttpError -> {
                    val msg = "Error ${resp.code}: ${resp.message}"
                    withContext(Dispatchers.Main) {
                        _errorEvent.value = msg
                    }
                }

                is ApiResult.NetworkError -> {
                    val msg = "Network error: ${resp.exception.message}"
                    withContext(Dispatchers.Main) {
                        _errorEvent.value = msg
                    }
                }

                is ApiResult.UnknownError -> {
                    val msg = "Unknown error: ${resp.exception.message}"
                    withContext(Dispatchers.Main) {
                        _errorEvent.value = msg
                    }
                }
            }
        }
    }


    fun loadProfileImage(userId: Int) = viewModelScope.launch {
        when (val res = imageService.downloadImage(userId)) {
            is GrpcResult.Success    -> _photoBytes.postValue(res.data?.imageData?.toByteArray())
            is GrpcResult.GrpcError  -> _errorEvent.postValue("gRPC error: ${res.message}")
            is GrpcResult.NetworkError-> _errorEvent.postValue("Network error: ${res.exception.message}")
            is GrpcResult.UnknownError-> _errorEvent.postValue("Unknown: ${res.exception.message}")
        }
    }

    override fun onCleared() {
        super.onCleared()
        imageService.close()
    }
}
